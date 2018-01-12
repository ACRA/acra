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
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.acra.processor.creator.BuildMethodCreator;
import org.acra.processor.util.Strings;

import java.util.Collection;

/**
 * @author F43nd1r
 * @since 11.01.2018
 */

class TransformedField implements ConfigElement, BuilderElement, ValidatedElement {
    private final String name;
    private final TypeName type;
    private final Transformable transform;

    TransformedField(String name, TypeName type, Transformable transform) {
        this.name = name;
        this.type = type;
        this.transform = transform;
    }

    @Override
    public void addToBuilder(TypeSpec.Builder builder, ClassName builderName, CodeBlock.Builder constructorAlways, CodeBlock.Builder constructorWhenAnnotationPresent) {
        transform.addWithoutGetter(builder, builderName, constructorAlways, constructorWhenAnnotationPresent);
        addGetter(builder);
    }

    @Override
    public void configureGetter(MethodSpec.Builder builder) {
        builder.addStatement("return $L.$L($L)", Strings.FIELD_DELEGATE, name, getName());
    }

    @Override
    public void addToBuildMethod(BuildMethodCreator method) {
        transform.addToBuildMethod(method);
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

    public interface Transformable extends ValidatedElement {
        void addWithoutGetter(TypeSpec.Builder builder, ClassName builderName, CodeBlock.Builder constructorAlways, CodeBlock.Builder constructorWhenAnnotationPresent);
    }
}
