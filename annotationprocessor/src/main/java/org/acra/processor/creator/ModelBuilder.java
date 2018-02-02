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

package org.acra.processor.creator;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.TypeName;

import org.acra.annotation.BuilderMethod;
import org.acra.annotation.Configuration;
import org.acra.annotation.ConfigurationValue;
import org.acra.annotation.PreBuild;
import org.acra.annotation.Transform;
import org.acra.processor.element.BuilderElement;
import org.acra.processor.element.Element;
import org.acra.processor.element.ElementFactory;
import org.acra.processor.util.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * @author F43nd1r
 * @since 10.01.2018
 */

class ModelBuilder {
    private final TypeElement baseAnnotation;
    private final ElementFactory modelFactory;
    private final Messager messager;
    private final List<Element> elements;
    private final TypeElement baseBuilder;

    ModelBuilder(@NonNull TypeElement baseAnnotation, @NonNull ElementFactory modelFactory, @NonNull TypeElement baseBuilder, @NonNull Messager messager) {
        this.baseAnnotation = baseAnnotation;
        this.modelFactory = modelFactory;
        this.messager = messager;
        this.elements = new ArrayList<>();
        this.baseBuilder = baseBuilder;
    }

    private void handleParameter() {
        elements.add(new BuilderElement.Context());
        elements.add(new BuilderElement.Enabled());
    }

    private void handleAnnotationMethods() {
        for (ExecutableElement method : ElementFilter.methodsIn(baseAnnotation.getEnclosedElements())) {
            elements.add(MoreElements.isAnnotationPresent(method, StringRes.class) ? modelFactory.fromStringResourceAnnotationMethod(method) : modelFactory.fromAnnotationMethod(method));
        }
    }

    private void handleBaseBuilder() {
        if (!MoreTypes.isTypeOf(Object.class, baseBuilder.asType())) {
            final List<ExecutableElement> constructors = ElementFilter.constructorsIn(baseBuilder.getEnclosedElements());
            Optional<ExecutableElement> constructor = constructors.stream().filter(c -> c.getParameters().size() == 0).findAny();
            if (constructor.isPresent()) {
                elements.add(modelFactory.fromDelegateConstructor(constructor.get(), false));
            } else {
                constructor = constructors.stream().filter(c -> c.getParameters().size() == 1 && Types.CONTEXT.equals(TypeName.get(c.getParameters().get(0).asType()))).findAny();
                if (constructor.isPresent()) {
                    elements.add(modelFactory.fromDelegateConstructor(constructor.get(), true));
                } else {
                    final AnnotationMirror mirror = baseAnnotation.getAnnotationMirrors().stream()
                            .filter(m -> MoreTypes.isTypeOf(Configuration.class, m.getAnnotationType()))
                            .findAny().orElseThrow(IllegalArgumentException::new);
                    messager.printMessage(Diagnostic.Kind.ERROR, "Classes used as base builder must have a constructor which takes no arguments, " +
                            "or exactly one argument of type Class", baseAnnotation, mirror, mirror.getElementValues().entrySet().stream()
                            .filter(entry -> entry.getKey().getSimpleName().toString().equals("builderSuperClass")).findAny().map(Map.Entry::getValue).orElse(null));
                    throw new IllegalArgumentException();
                }
            }
            handleBaseBuilderMethods();
        }
    }

    private void handleBaseBuilderMethods() {
        for (ExecutableElement method : ElementFilter.methodsIn(baseBuilder.getEnclosedElements())) {
            if (method.getAnnotation(PreBuild.class) != null) {
                elements.add(modelFactory.fromPreBuildDelegateMethod(method));
            } else if (method.getAnnotation(Transform.class) != null) {
                final String transform = method.getAnnotation(Transform.class).methodName();
                elements.stream().filter(field -> field.getName().equals(transform)).findAny()
                        .ifPresent(element -> elements.set(elements.indexOf(element), modelFactory.fromTransformDelegateMethod(method, element)));
            } else if (method.getAnnotation(ConfigurationValue.class) != null) {
                elements.add(modelFactory.fromConfigDelegateMethod(method));
            } else if (method.getAnnotation(BuilderMethod.class) != null) {
                elements.add(modelFactory.fromBuilderDelegateMethod(method));
            }
        }
    }

    @NonNull
    List<Element> build() {
        handleParameter();
        handleAnnotationMethods();
        handleBaseBuilder();
        return elements;
    }
}
