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
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import org.acra.processor.util.Strings
import java.util.stream.Collectors

/**
 * @author F43nd1r
 * @since 10.01.2018
 */
internal open class DelegateMethod(override val name: String, override val type: TypeName, override val annotations: Collection<AnnotationSpec>, private val parameters: Collection<ParameterSpec>,
                                   private val typeVariables: Collection<TypeVariableName>, private val modifiers: Collection<KModifier>, private val javadoc: String?) : BuilderElement {
    override fun addToBuilder(builder: TypeSpec.Builder, builderName: ClassName, constructor: CodeBlock.Builder) {
        val method = baseMethod(builderName)
        if (type == Unit::class.asTypeName()) {
            method.addStatement("%L.%L(%L)", Strings.FIELD_DELEGATE, name, parameters.stream().map { p: ParameterSpec -> p.name }.collect(Collectors.joining(", ")))
                    .addStatement("return this")
        } else {
            method.addStatement("return %L.%L(%L)", Strings.FIELD_DELEGATE, name, parameters.stream().map { p: ParameterSpec -> p.name }.collect(Collectors.joining(", ")))
        }
        builder.addFunction(method.build())
    }

    private fun baseMethod(builderName: ClassName): FunSpec.Builder {
        val method = FunSpec.builder(name)
                .addModifiers(modifiers)
                .addParameters(parameters)
                .addTypeVariables(typeVariables)
                .addAnnotations(annotations)
        if (javadoc != null) {
            method.addKdoc(javadoc.replace("(\n|^) ".toRegex(), "$1"))
        }
        if (type == Unit::class.asTypeName()) {
            method.returns(builderName)
                    .addKdoc("@return this instance\n")
        } else {
            method.returns(type)
        }
        return method
    }

    internal class Config(name: String, type: TypeName, annotations: Collection<AnnotationSpec>, parameters: Collection<ParameterSpec>,
                          typeVariables: Collection<TypeVariableName>, modifiers: Collection<KModifier>, javadoc: String?) : DelegateMethod(name, type, annotations, parameters, typeVariables, modifiers, javadoc), ConfigElement{


        override fun addToConfig(builder: TypeSpec.Builder, constructor: FunSpec.Builder) {
            builder.addProperty(PropertySpec.builder(name, type).build())
            constructor.addStatement("%1L = %2L.%1L()", name, Strings.PARAM_0)
        }
    }
}