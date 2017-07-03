/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.resolve.calls.context.CheckArgumentTypesMode
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemOperation
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintInjector
import org.jetbrains.kotlin.resolve.calls.inference.components.SimpleConstraintSystemImpl
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.results.FlatSignature
import org.jetbrains.kotlin.resolve.calls.results.OverloadingConflictResolver
import org.jetbrains.kotlin.resolve.calls.results.TypeSpecificityComparator
import org.jetbrains.kotlin.resolve.calls.tower.ImplicitScopeTower
import org.jetbrains.kotlin.resolve.calls.tower.TowerResolver
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addIfNotNull


class CallableReferenceOverloadConflictResolver(
        builtIns: KotlinBuiltIns,
        specificityComparator: TypeSpecificityComparator,
        statelessCallbacks: KotlinResolutionStatelessCallbacks,
        constraintInjector: ConstraintInjector
) : OverloadingConflictResolver<CallableReferenceCandidate>(
        builtIns,
        specificityComparator,
        { it.candidate },
        { SimpleConstraintSystemImpl(constraintInjector, builtIns) },
        Companion::createFlatSignature,
        { null },
        { statelessCallbacks.isDescriptorFromSource(it) }
        ) {
    companion object {
        private fun createFlatSignature(candidate: CallableReferenceCandidate) =
                FlatSignature.createFromReflectionType(candidate, candidate.candidate, candidate.numDefaults, candidate.reflectionCandidateType)
    }
}


class CallableReferenceResolver(
        private val towerResolver: TowerResolver,
        private val callableReferenceOverloadConflictResolver: CallableReferenceOverloadConflictResolver,
        private val callComponents: KotlinCallComponents
) {

    fun processCallableReferenceArgument(
            csBuilder: ConstraintSystemBuilder,
            resolvedAtom: ResolvedCallableReferenceAtom
    ) {
        val argument = resolvedAtom.atom
        val expectedType = resolvedAtom.expectedType?.let { csBuilder.buildCurrentSubstitutor().safeSubstitute(it) }

        val scopeTower = callComponents.statelessCallbacks.getScopeTowerForCallableReferenceArgument(argument)
        val candidates = runRHSResolution(scopeTower, argument, expectedType) { checkCallableReference ->
            csBuilder.runTransaction { checkCallableReference(this); false }
        }
        val diagnostics = SmartList<KotlinCallDiagnostic>()

        val chosenCandidate = candidates.singleOrNull()
        if (chosenCandidate != null) {
            val (toFreshSubstitutor, diagnostic) = with(chosenCandidate) {
                csBuilder.checkCallableReference(argument, dispatchReceiver, extensionReceiver, candidate,
                                                 reflectionCandidateType, expectedType, scopeTower.lexicalScope.ownerDescriptor)
            }
            diagnostics.addIfNotNull(diagnostic)
            chosenCandidate.freshSubstitutor = toFreshSubstitutor
        }
        else {
            if (candidates.isEmpty()) {
                diagnostics.add(NoneCallableReferenceCandidates(argument))
            }
            else {
                diagnostics.add(CallableReferenceCandidatesAmbiguity(argument, candidates))
            }
        }

        // todo -- create this inside CallableReferencesCandidateFactory
        val subKtArguments = listOfNotNull(buildResolvedKtArgument(argument.lhsResult))

        resolvedAtom.setAnalyzedResults(chosenCandidate, subKtArguments, diagnostics)
    }

    private fun buildResolvedKtArgument(lhsResult: LHSResult): ResolvedAtom? {
        if (lhsResult !is LHSResult.Expression) return null
        val lshCallArgument = lhsResult.lshCallArgument
        return when(lshCallArgument) {
            is SubKotlinCallArgument -> lshCallArgument.callResult
            is ExpressionKotlinCallArgument -> ResolvedExpressionAtom(lshCallArgument)
            else -> unexpectedArgument(lshCallArgument)
        }
    }

    private fun runRHSResolution(
            scopeTower: ImplicitScopeTower,
            callableReference: CallableReferenceKotlinCallArgument,
            expectedType: UnwrappedType?, // this type can have not fixed type variable inside
            compatibilityChecker: ((ConstraintSystemOperation) -> Unit) -> Unit // you can run anything throw this operation and all this operation will be rolled back
    ): Set<CallableReferenceCandidate> {
        val factory = CallableReferencesCandidateFactory(callableReference, callComponents, scopeTower, compatibilityChecker, expectedType)
        val processor = createCallableReferenceProcessor(factory)
        val candidates = towerResolver.runResolve(scopeTower, processor, useOrder = true)
        return callableReferenceOverloadConflictResolver.chooseMaximallySpecificCandidates(
                candidates,
                CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS,
                discriminateGenerics = false, // we can't specify generics explicitly for callable references
                isDebuggerContext = scopeTower.isDebuggerContext)
    }
}
