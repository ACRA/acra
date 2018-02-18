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

package org.acra.processor.element;

import android.support.annotation.NonNull;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author F43nd1r
 * @since 10.01.2018
 */

public class ElementFactory {

    private final Elements elements;


    public ElementFactory(@NonNull Elements elements) {
        this.elements = elements;
    }

    @NonNull
    private static Pair<List<AnnotationSpec>, Set<ClassName>> getAnnotations(@NonNull ExecutableElement method) {
        final List<AnnotationSpec> specs = method.getAnnotationMirrors().stream().map(AnnotationSpec::get).collect(Collectors.toList());
        final Set<ClassName> markerAnnotations = new HashSet<>();
        for (final Iterator<AnnotationSpec> iterator = specs.iterator(); iterator.hasNext(); ) {
            final AnnotationSpec spec = iterator.next();
            for (ClassName a : org.acra.processor.util.Types.MARKER_ANNOTATIONS) {
                if (a.equals(spec.type)) {
                    iterator.remove();
                    markerAnnotations.add(a);
                }
            }
        }
        return Pair.of(specs, markerAnnotations);
    }

    @NonNull
    public Element fromAnnotationMethod(@NonNull ExecutableElement method) {
        final Pair<List<AnnotationSpec>, Set<ClassName>> annotations = getAnnotations(method);
        return new AnnotationField.Normal(method.getSimpleName().toString(), TypeName.get(method.getReturnType()), annotations.getLeft(), annotations.getRight(), method.getDefaultValue(),
                elements.getDocComment(method));
    }

    @NonNull
    public Element fromStringResourceAnnotationMethod(@NonNull ExecutableElement method) {
        final Pair<List<AnnotationSpec>, Set<ClassName>> annotations = getAnnotations(method);
        return new AnnotationField.StringResource(method.getSimpleName().toString(), annotations.getLeft(), annotations.getRight(),
                method.getDefaultValue(), elements.getDocComment(method));
    }

    @NonNull
    public Element fromBuilderDelegateMethod(@NonNull ExecutableElement method) {
        return new DelegateMethod(method.getSimpleName().toString(), TypeName.get(method.getReturnType()), getAnnotations(method).getLeft(),
                method.getParameters().stream().map(p -> ParameterSpec.builder(TypeName.get(p.asType()), p.getSimpleName().toString()).build()).collect(Collectors.toList()),
                method.getTypeParameters().stream().map(TypeVariableName::get).collect(Collectors.toList()), method.getModifiers(), elements.getDocComment(method));
    }

    @NonNull
    public Element fromConfigDelegateMethod(@NonNull ExecutableElement method) {
        return new DelegateMethod.Config(method.getSimpleName().toString(), TypeName.get(method.getReturnType()), getAnnotations(method).getLeft(),
                method.getParameters().stream().map(p -> ParameterSpec.builder(TypeName.get(p.asType()), p.getSimpleName().toString()).build()).collect(Collectors.toList()),
                method.getTypeParameters().stream().map(TypeVariableName::get).collect(Collectors.toList()), method.getModifiers(), elements.getDocComment(method));
    }

    @NonNull
    public Element fromPreBuildDelegateMethod(@NonNull ExecutableElement method) {
        return new PreBuildMethod(method.getSimpleName().toString());
    }

    @NonNull
    public Element fromTransformDelegateMethod(@NonNull ExecutableElement method, Element transform) {
        if (transform instanceof TransformedField.Transformable) {
            return new TransformedField(method.getSimpleName().toString(), TypeName.get(method.getReturnType()), (TransformedField.Transformable) transform);
        }
        return transform;
    }

    @NonNull
    public Element fromDelegateConstructor(@NonNull ExecutableElement constructor, boolean hasContextParameter) {
        return new BuilderElement.Delegate(TypeName.get(constructor.getEnclosingElement().asType()), hasContextParameter);
    }
}
