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
package org.acra.processor.creator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName
import org.acra.config.ACRAConfigurationException
import org.acra.config.ClassValidator
import org.acra.processor.util.Strings
import org.acra.processor.util.Types
import javax.lang.model.element.ExecutableElement

/**
 * @author F43nd1r
 * @since 12.06.2017
 */
class BuildMethodCreator(override: ExecutableElement, private val config: ClassName) {
    private val methodBuilder: FunSpec.Builder = Types.overriding(override)
            .returns(config)
            .addAnnotation(AnnotationSpec.builder(Throws::class).addMember("exceptionClasses = [%L::class]", ACRAConfigurationException::class.asTypeName()).build())
            .beginControlFlow("if (%L)", Strings.FIELD_ENABLED)
    private val anyNonDefault: MutableMap<String, CodeBlock> = mutableMapOf()
    private val statements: MutableList<CodeBlock> = mutableListOf()

    fun addNotUnset(name: String) {
        methodBuilder.beginControlFlow("if (%L == null)", name)
                .addStatement("throw %T(\"%L has to be set\")", ACRAConfigurationException::class.java, name)
                .endControlFlow()
    }

    fun addNotEmpty(name: String) {
        methodBuilder.beginControlFlow("if (%L.isEmpty())", name)
                .addStatement("throw %T(\"%L cannot be empty\")", ACRAConfigurationException::class.java, name)
                .endControlFlow()
    }

    fun addInstantiatable(name: String) {
        methodBuilder.addStatement("%T.check(%L)", ClassValidator::class.java, name)
    }

    fun addAnyNonDefault(name: String, default: CodeBlock) {
        anyNonDefault[name] = default
    }

    fun addDelegateCall(methodName: String) {
        statements.add(CodeBlock.builder().addStatement("%L.%L()", Strings.FIELD_DELEGATE, methodName).build())
    }

    fun build(): FunSpec {
        if (anyNonDefault.isNotEmpty()) {
            methodBuilder.beginControlFlow("if (%L)", anyNonDefault.map { CodeBlock.of("%L == %L", it.key, it.value) }
                    .reduce { c1: CodeBlock, c2: CodeBlock -> CodeBlock.builder().add(c1).add(" && ").add(c2).build() })
                    .addStatement("throw %T(\"One·of·%L·must·not·be·default\")", ACRAConfigurationException::class.java, anyNonDefault.keys.joinToString(",·"))
                    .endControlFlow()
        }
        methodBuilder.endControlFlow()
        for (s in statements) {
            methodBuilder.addCode(s)
        }
        methodBuilder.addStatement("return %T(this)", config)
        return methodBuilder.build()
    }
}