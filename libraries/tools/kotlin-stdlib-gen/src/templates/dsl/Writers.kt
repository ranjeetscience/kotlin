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

package templates

import java.io.File
import java.io.FileWriter

val COMMON_AUTOGENERATED_WARNING: String = """//
// NOTE THIS FILE IS AUTO-GENERATED by the GenerateStandardLib.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//"""

data class PlatformSourceFile(
        val platform: Platform,
        val sourceFile: SourceFile
)

private val platformsToGenerate = Platform.values - Platform.Native

@JvmName("groupByFileAndWriteGroups")
fun Sequence<TemplateGroup>.groupByFileAndWrite(
        fileNameBuilder: (PlatformSourceFile) -> File
) {
    flatMap { group ->
        group.invoke()
                .flatMap { it.instantiate(platformsToGenerate) }
                .sortedBy { it.sortingSignature }
    }.groupByFileAndWrite(fileNameBuilder)
}

@JvmName("groupByFileAndWriteTemplates")
fun Sequence<MemberTemplate>.groupByFileAndWrite(
        fileNameBuilder: (PlatformSourceFile) -> File
) {
    flatMap { it.instantiate(platformsToGenerate) }
        .groupByFileAndWrite(fileNameBuilder)
}

@JvmName("groupByFileAndWriteMembers")
fun Sequence<MemberBuilder>.groupByFileAndWrite(
        fileNameBuilder: (PlatformSourceFile) -> File
) {
    val groupedMembers = groupBy { PlatformSourceFile(it.platform, it.sourceFile) }

    for ((psf, members) in groupedMembers) {
        val file = fileNameBuilder(psf)
        members.writeTo(file, psf)
    }
}

fun List<MemberBuilder>.writeTo(file: File, platformSource: PlatformSourceFile) {
    val (platform, sourceFile) = platformSource
    println("Generating file: $file")
    file.parentFile.mkdirs()
    FileWriter(file).use { writer ->
        if (sourceFile.multifile) {
            writer.appendln("@file:kotlin.jvm.JvmMultifileClass")
        }

        writer.appendln("@file:kotlin.jvm.JvmName(\"${sourceFile.jvmClassName}\")")
        if (platform == Platform.JVM)
            writer.appendln("@file:kotlin.jvm.JvmVersion")
        writer.appendln()

        writer.append("package ${sourceFile.packageName ?: "kotlin"}\n\n")
        writer.append("${COMMON_AUTOGENERATED_WARNING}\n\n")
        if (platform == Platform.JS) {
            writer.appendln("import kotlin.js.*")
            if (sourceFile == SourceFile.Arrays) {
                writer.appendln("import primitiveArrayConcat")
                writer.appendln("import withType")
            }
        }
        writer.appendln("import kotlin.*")
        writer.appendln("import kotlin.text.*")
        writer.appendln("import kotlin.comparisons.*")

        if (platform != Platform.Common && sourceFile == SourceFile.Sequences) {
            writer.appendln("import kotlin.coroutines.experimental.*")
        }

        writer.appendln()

        for (f in this) {
            f.build(writer)
        }
    }
}
