package org.acra.processor.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import java.io.File
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName


fun Modifier.toKModifier(): KModifier? {
    return when (this) {
        Modifier.PUBLIC -> KModifier.PUBLIC
        Modifier.PROTECTED -> KModifier.PROTECTED
        Modifier.PRIVATE -> KModifier.PRIVATE
        Modifier.ABSTRACT -> KModifier.ABSTRACT
        Modifier.FINAL -> KModifier.FINAL
        else -> null
    }
}


fun TypeName.javaToKotlinType(): TypeName {
    return when (this) {
        is ParameterizedTypeName -> (rawType.javaToKotlinType() as ClassName).parameterizedBy(*typeArguments.map { it.javaToKotlinType() }.toTypedArray())
        is WildcardTypeName -> {
            if (inTypes.isNotEmpty()) WildcardTypeName.consumerOf(inTypes[0].javaToKotlinType())
            else WildcardTypeName.producerOf(outTypes[0].javaToKotlinType())
        }
        else -> JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()?.let { ClassName.bestGuess(it) } ?: this
    }
}

fun FileSpec.Builder.writeTo(processingEnv: ProcessingEnvironment) {
    val dir = (processingEnv.options["kapt.kotlin.generated"]
            ?: processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.").let { throw IllegalArgumentException() })
    addComment("""
                    |Copyright (c) ${Calendar.getInstance()[Calendar.YEAR]}
                    |
                    |Licensed under the Apache License, Version 2.0 (the "License");
                    |you may not use this file except in compliance with the License.
                    |
                    |http://www.apache.org/licenses/LICENSE-2.0
                    |
                    |Unless required by applicable law or agreed to in writing, software
                    |distributed under the License is distributed on an "AS IS" BASIS,
                    |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                    |See the License for the specific language governing permissions and
                    |limitations under the License.
                    """.trimMargin())
            .build()
            .writeTo(File(dir))
}