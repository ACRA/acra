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

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;

/**
 * @author F43nd1r
 * @since 12.01.2018
 */
public class InitializerVisitor extends SimpleAnnotationValueVisitor9<String, List<Object>> {
    private final TypeName type;

    public InitializerVisitor(TypeName type) {
        this.type = type;
    }

    @Override
    protected String defaultAction(Object o, List<Object> objects) {
        objects.add(o);
        return "$L";
    }

    @Override
    public String visitString(String s, List<Object> objects) {
        objects.add(s);
        return "$S";
    }

    @Override
    public String visitEnumConstant(VariableElement c, List<Object> objects) {
        objects.add(c.asType());
        objects.add(c.getSimpleName());
        return "$T.$L";
    }

    @Override
    public String visitType(TypeMirror t, List<Object> objects) {
        objects.add(t);
        return "$T.class";
    }

    @Override
    public String visitArray(List<? extends AnnotationValue> values, List<Object> objects) {
        ArrayTypeName arrayTypeName = (ArrayTypeName) type;
        if (arrayTypeName.componentType instanceof ParameterizedTypeName) {
            arrayTypeName = ArrayTypeName.of(((ParameterizedTypeName) arrayTypeName.componentType).rawType);
        }
        objects.add(arrayTypeName);
        return "new $T" + values.stream().map(value -> value.accept(this, objects)).collect(Collectors.joining(", ", "{", "}"));
    }
}
