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
package org.acra.processor.creator

import androidx.annotation.StringRes
import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.squareup.kotlinpoet.asTypeName
import org.acra.annotation.BuilderMethod
import org.acra.annotation.Configuration
import org.acra.annotation.ConfigurationValue
import org.acra.annotation.PreBuild
import org.acra.annotation.Transform
import org.acra.processor.element.BuilderElement
import org.acra.processor.element.Element
import org.acra.processor.element.ElementFactory
import org.acra.processor.util.Types
import java.util.*
import javax.annotation.processing.Messager
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic

/**
 * @author F43nd1r
 * @since 10.01.2018
 */
internal class ModelBuilder(private val baseAnnotation: TypeElement, private val modelFactory: ElementFactory, baseBuilder: TypeElement, private val messager: Messager) {
    private val elements: MutableList<Element>
    private val baseBuilder: TypeElement
    private fun handleParameter() {
        elements.add(BuilderElement.Context())
        elements.add(BuilderElement.Enabled())
    }

    private fun handleAnnotationMethods() {
        for (method in ElementFilter.methodsIn(baseAnnotation.enclosedElements)) {
            elements.add(when {
                MoreElements.isAnnotationPresent(method, StringRes::class.java) -> modelFactory.fromStringResourceAnnotationMethod(method)
                method.returnType.toString().endsWith("[]") -> modelFactory.fromArrayAnnotationMethod(method)
                method.returnType.toString().contains("Class") -> modelFactory.fromClassAnnotationMethod(method)
                else -> modelFactory.fromAnnotationMethod(method)
            })
        }
    }

    private fun handleBaseBuilder() {
        if (!MoreTypes.isTypeOf(Any::class.java, baseBuilder.asType())) {
            val constructors = ElementFilter.constructorsIn(baseBuilder.enclosedElements)
            var constructor = constructors.stream().filter { c: ExecutableElement -> c.parameters.size == 0 }.findAny()
            if (constructor.isPresent) {
                elements.add(modelFactory.fromDelegateConstructor(constructor.get(), false))
            } else {
                constructor = constructors.stream().filter { c: ExecutableElement -> c.parameters.size == 1 && Types.CONTEXT == c.parameters[0].asType().asTypeName() }.findAny()
                if (constructor.isPresent) {
                    elements.add(modelFactory.fromDelegateConstructor(constructor.get(), true))
                } else {
                    val mirror = baseAnnotation.annotationMirrors.stream()
                            .filter { m: AnnotationMirror -> MoreTypes.isTypeOf(Configuration::class.java, m.annotationType) }
                            .findAny().orElseThrow { IllegalArgumentException() }
                    messager.printMessage(Diagnostic.Kind.ERROR, "Classes used as base builder must have a constructor which takes no arguments, " +
                            "or exactly one argument of type Class", baseAnnotation, mirror, mirror.elementValues.entries.firstOrNull { it.key.simpleName.toString() == "builderSuperClass" }?.value)
                    throw java.lang.IllegalArgumentException()
                }
            }
            handleBaseBuilderMethods()
        }
    }

    private fun handleBaseBuilderMethods() {
        for (method in ElementFilter.methodsIn(baseBuilder.enclosedElements)) {
            when {
                method.getAnnotation(PreBuild::class.java) != null -> {
                    elements.add(modelFactory.fromPreBuildDelegateMethod(method))
                }
                method.getAnnotation(Transform::class.java) != null -> {
                    val transform: String = method.getAnnotation(Transform::class.java).methodName
                    elements.stream().filter { field: Element -> field.name == transform }.findAny()
                            .ifPresent { element: Element -> elements[elements.indexOf(element)] = modelFactory.fromTransformDelegateMethod(method, element) }
                }
                method.getAnnotation(ConfigurationValue::class.java) != null -> {
                    elements.add(modelFactory.fromConfigDelegateMethod(method))
                }
                method.getAnnotation(BuilderMethod::class.java) != null -> {
                    elements.add(modelFactory.fromBuilderDelegateMethod(method))
                }
            }
        }
    }

    fun build(): List<Element> {
        handleParameter()
        handleAnnotationMethods()
        handleBaseBuilder()
        return elements
    }

    init {
        elements = ArrayList()
        this.baseBuilder = baseBuilder
    }
}