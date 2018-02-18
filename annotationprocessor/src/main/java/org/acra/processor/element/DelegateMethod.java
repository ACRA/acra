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
import android.support.annotation.Nullable;
import com.squareup.javapoet.*;
import org.acra.processor.util.Strings;
import org.acra.processor.util.Types;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author F43nd1r
 * @since 10.01.2018
 */

class DelegateMethod extends AbstractElement implements BuilderElement.Interface {
    private final Collection<ParameterSpec> parameters;
    private final Collection<TypeVariableName> typeVariables;
    private final Collection<Modifier> modifiers;
    private final String javadoc;

    DelegateMethod(@NonNull String name, @NonNull TypeName type, @NonNull Collection<AnnotationSpec> annotations, @NonNull Collection<ParameterSpec> parameters,
                   @NonNull Collection<TypeVariableName> typeVariables, @NonNull Collection<Modifier> modifiers, @Nullable String javadoc) {
        super(name, type, annotations);
        this.parameters = parameters;
        this.typeVariables = typeVariables;
        this.modifiers = modifiers;
        this.javadoc = javadoc;
    }

    @Override
    public void addToBuilder(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName, @NonNull CodeBlock.Builder constructorAlways, @NonNull CodeBlock.Builder constructorWhenAnnotationPresent, CodeBlock.Builder constructorWhenAnnotationMissing) {
        final MethodSpec.Builder method = baseMethod(builderName);
        if (getType().equals(TypeName.VOID)) {
            method.addStatement("$L.$L($L)", Strings.FIELD_DELEGATE, getName(), parameters.stream().map(p -> p.name).collect(Collectors.joining(", ")))
                    .addStatement("return this");
        } else {
            method.addStatement("return $L.$L($L)", Strings.FIELD_DELEGATE, getName(), parameters.stream().map(p -> p.name).collect(Collectors.joining(", ")));
        }
        builder.addMethod(method.build());
    }

    private MethodSpec.Builder baseMethod(@NonNull ClassName builderName) {
        final MethodSpec.Builder method = MethodSpec.methodBuilder(getName())
                .addModifiers(modifiers)
                .addParameters(parameters)
                .addTypeVariables(typeVariables)
                .addAnnotations(getAnnotations());
        if (javadoc != null) {
            method.addJavadoc(javadoc.replaceAll("(\n|^) ", "$1"));
        }
        if (getType().equals(TypeName.VOID)) {
            method.returns(builderName)
                    .addAnnotation(Types.NON_NULL)
                    .addJavadoc("@return this instance\n");
        } else {
            method.returns(getType());
        }
        return method;
    }

    @Override
    public void addToBuilderInterface(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName) {
        if (modifiers.contains(Modifier.PUBLIC)) {
            builder.addMethod(baseMethod(builderName).addModifiers(Modifier.ABSTRACT).build());
        }
    }

    static class Config extends DelegateMethod implements org.acra.processor.element.ConfigElement {
        Config(@NonNull String name, @NonNull TypeName type, @NonNull Collection<AnnotationSpec> annotations, @NonNull Collection<ParameterSpec> parameters,
               @NonNull Collection<TypeVariableName> typeVariables, @NonNull Collection<Modifier> modifiers, @NonNull String javadoc) {
            super(name, type, annotations, parameters, typeVariables, modifiers, javadoc);
        }
    }
}
