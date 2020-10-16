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

import com.google.auto.common.MoreTypes
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import org.acra.annotation.Configuration
import org.acra.config.ConfigurationBuilder
import org.acra.processor.element.BuilderElement
import org.acra.processor.element.ConfigElement
import org.acra.processor.element.Element
import org.acra.processor.element.ElementFactory
import org.acra.processor.element.ValidatedElement
import org.acra.processor.util.Strings
import org.acra.processor.util.Types
import org.acra.processor.util.writeTo
import org.apache.commons.text.WordUtils
import java.io.Serializable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * @author F43nd1r
 * @since 04.06.2017
 */
class ClassCreator(private val baseAnnotation: TypeElement, private val configuration: Configuration, private val processingEnv: ProcessingEnvironment) {
    private val baseName = baseAnnotation.simpleName.toString().replace("Acra", "")
    private val configName: String = baseName + "Configuration"
    private val builderName: String = configName + "Builder"
    private val factoryName: String = builderName + "Factory"

    fun createClasses() {
        val baseBuilder: TypeElement?
        baseBuilder = try {
            processingEnv.elementUtils.getTypeElement(configuration.baseBuilderClass.qualifiedName)
        } catch (e: MirroredTypeException) {
            MoreTypes.asTypeElement(e.typeMirror)
        }
        val elements = ModelBuilder(baseAnnotation, ElementFactory(processingEnv.elementUtils), baseBuilder!!, processingEnv.messager).build()
        createBuilderClass(elements)
        createConfigClass(elements)
        if (configuration.isPlugin) {
            createFactoryClass()
            createExtensions()
        }
    }

    private fun createBuilderClass(elements: List<Element>) {
        val classBuilder = TypeSpec.classBuilder(builderName)
                .addOriginatingElement(baseAnnotation)
        val baseAnnotation = baseAnnotation.asType().asTypeName()
        Strings.addClassKdoc(classBuilder, baseAnnotation)
        val constructor = FunSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder(Strings.PARAM_0, Types.CONTEXT).build())
                .addKdoc("@param %L object annotated with {@link %T}\n", Strings.PARAM_0, baseAnnotation)
                .addStatement("val %2L : %1T? = %3L.javaClass.getAnnotation(%1T::class.java)", baseAnnotation, Strings.VAR_ANNOTATION, Strings.PARAM_0)
        classBuilder.addSuperinterface(ConfigurationBuilder::class.java)
        val primaryConstructor = CodeBlock.builder()
        val builder = ClassName(Strings.PACKAGE, builderName)
        elements.filterIsInstance<BuilderElement>().forEach { it.addToBuilder(classBuilder, builder, primaryConstructor) }
        classBuilder.primaryConstructor(constructor.addCode(primaryConstructor.build()).build())
        val build = BuildMethodCreator(Types.getOnlyMethod(processingEnv, ConfigurationBuilder::class.java.name), ClassName(Strings.PACKAGE, configName))
        elements.stream().filter { obj: Element? -> ValidatedElement::class.java.isInstance(obj) }.map { obj: Element? -> ValidatedElement::class.java.cast(obj) }.forEach { element: ValidatedElement -> element.addToBuildMethod(build) }
        classBuilder.addFunction(build.build())
        Strings.writeClass(processingEnv, classBuilder.build())
    }

    private fun createConfigClass(elements: List<Element>) {
        val classBuilder = TypeSpec.classBuilder(configName)
                .addOriginatingElement(baseAnnotation)
                .addSuperinterface(Serializable::class.java)
                .addSuperinterface(org.acra.config.Configuration::class.java)
        Strings.addClassKdoc(classBuilder, baseAnnotation.asType().asTypeName())
        val constructor = FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC)
                .addParameter(ParameterSpec.builder(Strings.PARAM_0, ClassName(Strings.PACKAGE, builderName)).build())
        elements.filterIsInstance<ConfigElement>().forEach { it.addToConfig(classBuilder, constructor) }
        classBuilder.addFunction(constructor.build())
        Strings.writeClass(processingEnv, classBuilder.build())
    }

    private fun createFactoryClass() {
        val configurationBuilderFactory = Types.CONFIGURATION_BUILDER_FACTORY
        Strings.writeClass(processingEnv, TypeSpec.classBuilder(factoryName)
                .addOriginatingElement(baseAnnotation)
                .addModifiers(KModifier.PUBLIC)
                .addSuperinterface(configurationBuilderFactory)
                .addAnnotation(AnnotationSpec.builder(AutoService::class.java).addMember("value = [%T::class]", configurationBuilderFactory).build())
                .addFunction(Types.overriding(Types.getOnlyMethod(processingEnv, Strings.CONFIGURATION_BUILDER_FACTORY))
                        .addStatement("return %T(%L)", ClassName(Strings.PACKAGE, builderName), Strings.PARAM_0)
                        .build())
                .build())
    }

    private fun createExtensions() {
        val builder = ClassName(Strings.PACKAGE, builderName)
        FileSpec.builder(Strings.PACKAGE, "${baseName}Extensions")
                .addFunction(FunSpec.builder(WordUtils.uncapitalize(baseName))
                        .receiver(ClassName(Strings.PACKAGE, "CoreConfigurationBuilder"))
                        .addParameter(ParameterSpec("initializer", LambdaTypeName.get(builder, returnType = Unit::class.asTypeName())))
                        .addStatement("this.getPluginConfigurationBuilder(%T::class.java).initializer()", builder)
                        .build())
                .writeTo(processingEnv)
    }
}