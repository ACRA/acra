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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.acra.processor.creator.BuildMethodCreator;
import org.acra.processor.util.InitializerVisitor;
import org.acra.processor.util.Strings;
import org.acra.processor.util.Types;
import org.apache.commons.text.WordUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.AnnotationValue;

/**
 * @author F43nd1r
 * @since 12.01.2018
 */

abstract class AnnotationField extends AbstractElement implements ConfigElement, BuilderElement, ValidatedElement, TransformedField.Transformable {
    private final Collection<ClassName> markers;
    private final String javadoc;

    AnnotationField(String name, TypeName type, Collection<AnnotationSpec> annotations, String javadoc, Collection<ClassName> markers) {
        super(name, type, annotations);
        this.javadoc = javadoc;
        this.markers = markers;
    }

    boolean hasMarker(ClassName marker) {
        return markers.contains(marker);
    }

    @Override
    public final void addToBuilder(TypeSpec.Builder builder, ClassName builderName, CodeBlock.Builder constructorAlways, CodeBlock.Builder constructorWhenAnnotationPresent) {
        addWithoutGetter(builder, builderName, constructorAlways, constructorWhenAnnotationPresent);
        addGetter(builder);
    }

    @Override
    public final void addWithoutGetter(TypeSpec.Builder builder, ClassName builderName, CodeBlock.Builder constructorAlways, CodeBlock.Builder constructorWhenAnnotationPresent) {
        BuilderElement.super.addToBuilder(builder, builderName, constructorAlways, constructorWhenAnnotationPresent);
        addSetter(builder, builderName);
        addInitializer(constructorWhenAnnotationPresent);
    }

    protected abstract void addInitializer(CodeBlock.Builder constructorWhenAnnotationPresent);

    @Override
    public void configureSetter(MethodSpec.Builder builder) {
        if (javadoc != null) {
            builder.addJavadoc(javadoc.replaceAll("(\n|^) ", "$1").replaceAll("@return ((.|\n)*)$", "@param " + getName() + " $1@return this instance\n"));
        }
    }

    static class Normal extends AnnotationField {
        private final AnnotationValue defaultValue;

        Normal(String name, TypeName type, Collection<AnnotationSpec> annotations, Collection<ClassName> markers, AnnotationValue defaultValue, String javadoc) {
            super(name, type, annotations, javadoc, markers);
            this.defaultValue = defaultValue;
        }

        @Override
        public void addInitializer(CodeBlock.Builder constructorWhenAnnotationPresent) {
            constructorWhenAnnotationPresent.addStatement("$1L = $2L.$1L()", getName(), Strings.VAR_ANNOTATION);
        }

        @Override
        public void configureField(FieldSpec.Builder builder) {
            if (defaultValue != null) {
                final List<Object> parameters = new ArrayList<>();
                final String statement = defaultValue.accept(new InitializerVisitor(getType()), parameters);
                builder.initializer(statement, parameters.toArray(new Object[parameters.size()]));
            }
        }

        @Override
        public void addToBuildMethod(BuildMethodCreator method) {
            if (defaultValue == null) {
                method.addNotUnset(getName(), getType());
            }
            if (hasMarker(Types.NON_EMPTY)) {
                method.addNotEmpty(getName());
            }
            if (hasMarker(Types.INSTANTIATABLE)) {
                method.addInstantiatable(getName());
            }
            if (hasMarker(Types.ANY_NON_DEFAULT)) {
                method.addAnyNonDefault(getName(), defaultValue);
            }
        }

    }

    static class StringResource extends AnnotationField {
        private final String originalName;
        private final boolean allowNull;

        StringResource(String name, Collection<AnnotationSpec> annotations, Collection<ClassName> markers,
                       boolean allowNull, String javadoc) {
            super((name.startsWith(Strings.PREFIX_RES)) ? WordUtils.uncapitalize(name.substring(Strings.PREFIX_RES.length())) : name, Types.STRING, annotations, javadoc, markers);
            this.originalName = name;
            this.allowNull = allowNull;
            getAnnotations().remove(Types.STRING_RES);
            if (allowNull) {
                getAnnotations().add(Types.NULLABLE);
            }
        }

        @Override
        public void addInitializer(CodeBlock.Builder constructorWhenAnnotationPresent) {
            constructorWhenAnnotationPresent.beginControlFlow("if ($L.$L() != 0)", Strings.VAR_ANNOTATION, originalName)
                    .addStatement("$L = $L.getString($L.$L())", getName(), Strings.FIELD_CONTEXT, Strings.VAR_ANNOTATION, originalName)
                    .endControlFlow();
        }

        @Override
        public void addSetter(TypeSpec.Builder builder, ClassName builderName) {
            super.addSetter(builder, builderName);
            final String parameterName = Strings.PREFIX_RES + WordUtils.capitalize(getName());
            final List<AnnotationSpec> annotations = new ArrayList<>(getAnnotations());
            annotations.removeIf(Types.NULLABLE::equals);
            annotations.add(Types.STRING_RES);
            final MethodSpec.Builder setter = BuilderElement.createSetter(builderName, parameterName, TypeName.INT, annotations, "this.$L = $L.getString($L)",
                    getName(), Strings.FIELD_CONTEXT, parameterName);
            configureSetter(setter);
            builder.addMethod(setter.build());
        }

        @Override
        public void addToBuildMethod(BuildMethodCreator method) {
            if (!allowNull) {
                method.addNotUnset(getName(), getType());
            }
            if (hasMarker(Types.ANY_NON_DEFAULT)) {
                method.addAnyNonDefault(getName(), null);
            }
        }
    }

}
