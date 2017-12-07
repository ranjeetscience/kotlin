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

package org.jetbrains.kotlin.idea.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.constants.evaluate.isStandaloneOnlyConstant
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.jvm.annotations.hasJvmFieldAnnotation
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatform
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class FakeJvmFieldConstantInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : KtVisitorVoid() {
            override fun visitProperty(property: KtProperty) {
                super.visitProperty(property)
                if (property.platform != JvmPlatform) return

                if (property.isLocal) return
                val initializer = property.initializer ?: return
                if (property.hasModifier(KtTokens.CONST_KEYWORD)) return
                // Top-level or object only
                if (property.containingClassOrObject is KtClass) return
                if (property.annotationEntries.isEmpty()) return

                // For some reason constant evaluation does not work for property.analyze()
                val context = initializer.analyze(BodyResolveMode.PARTIAL)
                val propertyDescriptor = context[BindingContext.DECLARATION_TO_DESCRIPTOR, property] as? VariableDescriptor ?: return
                if (!propertyDescriptor.hasJvmFieldAnnotation()) return

                val initializerValue = ConstantExpressionEvaluator.getConstant(
                        initializer, context
                )?.toConstantValue(propertyDescriptor.type) ?: return
                if (initializerValue.isStandaloneOnlyConstant()) return

                // TODO: should we check whether the property is used in Java annotations?
                // It's a bit controversial because, first, reference search can be expensive,
                // and second, it can be library property which is used in annotation in another project

                holder.registerProblem(
                        property.nameIdentifier ?: property,
                        "Non-const property is incorrectly declared as 'ConstantValue' in bytecode. " +
                        "This behaviour is subject to change in 1.3 thus breaking Java compatibility",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        ReplaceJvmFieldWithConstFix()
                )
            }
        }
    }

    private class ReplaceJvmFieldWithConstFix : LocalQuickFix {
        override fun getName() = "Replace '@JvmField' with 'const'"

        override fun getFamilyName() = name

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val property = descriptor.psiElement.getParentOfType<KtProperty>(strict = false) ?: return
            val context = property.analyze(BodyResolveMode.PARTIAL)
            property.annotationEntries.find { annotationEntry ->
                val declarationDescriptor = context[BindingContext.TYPE, annotationEntry.typeReference]?.constructor?.declarationDescriptor
                declarationDescriptor?.fqNameSafe == FqName("kotlin.jvm.JvmField")
            }?.delete()
            property.addModifier(KtTokens.CONST_KEYWORD)
        }
    }
}