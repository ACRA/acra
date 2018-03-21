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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.acra.processor.creator.BuildMethodCreator;
import org.acra.processor.util.Strings;

import java.util.Collection;

/**
 * @author F43nd1r
 * @since 11.01.2018
 */

class TransformedField implements ConfigElement, BuilderElement.Interface, ValidatedElement {
    private final String name;
    private final TypeName type;
    private final Transformable transform;

    TransformedField(@NonNull String name, @NonNull TypeName type, @NonNull Transformable transform) {
        this.name = name;
        this.type = type;
        this.transform = transform;
    }

    @Override
    public void addToBuilder(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName, @NonNull CodeBlock.Builder constructorAlways,
                             @NonNull CodeBlock.Builder constructorWhenAnnotationPresent, CodeBlock.Builder constructorWhenAnnotationMissing) {
        transform.addWithoutGetter(builder, builderName, constructorAlways, constructorWhenAnnotationPresent, constructorWhenAnnotationMissing);
        addGetter(builder);
    }

    @Override
    public void configureGetter(@NonNull MethodSpec.Builder builder) {
        builder.addStatement("return $L.$L($L)", Strings.FIELD_DELEGATE, name, getName());
    }

    @Override
    public void addToBuildMethod(@NonNull BuildMethodCreator method) {
        transform.addToBuildMethod(method);
    }

    @Override
    public void addToBuilderInterface(@NonNull TypeSpec.Builder builder, @NonNull ClassName builderName) {
        transform.addToBuilderInterface(builder, builderName);
    }

    @Override
    public String getName() {
        return transform.getName();
    }

    @Override
    public TypeName getType() {
        return type;
    }

    @Override
    public Collection<AnnotationSpec> getAnnotations() {
        return transform.getAnnotations();
    }

    public interface Transformable extends ConfigElement, Interface, ValidatedElement {
        void addWithoutGetter(TypeSpec.Builder builder, ClassName builderName, CodeBlock.Builder constructorAlways, CodeBlock.Builder constructorWhenAnnotationPresent, CodeBlock.Builder constructorWhenAnnotationMissing);
    }
}
