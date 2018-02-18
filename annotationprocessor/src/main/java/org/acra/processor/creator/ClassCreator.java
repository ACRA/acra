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

package org.acra.processor.creator;

import android.support.annotation.NonNull;
import com.google.auto.common.MoreTypes;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.acra.annotation.Configuration;
import org.acra.config.ConfigurationBuilder;
import org.acra.processor.element.*;
import org.acra.processor.util.Strings;
import org.acra.processor.util.Types;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static org.acra.processor.util.Strings.*;

/**
 * @author F43nd1r
 * @since 04.06.2017
 */

public class ClassCreator {
    private final TypeElement baseAnnotation;
    private final Configuration configuration;
    private final ProcessingEnvironment processingEnv;
    private final String factoryName;
    private final String configName;
    private final String builderName;
    private final String builderVisibleName;

    public ClassCreator(@NonNull TypeElement baseAnnotation, Configuration configuration, @NonNull ProcessingEnvironment processingEnv) {
        this.baseAnnotation = baseAnnotation;
        this.configuration = configuration;
        this.processingEnv = processingEnv;
        configName = baseAnnotation.getSimpleName().toString().replace("Acra", "") + "Configuration";
        builderVisibleName = configName + "Builder";
        builderName = configuration.isPlugin() ? builderVisibleName + "Impl" : builderVisibleName;
        factoryName = builderVisibleName + "Factory";

    }

    public void createClasses() throws IOException {
        TypeElement baseBuilder;
        try {
            baseBuilder = processingEnv.getElementUtils().getTypeElement(configuration.baseBuilderClass().getName());
        } catch (MirroredTypeException e) {
            baseBuilder = MoreTypes.asTypeElement(e.getTypeMirror());
        }
        final List<Element> elements = new ModelBuilder(baseAnnotation, new ElementFactory(processingEnv.getElementUtils()), baseBuilder, processingEnv.getMessager()).build();
        createBuilderClass(elements);
        createConfigClass(elements);
        if (configuration.isPlugin()) {
            createBuilderInterface(elements);
            createFactoryClass();
        }
    }

    private void createBuilderInterface(@NonNull List<Element> elements) throws IOException {
        final TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(builderVisibleName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ConfigurationBuilder.class);
        final TypeName baseAnnotation = TypeName.get(this.baseAnnotation.asType());
        Strings.addClassJavadoc(interfaceBuilder, baseAnnotation);
        ClassName builder = ClassName.get(PACKAGE, builderVisibleName);
        elements.stream().filter(BuilderElement.Interface.class::isInstance).map(BuilderElement.Interface.class::cast)
                .forEach(element -> element.addToBuilderInterface(interfaceBuilder, builder));
        Strings.writeClass(processingEnv.getFiler(), interfaceBuilder.build());
    }

    private void createBuilderClass(@NonNull List<Element> elements) throws IOException {
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(builderName)
                .addModifiers(Modifier.FINAL);
        final TypeName baseAnnotation = TypeName.get(this.baseAnnotation.asType());
        Strings.addClassJavadoc(classBuilder, baseAnnotation);
        final MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder(Types.CONTEXT, PARAM_0).addAnnotation(Types.NON_NULL).build())
                .addJavadoc("@param $L object annotated with {@link $T}\n", PARAM_0, baseAnnotation)
                .addStatement("final $1T $2L = $3L.getClass().getAnnotation($1T.class)", baseAnnotation, VAR_ANNOTATION, PARAM_0);
        if (!configuration.isPlugin()) {
            classBuilder.addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ConfigurationBuilder.class);
            constructor.addModifiers(Modifier.PUBLIC);
        } else {
            classBuilder.addSuperinterface(ClassName.get(PACKAGE, builderVisibleName));
        }
        final CodeBlock.Builder always = CodeBlock.builder();
        final CodeBlock.Builder whenAnnotationPresent = CodeBlock.builder();
        final CodeBlock.Builder whenAnnotationMissing = CodeBlock.builder();
        ClassName builder = ClassName.get(PACKAGE, builderName);
        elements.stream().filter(BuilderElement.class::isInstance).map(BuilderElement.class::cast).forEach(m -> m.addToBuilder(classBuilder, builder, always, whenAnnotationPresent, whenAnnotationMissing));
        constructor.addCode(always.build())
                .beginControlFlow("if ($L)", Strings.FIELD_ENABLED)
                .addCode(whenAnnotationPresent.build())
                .nextControlFlow("else")
                .addCode(whenAnnotationMissing.build())
                .endControlFlow();
        classBuilder.addMethod(constructor.build());
        final BuildMethodCreator build = new BuildMethodCreator(Types.getOnlyMethod(processingEnv, ConfigurationBuilder.class.getName()), ClassName.get(PACKAGE, configName));
        elements.stream().filter(ValidatedElement.class::isInstance).map(ValidatedElement.class::cast).forEach(element -> element.addToBuildMethod(build));
        classBuilder.addMethod(build.build());
        Strings.writeClass(processingEnv.getFiler(), classBuilder.build());
    }


    private void createConfigClass(@NonNull List<Element> elements) throws IOException {
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(configName)
                .addSuperinterface(Serializable.class)
                .addSuperinterface(org.acra.config.Configuration.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        Strings.addClassJavadoc(classBuilder, ClassName.get(baseAnnotation.asType()));
        final MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ClassName.get(PACKAGE, builderName), PARAM_0).addAnnotation(Types.NON_NULL).build());
        elements.stream().filter(ConfigElement.class::isInstance).map(ConfigElement.class::cast).forEach(element -> element.addToConfig(classBuilder, constructor));
        classBuilder.addMethod(constructor.build());
        Strings.writeClass(processingEnv.getFiler(), classBuilder.build());
    }

    private void createFactoryClass() throws IOException {
        final TypeName configurationBuilderFactory = Types.CONFIGURATION_BUILDER_FACTORY;
        Strings.writeClass(processingEnv.getFiler(), TypeSpec.classBuilder(factoryName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(configurationBuilderFactory)
                .addAnnotation(AnnotationSpec.builder(AutoService.class).addMember("value", "$T.class", configurationBuilderFactory).build())
                .addMethod(Types.overriding(Types.getOnlyMethod(processingEnv, Strings.CONFIGURATION_BUILDER_FACTORY))
                        .addAnnotation(Types.NON_NULL)
                        .addStatement("return new $T($L)", ClassName.get(PACKAGE, builderName), PARAM_0)
                        .build())
                .build());
    }
}
