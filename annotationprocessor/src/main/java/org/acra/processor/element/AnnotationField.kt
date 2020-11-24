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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import org.acra.processor.creator.BuildMethodCreator
import org.acra.processor.util.IsValidResourceVisitor
import org.acra.processor.util.Strings
import org.acra.processor.util.ToCodeBlockVisitor
import org.acra.processor.util.Types
import org.apache.commons.text.WordUtils
import java.util.*
import javax.lang.model.element.AnnotationValue

/**
 * @author F43nd1r
 * @since 12.01.2018
 */
abstract class AnnotationField(override val name: String, override val type: TypeName, override val annotations: Collection<AnnotationSpec>, private val javadoc: String?,
                               private val markers: Collection<ClassName>, val defaultValue: AnnotationValue?) : TransformedField.Transformable {
    fun hasMarker(marker: ClassName): Boolean {
        return markers.contains(marker)
    }

    override fun addToBuilder(builder: TypeSpec.Builder, builderName: ClassName, constructor: CodeBlock.Builder) {
        super.addToBuilder(builder, builderName, constructor)
        addSetter(builder, builderName)
        addInitializer(constructor)
    }

    override fun configureField(builder: PropertySpec.Builder) {
        if (javadoc != null) {
            builder.addKdoc("%L", javadoc.replace("(\n|^) ".toRegex(), "$1"))
        }
    }

    protected abstract fun addInitializer(constructor: CodeBlock.Builder)
    override fun configureSetter(builder: FunSpec.Builder) {
        if (javadoc != null) {
            val name = builder.build().parameters[0].name
            builder.addKdoc("%L", javadoc.replace("(\n|^) ".toRegex(), "$1").replace("@return ((.|\n)*)$".toRegex(), "@param $name $1@return this instance\n"))
        }
    }

    open class Normal(name: String, type: TypeName, annotations: Collection<AnnotationSpec>, markers: Collection<ClassName>, defaultValue: AnnotationValue?, javadoc: String) :
            AnnotationField(name, type, annotations, javadoc, markers, defaultValue) {
        public override fun addInitializer(constructor: CodeBlock.Builder) {
            constructor.addStatement("%1L = %2L?.%1L ?: %3L", name, Strings.VAR_ANNOTATION, defaultValue?.accept(ToCodeBlockVisitor(), null))
        }

        override fun addToBuildMethod(method: BuildMethodCreator) {
            if (defaultValue == null) {
                method.addNotUnset(name)
            }
            if (hasMarker(Types.NON_EMPTY)) {
                method.addNotEmpty(name)
            }
            if (hasMarker(Types.INSTANTIATABLE)) {
                method.addInstantiatable(name)
            }
            if (hasMarker(Types.ANY_NON_DEFAULT)) {
                method.addAnyNonDefault(name, defaultValue?.accept(ToCodeBlockVisitor(), null) ?: CodeBlock.of("null"))
            }
        }
    }

    class Clazz(name: String, type: TypeName, annotations: Collection<AnnotationSpec>, markers: Collection<ClassName>, defaultValue: AnnotationValue?, javadoc: String) :
            Normal(name, type, annotations, markers, defaultValue, javadoc) {
        override fun addInitializer(constructor: CodeBlock.Builder) {
            constructor.addStatement("%1L = %2L?.%1L?.java ?: %3L", name, Strings.VAR_ANNOTATION, defaultValue?.accept(ToCodeBlockVisitor(), null))
        }
    }

    class Array(name: String, type: TypeName, annotations: Collection<AnnotationSpec>, markers: Collection<ClassName>, defaultValue: AnnotationValue?, javadoc: String) :
            Normal(name, (type as ParameterizedTypeName).let { it.rawType.parameterizedBy(WildcardTypeName.producerOf(it.typeArguments.first())) }, annotations, markers,
                    defaultValue, javadoc) {
        private val originalType = type
        override fun configureSetter(builder: FunSpec.Builder) {
            super.configureSetter(builder)
            val param = builder.parameters.first().toBuilder(type = (originalType as ParameterizedTypeName).typeArguments.first()).addModifiers(KModifier.VARARG).build()
            builder.parameters.clear()
            builder.addParameter(param)
        }
    }

    class StringResource(private val originalName: String, annotations: Collection<AnnotationSpec>, markers: Collection<ClassName>,
                         defaultValue: AnnotationValue?, javadoc: String?) :
            AnnotationField(if (originalName.startsWith(Strings.PREFIX_RES)) WordUtils.uncapitalize(originalName.substring(Strings.PREFIX_RES.length)) else originalName,
                    Types.STRING, annotations - Types.STRING_RES, javadoc, markers, defaultValue) {
        private val hasDefault: Boolean = defaultValue != null && defaultValue.accept(IsValidResourceVisitor(), null)

        public override fun addInitializer(constructor: CodeBlock.Builder) {
            if (hasDefault) {
                constructor.addStatement("%L = %L.getString(%L?.%L.takeIf·{ it != 0 } ?: %L)", name, Strings.FIELD_CONTEXT, Strings.VAR_ANNOTATION, originalName, defaultValue)
            } else {
                constructor.addStatement("%L = %L?.%L.takeIf·{ it != 0 }?.let·{ %L.getString(it) } ?: \"\"", name, Strings.VAR_ANNOTATION, originalName, Strings.FIELD_CONTEXT)
            }
        }

        override fun addSetter(builder: TypeSpec.Builder, builderName: ClassName) {
            super.addSetter(builder, builderName)
            val setter = baseResSetter(builderName)
                    .addStatement("this.%L = %L.getString(%L)", name, Strings.FIELD_CONTEXT, Strings.ensurePrefix(Strings.PREFIX_RES, name))
                    .addStatement("return this")
            configureSetter(setter)
            builder.addFunction(setter.build())
        }

        private fun baseResSetter(builderName: ClassName): FunSpec.Builder {
            val parameterName = Strings.ensurePrefix(Strings.PREFIX_RES, name)
            val annotations: MutableList<AnnotationSpec> = ArrayList(annotations)
            annotations.add(Types.STRING_RES)
            return FunSpec.builder(Strings.ensurePrefix(Strings.PREFIX_SETTER, parameterName))
                    .addParameter(ParameterSpec.builder(parameterName, Int::class).addAnnotations(annotations).build())
                    .addModifiers(KModifier.PUBLIC)
                    .returns(builderName)
        }

        override fun addToBuildMethod(method: BuildMethodCreator) {
            if (defaultValue == null) {
                method.addNotUnset(name)
            }
            if (hasMarker(Types.ANY_NON_DEFAULT)) {
                method.addAnyNonDefault(name, CodeBlock.of("\"\""))
            }
        }
    }
}