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
import org.acra.processor.creator.BuildMethodCreator;
import org.acra.processor.util.ToCodeBlockVisitor;
import org.acra.processor.util.IsValidResourceVisitor;
import org.acra.processor.util.Strings;
import org.acra.processor.util.Types;
import org.apache.commons.text.WordUtils;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author F43nd1r
 * @since 12.01.2018
 */

abstract class AnnotationField extends AbstractElement implements TransformedField.Transformable {
    private final Collection<ClassName> markers;
    private final String javadoc;
    private final AnnotationValue defaultValue;

    AnnotationField(@NonNull String name, @NonNull TypeName type, @NonNull Collection<AnnotationSpec> annotations, @Nullable String javadoc, @NonNull Collection<ClassName> markers, AnnotationValue defaultValue) {
        super(name, type, annotations);
        this.javadoc = javadoc;
        this.markers = markers;
        this.defaultValue = defaultValue;
    }

    boolean hasMarker(@NonNull ClassName marker) {
        return markers.contains(marker);
    }

    @Override
    public final void addToBuilder(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName, @NonNull CodeBlock.Builder constructorAlways, @NonNull CodeBlock.Builder constructorWhenAnnotationPresent, CodeBlock.Builder constructorWhenAnnotationMissing) {
        addWithoutGetter(builder, builderName, constructorAlways, constructorWhenAnnotationPresent, constructorWhenAnnotationMissing);
        addGetter(builder);
    }

    @Override
    public final void addWithoutGetter(@NonNull TypeSpec.Builder builder, ClassName builderName, CodeBlock.Builder constructorAlways, CodeBlock.Builder constructorWhenAnnotationPresent, CodeBlock.Builder constructorWhenAnnotationMissing) {
        TransformedField.Transformable.super.addToBuilder(builder, builderName, constructorAlways, constructorWhenAnnotationPresent, constructorWhenAnnotationMissing);
        addSetter(builder, builderName);
        addInitializer(constructorWhenAnnotationPresent, constructorWhenAnnotationMissing);
    }

    protected abstract void addInitializer(CodeBlock.Builder constructorWhenAnnotationPresent, CodeBlock.Builder constructorWhenAnnotationMissing);

    AnnotationValue getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void configureSetter(@NonNull MethodSpec.Builder builder) {
        if (javadoc != null) {
            final String name = builder.build().parameters.get(0).name;
            builder.addJavadoc(javadoc.replaceAll("(\n|^) ", "$1").replaceAll("@return ((.|\n)*)$", "@param " + name + " $1@return this instance\n"));
        }
    }

    static class Normal extends AnnotationField {

        Normal(String name, TypeName type, Collection<AnnotationSpec> annotations, Collection<ClassName> markers, AnnotationValue defaultValue, String javadoc) {
            super(name, type, annotations, javadoc, markers, defaultValue);
        }

        @Override
        public void addInitializer(@NonNull CodeBlock.Builder constructorWhenAnnotationPresent, CodeBlock.Builder constructorWhenAnnotationMissing) {
            constructorWhenAnnotationPresent.addStatement("$1L = $2L.$1L()", getName(), Strings.VAR_ANNOTATION);
            if (getDefaultValue() != null) {
                constructorWhenAnnotationMissing.addStatement("$L = $L", getName(), getDefaultValue().accept(new ToCodeBlockVisitor(getType()), null));
            }
        }

        @Override
        public void addToBuildMethod(@NonNull BuildMethodCreator method) {
            if (getDefaultValue() == null) {
                method.addNotUnset(getName(), getType());
            }
            if (hasMarker(Types.NON_EMPTY)) {
                method.addNotEmpty(getName());
            }
            if (hasMarker(Types.INSTANTIATABLE)) {
                method.addInstantiatable(getName());
            }
            if (hasMarker(Types.ANY_NON_DEFAULT)) {
                method.addAnyNonDefault(getName(), getDefaultValue().accept(new ToCodeBlockVisitor(getType()), null));
            }
        }

    }

    static class StringResource extends AnnotationField {
        private final String originalName;
        private final boolean hasDefault;

        StringResource(String name, Collection<AnnotationSpec> annotations, Collection<ClassName> markers,
                       AnnotationValue defaultValue, String javadoc) {
            super((name.startsWith(Strings.PREFIX_RES)) ? WordUtils.uncapitalize(name.substring(Strings.PREFIX_RES.length())) : name, Types.STRING, annotations, javadoc, markers, defaultValue);
            this.originalName = name;
            this.hasDefault = defaultValue != null && getDefaultValue().accept(new IsValidResourceVisitor(), null);
            getAnnotations().remove(Types.STRING_RES);
            getAnnotations().add(hasDefault ? Types.NON_NULL : Types.NULLABLE);
        }

        @Override
        public void addInitializer(@NonNull CodeBlock.Builder constructorWhenAnnotationPresent, CodeBlock.Builder constructorWhenAnnotationMissing) {
            constructorWhenAnnotationPresent.beginControlFlow("if ($L.$L() != 0)", Strings.VAR_ANNOTATION, originalName)
                    .addStatement("$L = $L.getString($L.$L())", getName(), Strings.FIELD_CONTEXT, Strings.VAR_ANNOTATION, originalName)
                    .endControlFlow();
            if (hasDefault) {
                constructorWhenAnnotationMissing.addStatement("$L = $L.getString($L)", getName(), Strings.FIELD_CONTEXT, getDefaultValue());
            }

        }

        @Override
        public void addSetter(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName) {
            super.addSetter(builder, builderName);
            final MethodSpec.Builder setter = baseResSetter(builderName)
                    .addStatement("this.$L = $L.getString($L)", getName(), Strings.FIELD_CONTEXT, Strings.PREFIX_RES + WordUtils.capitalize(getName()))
                    .addStatement("return this");
            configureSetter(setter);
            builder.addMethod(setter.build());
        }

        private MethodSpec.Builder baseResSetter(ClassName builderName) {
            final String parameterName = Strings.PREFIX_RES + WordUtils.capitalize(getName());
            final List<AnnotationSpec> annotations = new ArrayList<>(getAnnotations());
            annotations.removeIf(Arrays.asList(Types.NULLABLE, Types.NON_NULL)::contains);
            annotations.add(Types.STRING_RES);
            return MethodSpec.methodBuilder(Strings.PREFIX_SETTER + WordUtils.capitalize(parameterName))
                    .addParameter(ParameterSpec.builder(TypeName.INT, parameterName).addAnnotations(annotations).build())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Types.NON_NULL)
                    .returns(builderName);
        }

        @Override
        public void addToBuilderInterface(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName) {
            super.addToBuilderInterface(builder, builderName);
            final MethodSpec.Builder setter = baseResSetter(builderName).addModifiers(Modifier.ABSTRACT);
            configureSetter(setter);
            builder.addMethod(setter.build());
        }

        @Override
        public void addToBuildMethod(@NonNull BuildMethodCreator method) {
            if (getDefaultValue() == null) {
                method.addNotUnset(getName(), getType());
            }
            if (hasMarker(Types.ANY_NON_DEFAULT)) {
                method.addAnyNonDefault(getName(), CodeBlock.of("null"));
            }
        }
    }

}
