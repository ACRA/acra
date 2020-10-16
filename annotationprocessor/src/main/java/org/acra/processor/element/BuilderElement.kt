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
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.acra.processor.util.Strings
import org.acra.processor.util.Types
import java.util.*

/**
 * @author F43nd1r
 * @since 11.01.2018
 */
interface BuilderElement : Element {
    fun addToBuilder(builder: TypeSpec.Builder, builderName: ClassName, constructor: CodeBlock.Builder) {
        val field = PropertySpec.builder(name, type).addAnnotations(annotations).mutable()
        configureField(field)
        builder.addProperty(field.build())
    }

    fun configureField(builder: PropertySpec.Builder) {}

    fun addSetter(builder: TypeSpec.Builder, builderName: ClassName) {
        val method = baseSetter(builderName)
                .addStatement("return apply·{ this.%1L·= %1L }", name)
        configureSetter(method)
        builder.addFunction(method.build())
    }

    fun baseSetter(builderName: ClassName): FunSpec.Builder {
        val builder = FunSpec.builder(Strings.ensurePrefix(Strings.PREFIX_SETTER, name))
        val annotations = ArrayList(annotations)
        val deprecated = annotations.find { it.typeName == Deprecated::class.asTypeName() }
        if (deprecated != null) {
            annotations.remove(deprecated)
            builder.addAnnotation(deprecated)
        }
        return builder
                .addParameter(ParameterSpec.builder(name, type).addAnnotations(annotations).build())
                .addModifiers(KModifier.PUBLIC)
                .returns(builderName)
    }

    fun configureSetter(builder: FunSpec.Builder) {}

    interface Final : BuilderElement {
        override fun configureField(builder: PropertySpec.Builder) {
            builder.addModifiers(KModifier.FINAL)
        }
    }

    class Context : AbstractElement(Strings.FIELD_CONTEXT, Types.CONTEXT, emptyList()), Final {
        override fun addToBuilder(builder: TypeSpec.Builder, builderName: ClassName, constructor: CodeBlock.Builder
        ) {
            super.addToBuilder(builder, builderName, constructor)
            constructor.addStatement("%L = %L", name, Strings.PARAM_0)
        }
    }

    class Delegate internal constructor(type: TypeName, private val hasContextParameter: Boolean) : AbstractElement(Strings.FIELD_DELEGATE, type, emptyList()), Final {
        override fun addToBuilder(builder: TypeSpec.Builder, builderName: ClassName, constructor: CodeBlock.Builder
        ) {
            super.addToBuilder(builder, builderName, constructor)
            if (hasContextParameter) {
                constructor.addStatement("%L = %T(%L)", name, type, Strings.PARAM_0)
            } else {
                constructor.addStatement("%L = %T()", name, type)
            }
        }
    }

    class Enabled : AbstractElement(Strings.FIELD_ENABLED, Boolean::class.asClassName(), emptyList()), BuilderElement, ConfigElement {
        override fun addToBuilder(builder: TypeSpec.Builder, builderName: ClassName, constructor: CodeBlock.Builder
        ) {
            super.addToBuilder(builder, builderName, constructor)
            addSetter(builder, builderName)
            constructor.addStatement("%L = %L != null", name, Strings.VAR_ANNOTATION)
        }

        override fun addToConfig(builder: TypeSpec.Builder, constructor: FunSpec.Builder) {
            super.addToConfig(builder, constructor)
            builder.addFunction(FunSpec.builder(Strings.FIELD_ENABLED).addStatement("return %L", Strings.FIELD_ENABLED).addModifiers(KModifier.OVERRIDE).returns(Boolean::class).build())
        }
    }
}