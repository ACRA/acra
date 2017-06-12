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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import org.acra.ModelUtils;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

/**
 * @author F43nd1r
 * @since 12.06.2017
 */

public class FieldSetter extends FieldMethod {
    private static final String PREFIX_SETTER = "set";
    private final ClassName builderName;

    public FieldSetter(Field field, ClassName builderName) {
        super(field);
        this.builderName = builderName;
    }

    @Override
    public boolean shouldPropagate() {
        return false;
    }

    @Override
    public void writeTo(TypeSpec.Builder builder, ModelUtils utils) {
        builder.addMethod(MethodSpec.methodBuilder(getName())
                .returns(builderName)
                .addParameter(ParameterSpec.builder(getField().getType().getName(), getField().getName()).addAnnotations(getAnnotations()).build())
                .varargs(getField().getType().getMirror().getKind() == TypeKind.ARRAY)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(NonNull.class)
                .addStatement("this.$1L = $1L", getField().getName())
                .addStatement("return this")
                .addJavadoc(getField().getJavadoc().replaceAll("(\n|^) ", "$1").replaceAll("@return ((.|\n)*)$", "@param " + getField().getName() + " $1@return this instance\n"))
                .build());
    }

    @Override
    public String getName() {
        return PREFIX_SETTER + Character.toUpperCase(getField().getName().charAt(0)) + getField().getName().substring(1);
    }

    @Override
    public List<AnnotationSpec> getAnnotations() {
        return getField().getAnnotations();
    }

    @Override
    public ClassName getReturnType() {
        return builderName;
    }
}
