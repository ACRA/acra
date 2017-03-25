/*
 * Copyright (c) 2017
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

package org.acra;

import com.squareup.javapoet.AnnotationSpec;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * The minimal Definition needed to create a getter
 *
 * @author F43nd1r
 * @since 18.03.2017
 */
class MethodDefinition {
    private final String name;
    private final TypeMirror type;
    private final List<AnnotationSpec> annotations;

    static MethodDefinition from(ExecutableElement method) {
        return new MethodDefinition(method.getSimpleName().toString(), method.getReturnType(), ModelUtils.getAnnotations(method));
    }

    MethodDefinition(String name, TypeMirror type, List<AnnotationSpec> annotations) {
        this.name = name;
        this.type = type;
        this.annotations = annotations;
    }

    String getName() {
        return name;
    }

    TypeMirror getType() {
        return type;
    }

    List<AnnotationSpec> getAnnotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MethodDefinition that = (MethodDefinition) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return annotations.stream().map(AnnotationSpec::toString).collect(Collectors.joining(" ")) + " " + type + " " + name + "()";
    }
}
