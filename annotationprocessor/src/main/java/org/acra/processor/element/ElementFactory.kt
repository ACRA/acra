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
package org.acra.processor.element

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.asTypeVariableName
import org.acra.processor.util.Types
import org.acra.processor.util.javaToKotlinType
import org.acra.processor.util.toKModifier
import org.apache.commons.lang3.tuple.Pair
import java.util.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.util.Elements

/**
 * @author F43nd1r
 * @since 10.01.2018
 */
class ElementFactory(private val elements: Elements) {
    fun fromAnnotationMethod(method: ExecutableElement): Element {
        val annotations = getAnnotations(method)
        return AnnotationField.Normal(method.simpleName.toString(), method.returnType.asTypeName().javaToKotlinType(), annotations.left, annotations.right, method.defaultValue,
                elements.getDocComment(method))
    }

    fun fromClassAnnotationMethod(method: ExecutableElement): Element {
        val annotations = getAnnotations(method)
        return AnnotationField.Clazz(method.simpleName.toString(), method.returnType.asTypeName().javaToKotlinType(), annotations.left, annotations.right, method.defaultValue,
                elements.getDocComment(method))
    }

    fun fromStringResourceAnnotationMethod(method: ExecutableElement): Element {
        val annotations = getAnnotations(method)
        return AnnotationField.StringResource(method.simpleName.toString(), annotations.left, annotations.right,
                method.defaultValue, elements.getDocComment(method))
    }

    fun fromBuilderDelegateMethod(method: ExecutableElement): Element {
        return DelegateMethod(method.simpleName.toString(), method.returnType.asTypeName().javaToKotlinType(), getAnnotations(method).left,
                method.parameters.map { ParameterSpec.builder(it.simpleName.toString(), it.asType().asTypeName().javaToKotlinType()).build() },
                method.typeParameters.map { it.asTypeVariableName() }, method.modifiers.mapNotNull { it.toKModifier() }, elements.getDocComment(method))
    }

    fun fromConfigDelegateMethod(method: ExecutableElement): Element {
        return DelegateMethod.Config(method.simpleName.toString(), method.returnType.asTypeName().javaToKotlinType(), getAnnotations(method).left,
                method.parameters.map { ParameterSpec.builder(it.simpleName.toString(), it.asType().asTypeName().javaToKotlinType()).build() },
                method.typeParameters.map { it.asTypeVariableName() }, method.modifiers.mapNotNull { it.toKModifier() }, elements.getDocComment(method))
    }

    fun fromPreBuildDelegateMethod(method: ExecutableElement): Element {
        return PreBuildMethod(method.simpleName.toString())
    }

    fun fromTransformDelegateMethod(method: ExecutableElement, transform: Element): Element {
        return if (transform is TransformedField.Transformable) {
            TransformedField(method.simpleName.toString(), method.returnType.asTypeName().javaToKotlinType(), transform)
        } else transform
    }

    fun fromDelegateConstructor(constructor: ExecutableElement, hasContextParameter: Boolean): Element {
        return BuilderElement.Delegate(constructor.enclosingElement.asType().asTypeName(), hasContextParameter)
    }

    companion object {
        private fun getAnnotations(method: ExecutableElement): Pair<List<AnnotationSpec>, Set<ClassName>> {
            val specs = method.annotationMirrors.map { AnnotationSpec.get(it) }.toMutableList()
            specs.remove(AnnotationSpec.builder(NonNull::class).build())
            specs.remove(AnnotationSpec.builder(Nullable::class).build())
            specs.replaceAll { if (it == AnnotationSpec.builder(java.lang.Deprecated::class).build()) AnnotationSpec.builder(Deprecated::class).addMember("message = \"see doc\"").build() else it }
            val markerAnnotations: MutableSet<ClassName> = HashSet()
            val iterator = specs.iterator()
            while (iterator.hasNext()) {
                val spec = iterator.next()
                for (a in Types.MARKER_ANNOTATIONS) {
                    if (a == spec.typeName) {
                        iterator.remove()
                        markerAnnotations.add(a)
                    }
                }
            }
            return Pair.of(specs, markerAnnotations)
        }
    }
}