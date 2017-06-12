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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import org.acra.annotation.AnyNonDefault;
import org.acra.annotation.Instantiatable;
import org.acra.annotation.NonEmpty;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ClassValidator;
import org.acra.definition.Field;

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

    BuildMethodCreator(ExecutableElement override, ClassName config) {
        this.config = config;
        methodBuilder = MethodSpec.overriding(override)
                .returns(config);
        anyNonDefault = new ArrayList<>();
    }

    void addValidation(Field field, ExecutableElement method){
        if (!field.hasDefault()) {
            methodBuilder.beginControlFlow("if ($L == null)", field.getName())
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

    void addMethodCall(String delegate, String methodName){
        methodBuilder.addStatement("$L.$L()", delegate, methodName);
    }

    MethodSpec build(){
        if (anyNonDefault.size() > 0) {
            methodBuilder.beginControlFlow("if ($L)", anyNonDefault.stream().map(m -> m.getSimpleName().toString() + "() == " + m.getDefaultValue()).collect(Collectors.joining(" && ")))
                    .addStatement("throw new $T(\"One of $L must not be default\")", ACRAConfigurationException.class,
                            anyNonDefault.stream().map(m -> m.getSimpleName().toString()).collect(Collectors.joining(", ")))
                    .endControlFlow();
        }
        methodBuilder.addStatement("return new $T(this)", config);
        return methodBuilder.build();
    }


}
