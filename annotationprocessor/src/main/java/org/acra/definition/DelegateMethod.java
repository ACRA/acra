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
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.acra.ModelUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

/**
 * @author F43nd1r
 * @since 12.06.2017
 */

public class DelegateMethod implements Method {
    private final String name;
    private final String delegate;
    private final List<AnnotationSpec> annotations;
    private final Type returnType;
    private final List<ParameterSpec> parameters;
    private final Collection<Modifier> modifiers;
    private final List<TypeVariableName> typeVariables;
    private final String javadoc;
    private final ClassName builderName;

    public static DelegateMethod from(ExecutableElement method, String field, ClassName builderName, ModelUtils utils) {
        return new DelegateMethod(method.getSimpleName().toString(), field, utils.getType(method.getReturnType()), utils.getAnnotations(method),
                utils.getParameters(method), utils.getTypeParameters(method), method.getModifiers(), utils.getJavadoc(method), builderName);
    }

    private DelegateMethod(String name, String delegate, Type returnType, List<AnnotationSpec> annotations, List<ParameterSpec> parameters,
                           List<TypeVariableName> typeVariables, Collection<Modifier> modifiers, String javadoc, ClassName builderName) {
        this.name = name;
        this.delegate = delegate;
        this.annotations = annotations;
        this.returnType = returnType;
        this.parameters = parameters;
        this.modifiers = modifiers;
        this.typeVariables = typeVariables;
        this.javadoc = javadoc;
        this.builderName = builderName;
    }

    @Override
    public boolean shouldPropagate() {
        return parameters.size() == 0;
    }

    @Override
    public void writeTo(TypeSpec.Builder builder, ModelUtils utils) {
        final MethodSpec.Builder method = MethodSpec.methodBuilder(name)
                .addModifiers(modifiers)
                .addParameters(parameters)
                .addTypeVariables(typeVariables)
                .addAnnotations(annotations);
        if (javadoc != null) {
            method.addJavadoc(javadoc.replaceAll("(\n|^) ", "$1"));
        }
        if (returnType.getName().equals(TypeName.VOID)) {
            method.addStatement("$L.$L($L)", delegate, name, parameters.stream().map(p -> p.name).collect(Collectors.joining(", ")))
                    .addStatement("return this")
                    .returns(builderName)
                    .addAnnotation(NonNull.class)
                    .addJavadoc("@return this instance\n");
        } else {
            method.addStatement("return $L.$L($L)", delegate, name, parameters.stream().map(p -> p.name).collect(Collectors.joining(", ")))
                    .returns(returnType.getName());
        }
        builder.addMethod(method.build());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<AnnotationSpec> getAnnotations() {
        return annotations;
    }

    @Override
    public TypeName getReturnType() {
        return returnType.getName();
    }
}
