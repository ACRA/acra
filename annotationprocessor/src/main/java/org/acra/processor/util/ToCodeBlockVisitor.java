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

package org.acra.processor.util;

import android.support.annotation.NonNull;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

/**
 * @author F43nd1r
 * @since 12.01.2018
 */
public class ToCodeBlockVisitor extends SimpleAnnotationValueVisitor8<CodeBlock, Void> {
    private final TypeName type;

    public ToCodeBlockVisitor(TypeName type) {
        this.type = type;
    }

    @NonNull
    @Override
    protected CodeBlock defaultAction(Object o, Void v) {
        return CodeBlock.of("$L", o);
    }

    @NonNull
    @Override
    public CodeBlock visitString(String s, Void v) {
        return CodeBlock.of("$S", s);
    }

    @NonNull
    @Override
    public CodeBlock visitEnumConstant(@NonNull VariableElement c, Void v) {
        return CodeBlock.of("$T.$L", c.asType(), c.getSimpleName());
    }

    @NonNull
    @Override
    public CodeBlock visitType(TypeMirror t, Void v) {
        return CodeBlock.of("$T.class", t);
    }

    @NonNull
    @Override
    public CodeBlock visitArray(@NonNull List<? extends AnnotationValue> values, Void v) {
        ArrayTypeName arrayTypeName = (ArrayTypeName) type;
        if (arrayTypeName.componentType instanceof ParameterizedTypeName) {
            arrayTypeName = ArrayTypeName.of(((ParameterizedTypeName) arrayTypeName.componentType).rawType);
        }
        return CodeBlock.of("new $T{$L}", arrayTypeName, values.stream().map(value -> value.accept(this, null))
                .reduce((c1, c2) -> CodeBlock.builder().add(c1).add(", ").add(c2).build()).orElseGet(() -> CodeBlock.builder().build()));
    }
}
