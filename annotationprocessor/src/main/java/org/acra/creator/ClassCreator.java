/*
 * Copyright (c) 2017
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

package org.acra.creator;

import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.acra.ModelUtils;
import org.acra.definition.Type;
import org.acra.annotation.Configuration;
import org.acra.config.ConfigurationBuilderFactory;
import org.acra.definition.Method;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static org.acra.ModelUtils.PACKAGE;
import static org.acra.ModelUtils.PARAM_0;

/**
 * @author F43nd1r
 * @since 04.06.2017
 */

public class ClassCreator {
    private final Type baseAnnotation;
    private final Configuration configuration;
    private final ModelUtils utils;
    private final String factoryName;
    private final ClassName config;
    private final ClassName builder;

    public ClassCreator(TypeElement baseAnnotation, Configuration configuration, ModelUtils utils) {
        this.baseAnnotation = new Type(baseAnnotation);
        this.configuration = configuration;
        this.utils = utils;
        final String configName = baseAnnotation.getSimpleName().toString().replace("Acra", "") + "Configuration";
        final String builderName = configName + "Builder";
        factoryName = builderName + "Factory";
        config = ClassName.get(PACKAGE, configName);
        builder = ClassName.get(PACKAGE, builderName);

    }

    public void createClasses() throws IOException {
        createConfigClass(createBuilderClass());
        if (configuration.createBuilderFactory()) {
            createFactoryClass();
        }
    }

    private List<Method> createBuilderClass() throws IOException {
        return new BuilderCreator(baseAnnotation, utils.getType(configuration::baseBuilderClass), config, builder, utils).create().stream()
                .filter(Method::shouldPropagate).collect(Collectors.toList());
    }


    private void createConfigClass(List<Method> methods) throws IOException {
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(config.simpleName())
                .addSuperinterface(Serializable.class)
                .addSuperinterface(org.acra.config.Configuration.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        utils.addClassJavadoc(classBuilder, baseAnnotation.getElement());
        final MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(builder, PARAM_0).addAnnotation(NonNull.class).build());
        for (Method method : methods) {
            final String name = method.getName();
            final TypeName type = utils.getImmutableType(method.getReturnType());
            if (!type.equals(method.getReturnType())) {
                constructor.addStatement("$1L = new $2T($3L.$1L())", name, type, PARAM_0);
            } else {
                constructor.addStatement("$1L = $2L.$1L()", name, PARAM_0);
            }
            classBuilder.addField(FieldSpec.builder(type, name, Modifier.PRIVATE).addAnnotations(method.getAnnotations()).build());
            classBuilder.addMethod(MethodSpec.methodBuilder(name)
                    .returns(type)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotations(method.getAnnotations())
                    .addStatement("return $L", name)
                    .build());
        }
        classBuilder.addMethod(constructor.build());
        utils.write(classBuilder.build());
    }

    private void createFactoryClass() throws IOException {
        final Type configurationBuilderFactory = utils.getType(ConfigurationBuilderFactory.class);
        utils.write(TypeSpec.classBuilder(factoryName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(configurationBuilderFactory.getName())
                .addAnnotation(AnnotationSpec.builder(AutoService.class).addMember("value", "$T.class", configurationBuilderFactory.getName()).build())
                .addMethod(MethodSpec.overriding(utils.getOnlyMethod(configurationBuilderFactory.getElement()))
                        .addAnnotation(NonNull.class)
                        .addStatement("return new $T($L)", builder, PARAM_0)
                        .build())
                .build());
    }
}
