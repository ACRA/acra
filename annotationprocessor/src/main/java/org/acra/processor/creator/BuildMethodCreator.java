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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.acra.config.ACRAConfigurationException;
import org.acra.config.ClassValidator;
import org.acra.processor.util.Strings;
import org.acra.processor.util.Types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;

/**
 * @author F43nd1r
 * @since 12.06.2017
 */

public class BuildMethodCreator {
    private final MethodSpec.Builder methodBuilder;
    private final Map<String, CodeBlock> anyNonDefault;
    private final ClassName config;
    private final List<CodeBlock> statements;

    BuildMethodCreator(@NonNull ExecutableElement override, @NonNull ClassName config) {
        this.config = config;
        methodBuilder = Types.overriding(override)
                .addAnnotation(Types.NON_NULL)
                .returns(config)
                .beginControlFlow("if ($L)", Strings.FIELD_ENABLED);
        anyNonDefault = new LinkedHashMap<>();
        statements = new ArrayList<>();
    }

    public void addNotUnset(@NonNull String name, @NonNull TypeName type) {
        methodBuilder.beginControlFlow("if ($L == $L)", name, getDefault(type))
                .addStatement("throw new $T(\"$L has to be set\")", ACRAConfigurationException.class, name)
                .endControlFlow();
    }

    public void addNotEmpty(@NonNull String name) {
        methodBuilder.beginControlFlow("if ($L.length == 0)", name)
                .addStatement("throw new $T(\"$L cannot be empty\")", ACRAConfigurationException.class, name)
                .endControlFlow();
    }

    public void addInstantiatable(@NonNull String name) {
        methodBuilder.addStatement("$T.check($L)", ClassValidator.class, name);
    }

    public void addAnyNonDefault(@NonNull String name, @NonNull CodeBlock defaultValue) {
        anyNonDefault.put(name, defaultValue);
    }

    @NonNull
    private String getDefault(@NonNull TypeName type) {
        if (type.isPrimitive()) {
            if (type.equals(TypeName.BOOLEAN)) {
                return "false";
            } else if (type.equals(TypeName.BYTE)) {
                return "0";
            } else if (type.equals(TypeName.SHORT)) {
                return "0";
            } else if (type.equals(TypeName.INT)) {
                return "0";
            } else if (type.equals(TypeName.LONG)) {
                return "0L";
            } else if (type.equals(TypeName.CHAR)) {
                return "\u0000";
            } else if (type.equals(TypeName.FLOAT)) {
                return "0.0f";
            } else if (type.equals(TypeName.DOUBLE)) {
                return "0.0d";
            }
        }
        return "null";
    }

    public void addDelegateCall(@NonNull String methodName) {
        statements.add(CodeBlock.builder().addStatement("$L.$L()", Strings.FIELD_DELEGATE, methodName).build());
    }

    @NonNull
    MethodSpec build() {
        if (anyNonDefault.size() > 0) {
            methodBuilder.beginControlFlow("if ($L)", anyNonDefault.entrySet().stream().map(field -> CodeBlock.builder().add(field.getKey()).add(" == ").add(field.getValue()).build())
                    .reduce((c1, c2) -> CodeBlock.builder().add(c1).add(" && ").add(c2).build()).orElseGet(() -> CodeBlock.of("true")))
                    .addStatement("throw new $T(\"One of $L must not be default\")", ACRAConfigurationException.class,
                            anyNonDefault.keySet().stream().collect(Collectors.joining(", ")))
                    .endControlFlow();
        }
        methodBuilder.endControlFlow();
        for (CodeBlock s : statements) {
            methodBuilder.addCode(s);
        }
        methodBuilder.addStatement("return new $T(this)", config);
        return methodBuilder.build();
    }


}
