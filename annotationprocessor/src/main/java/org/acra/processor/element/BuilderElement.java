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
import com.squareup.javapoet.*;
import org.acra.processor.util.Strings;
import org.acra.processor.util.Types;
import org.apache.commons.text.WordUtils;

import javax.lang.model.element.Modifier;
import java.util.Collections;

/**
 * @author F43nd1r
 * @since 11.01.2018
 */

public interface BuilderElement extends Element {

    default void addToBuilder(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName, @NonNull CodeBlock.Builder constructorAlways,
                              @NonNull CodeBlock.Builder constructorWhenAnnotationPresent) {
        final FieldSpec.Builder field = FieldSpec.builder(getType(), getName(), Modifier.PRIVATE).addAnnotations(getAnnotations());
        configureField(field);
        builder.addField(field.build());
    }

    default void configureField(@NonNull FieldSpec.Builder builder) {
    }

    default void addGetter(@NonNull TypeSpec.Builder builder) {
        final MethodSpec.Builder method = MethodSpec.methodBuilder(getName())
                .addAnnotations(getAnnotations())
                .returns(getType());
        configureGetter(method);
        builder.addMethod(method.build());
    }

    default void configureGetter(@NonNull MethodSpec.Builder builder) {
        builder.addStatement("return $L", getName());
    }

    default void addSetter(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName) {
        final MethodSpec.Builder method = createSetter(builderName, getName(), getType(), getAnnotations(), "this.$1L = $1L", getName());
        configureSetter(method);
        builder.addMethod(method.build());
    }

    default void configureSetter(@NonNull MethodSpec.Builder builder) {
    }

    static MethodSpec.Builder createSetter(@NonNull ClassName builderName, @NonNull String parameterName, @NonNull TypeName parameterType, @NonNull Iterable<AnnotationSpec> annotations,
                                           @NonNull String statement, @NonNull Object... statementArgs) {
        return MethodSpec.methodBuilder(Strings.PREFIX_SETTER + WordUtils.capitalize(parameterName))
                .addParameter(ParameterSpec.builder(parameterType, parameterName).addAnnotations(annotations).build())
                .varargs(parameterType instanceof ArrayTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Types.NON_NULL)
                .returns(builderName)
                .addStatement(statement, statementArgs)
                .addStatement("return this");
    }

    interface Final extends BuilderElement {
        default void configureField(@NonNull FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.FINAL);
        }
    }

    class Context extends AbstractElement implements Final {
        public Context() {
            super(Strings.FIELD_CONTEXT, Types.CONTEXT, Collections.singleton(Types.NON_NULL));
        }

        @Override
        public void addToBuilder(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName, @NonNull CodeBlock.Builder constructorAlways,
                                 @NonNull CodeBlock.Builder constructorWhenAnnotationPresent) {
            Final.super.addToBuilder(builder, builderName, constructorAlways, constructorWhenAnnotationPresent);
            constructorAlways.addStatement("$L = $L", getName(), Strings.PARAM_0);
        }
    }

    class Delegate extends AbstractElement implements Final {
        private final boolean hasContextParameter;

        Delegate(@NonNull TypeName type, boolean hasContextParameter) {
            super(Strings.FIELD_DELEGATE, type, Collections.singleton(Types.NON_NULL));
            this.hasContextParameter = hasContextParameter;
        }

        @Override
        public void addToBuilder(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName, @NonNull CodeBlock.Builder constructorAlways,
                                 @NonNull CodeBlock.Builder constructorWhenAnnotationPresent) {
            Final.super.addToBuilder(builder, builderName, constructorAlways, constructorWhenAnnotationPresent);
            if (hasContextParameter) {
                constructorAlways.addStatement("$L = new $T($L)", getName(), getType(), Strings.PARAM_0);
            } else {
                constructorAlways.addStatement("$L = new $T()", getName(), getType());
            }
        }
    }

    class Enabled extends AbstractElement implements BuilderElement, ConfigElement {
        public Enabled() {
            super(Strings.FIELD_ENABLED, TypeName.BOOLEAN, Collections.emptyList());
        }

        @Override
        public void addToBuilder(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName, @NonNull CodeBlock.Builder constructorAlways,
                                 @NonNull CodeBlock.Builder constructorWhenAnnotationPresent) {
            BuilderElement.super.addToBuilder(builder, builderName, constructorAlways, constructorWhenAnnotationPresent);
            addSetter(builder, builderName);
            addGetter(builder);
            constructorAlways.addStatement("$L = $L != null", getName(), Strings.VAR_ANNOTATION);
        }
    }
}
