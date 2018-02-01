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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.acra.processor.util.Strings;
import org.acra.processor.util.Types;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

/**
 * @author F43nd1r
 * @since 10.01.2018
 */

class DelegateMethod extends AbstractElement implements BuilderElement {
    private final Collection<ParameterSpec> parameters;
    private final Collection<TypeVariableName> typeVariables;
    private final Collection<Modifier> modifiers;
    private final String javadoc;

    DelegateMethod(String name, TypeName type, Collection<AnnotationSpec> annotations, Collection<ParameterSpec> parameters,
                   Collection<TypeVariableName> typeVariables, Collection<Modifier> modifiers, String javadoc) {
        super(name, type, annotations);
        this.parameters = parameters;
        this.typeVariables = typeVariables;
        this.modifiers = modifiers;
        this.javadoc = javadoc;
    }

    @Override
    public void addToBuilder(TypeSpec.Builder builder, ClassName builderName, CodeBlock.Builder constructorAlways, CodeBlock.Builder constructorWhenAnnotationPresent) {
        final MethodSpec.Builder method = MethodSpec.methodBuilder(getName())
                .addModifiers(modifiers)
                .addParameters(parameters)
                .addTypeVariables(typeVariables)
                .addAnnotations(getAnnotations());
        if (javadoc != null) {
            method.addJavadoc(javadoc.replaceAll("(\n|^) ", "$1"));
        }
        if (getType().equals(TypeName.VOID)) {
            method.addStatement("$L.$L($L)", Strings.FIELD_DELEGATE, getName(), parameters.stream().map(p -> p.name).collect(Collectors.joining(", ")))
                    .addStatement("return this")
                    .returns(builderName)
                    .addAnnotation(Types.NON_NULL)
                    .addJavadoc("@return this instance\n");
        } else {
            method.addStatement("return $L.$L($L)", Strings.FIELD_DELEGATE, getName(), parameters.stream().map(p -> p.name).collect(Collectors.joining(", ")))
                    .returns(getType());
        }
        builder.addMethod(method.build());
    }

    /**
     * @author F43nd1r
     * @since 11.01.2018
     */

    public static class Config extends DelegateMethod implements org.acra.processor.element.ConfigElement {
        public Config(String name, TypeName type, Collection<AnnotationSpec> annotations, Collection<ParameterSpec> parameters,
                      Collection<TypeVariableName> typeVariables, Collection<Modifier> modifiers, String javadoc) {
            super(name, type, annotations, parameters, typeVariables, modifiers, javadoc);
        }
    }
}
