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

package org.acra.creator;

import android.support.annotation.NonNull;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import org.acra.annotation.AnyNonDefault;
import org.acra.annotation.Instantiatable;
import org.acra.annotation.NonEmpty;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ClassValidator;
import org.acra.definition.Field;
import org.acra.definition.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;

/**
 * @author F43nd1r
 * @since 12.06.2017
 */

class BuildMethodCreator {
    private final MethodSpec.Builder methodBuilder;
    private final List<ExecutableElement> anyNonDefault;
    private final ClassName config;
    private final List<CodeBlock> statements;

    BuildMethodCreator(ExecutableElement override, ClassName config) {
        this.config = config;
        methodBuilder = MethodSpec.overriding(override)
                .addAnnotation(NonNull.class)
                .returns(config)
                .beginControlFlow("if ($L)", BuilderCreator.ENABLED);
        anyNonDefault = new ArrayList<>();
        statements = new ArrayList<>();
    }

    void addValidation(Field field, ExecutableElement method) {
        if (!field.hasDefault()) {
            methodBuilder.beginControlFlow("if ($L == $L)", field.getName(), getDefault(field.getType()))
                    .addStatement("throw new $T(\"$L has to be set\")", ACRAConfigurationException.class, field.getName())
                    .endControlFlow();
        }
        if (method.getAnnotation(NonEmpty.class) != null) {
            methodBuilder.beginControlFlow("if ($L().length == 0)", field.getName())
                    .addStatement("throw new $T(\"$L cannot be empty\")", ACRAConfigurationException.class, field.getName())
                    .endControlFlow();
        }
        if (method.getAnnotation(Instantiatable.class) != null) {
            methodBuilder.addStatement("$T.check($L())", ClassValidator.class, field.getName());
        }
        if (method.getAnnotation(AnyNonDefault.class) != null) {
            anyNonDefault.add(method);
        }
    }

    private String getDefault(Type type) {
        switch (type.getMirror().getKind()) {
            case BOOLEAN:
                return "false";
            case BYTE:
                return "0";
            case SHORT:
                return "0";
            case INT:
                return "0";
            case LONG:
                return "0L";
            case CHAR:
                return "\u0000";
            case FLOAT:
                return "0.0f";
            case DOUBLE:
                return "0.0d";
            default:
                return "null";
        }
    }

    void addMethodCall(String delegate, String methodName) {
        statements.add(CodeBlock.builder().addStatement("$L.$L()", delegate, methodName).build());
    }

    MethodSpec build() {
        if (anyNonDefault.size() > 0) {
            methodBuilder.beginControlFlow("if ($L)", anyNonDefault.stream().map(m -> m.getSimpleName().toString() + "() == " + m.getDefaultValue()).collect(Collectors.joining(" && ")))
                    .addStatement("throw new $T(\"One of $L must not be default\")", ACRAConfigurationException.class,
                            anyNonDefault.stream().map(m -> m.getSimpleName().toString()).collect(Collectors.joining(", ")))
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
