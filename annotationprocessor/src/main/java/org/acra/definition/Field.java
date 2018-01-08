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

package org.acra.definition;

import android.support.annotation.NonNull;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.acra.ModelUtils;

import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

/**
 * The minimal Definition needed to create a getter
 *
 * @author F43nd1r
 * @since 18.03.2017
 */
public class Field {
    private final String name;
    private final Type type;
    private final List<AnnotationSpec> annotations;
    private final AnnotationValue defaultValue;
    private final String javadoc;

    public static Field from(ExecutableElement method, ModelUtils utils) {
        return new Field(method.getSimpleName().toString(), utils.getType(method.getReturnType()), utils.getAnnotations(method), method.getDefaultValue(), utils.getJavadoc(method));
    }

    public Field(String name, Type type, List<AnnotationSpec> annotations, AnnotationValue defaultValue, String javadoc) {
        this.name = name;
        this.type = type;
        this.annotations = annotations;
        this.defaultValue = defaultValue;
        this.javadoc = javadoc;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    List<AnnotationSpec> getAnnotations() {
        return annotations;
    }

    public boolean hasDefault() {
        return defaultValue != null;
    }

    public void addTo(TypeSpec.Builder builder, ModelUtils utils) {
        annotations.removeIf(a -> a.type.equals(TypeName.get(NonNull.class)));
        final FieldSpec.Builder field = FieldSpec.builder(type.getName(), name, Modifier.PRIVATE)
                .addAnnotations(annotations);
        if(defaultValue != null){
            if (type.getMirror().getKind() == TypeKind.ARRAY) {
                field.initializer("new $T$L", utils.erasure(type.getMirror()), defaultValue);
            }else {
                field.initializer("$L", defaultValue);
            }
        }
        builder.addField(field.build());
    }

    public String getJavadoc() {
        return javadoc;
    }
}
