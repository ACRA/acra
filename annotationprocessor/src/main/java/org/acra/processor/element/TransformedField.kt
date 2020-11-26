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
package org.acra.processor.element

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.acra.processor.creator.BuildMethodCreator
import org.acra.processor.util.Strings

/**
 * @author F43nd1r
 * @since 11.01.2018
 */
class TransformedField(val transformName: String, override val type: TypeName, private val transform: Transformable) : ConfigElement, BuilderElement, ValidatedElement {
    override val name: String
        get() = transform.name
    override fun addToBuilder(builder: TypeSpec.Builder, builderName: ClassName, constructor: CodeBlock.Builder) {
        transform.addToBuilder(builder, builderName, constructor)
        builder.addFunction(FunSpec.builder(transformName).returns(type).addStatement("return %L.%L(%L)", Strings.FIELD_DELEGATE, transformName, name).build())
    }

    override fun addToBuildMethod(method: BuildMethodCreator) {
        transform.addToBuildMethod(method)
    }

    override fun addToConfig(builder: TypeSpec.Builder, constructor: FunSpec.Builder) {
        builder.addProperty(PropertySpec.builder(name, type).build())
        constructor.addStatement("%L = %L.%L()", name, Strings.PARAM_0, transformName)
    }

    override val annotations: Collection<com.squareup.kotlinpoet.AnnotationSpec>
        get() = transform.annotations

    interface Transformable : ConfigElement, BuilderElement, ValidatedElement
}