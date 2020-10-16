/*
 * Copyright (c) 2018 the ACRA team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acra.processor.util

import androidx.annotation.StringRes
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.asTypeVariableName
import org.acra.annotation.AnyNonDefault
import org.acra.annotation.BuilderMethod
import org.acra.annotation.Configuration
import org.acra.annotation.ConfigurationValue
import org.acra.annotation.Instantiatable
import org.acra.annotation.NonEmpty
import org.acra.annotation.PreBuild
import org.acra.annotation.Transform
import org.acra.collections.ImmutableList
import org.acra.collections.ImmutableMap
import org.acra.collections.ImmutableSet
import java.lang.Deprecated
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic
import kotlin.IllegalArgumentException
import kotlin.String

/**
 * @author F43nd1r
 * @since 11.01.2018
 */
object Types {
    val IMMUTABLE_MAP: ClassName = ImmutableMap::class.asClassName()
    val IMMUTABLE_SET: ClassName = ImmutableSet::class.asClassName()
    val IMMUTABLE_LIST: ClassName = ImmutableList::class.asClassName()
    val MAP: ClassName = MutableMap::class.asClassName()
    val SET: ClassName = MutableSet::class.asClassName()
    val LIST: ClassName = MutableList::class.asClassName()
    @JvmField
    val STRING: ClassName = ClassName("kotlin", "String")
    @JvmField
    val STRING_RES = AnnotationSpec.builder(StringRes::class.java).build()
    @JvmField
    val DEPRECATED = AnnotationSpec.builder(kotlin.Deprecated::class.java).build()
    @JvmField
    val ANY_NON_DEFAULT: ClassName = AnyNonDefault::class.asClassName()
    val BUILDER_METHOD: ClassName = BuilderMethod::class.asClassName()
    val CONFIGURATION: ClassName = Configuration::class.asClassName()
    val CONFIGURATION_VALUE: ClassName = ConfigurationValue::class.asClassName()
    @JvmField
    val INSTANTIATABLE: ClassName = Instantiatable::class.asClassName()
    @JvmField
    val NON_EMPTY: ClassName = NonEmpty::class.asClassName()
    val PRE_BUILD: ClassName = PreBuild::class.asClassName()
    val TRANSFORM: ClassName = Transform::class.asClassName()
    @JvmField
    val CONTEXT: ClassName = ClassName.bestGuess(Strings.CONTEXT)
    val CONFIGURATION_BUILDER_FACTORY: ClassName = ClassName.bestGuess(Strings.CONFIGURATION_BUILDER_FACTORY)
    @JvmField
    val MARKER_ANNOTATIONS = listOf(ANY_NON_DEFAULT, BUILDER_METHOD, CONFIGURATION, CONFIGURATION_VALUE, INSTANTIATABLE, NON_EMPTY, PRE_BUILD, TRANSFORM)
    @JvmStatic
    fun overriding(method: ExecutableElement): FunSpec.Builder {
        return FunSpec.builder(method.simpleName.toString())
                .addModifiers(KModifier.OVERRIDE, *method.modifiers.mapNotNull { it.toKModifier() }.minus(KModifier.ABSTRACT).toTypedArray())
                .returns(method.returnType.asTypeName())
                .addTypeVariables(method.typeParameters.map { it.asTypeVariableName() })
                .addParameters(method.parameters.map { element: VariableElement ->
                    ParameterSpec.builder(element.simpleName.toString(), element.asType().asTypeName()).addAnnotations(element.annotationMirrors.map { AnnotationSpec.get(it) }).build()
                })
    }

    fun getOnlyMethod(processingEnv: ProcessingEnvironment, className: String?): ExecutableElement {
        val typeElement = processingEnv.elementUtils.getTypeElement(className)
        val elements = ElementFilter.methodsIn(typeElement.enclosedElements)
        return if (elements.size == 1) {
            elements[0]
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Needs exactly one method", typeElement)
            throw IllegalArgumentException()
        }
    }
}