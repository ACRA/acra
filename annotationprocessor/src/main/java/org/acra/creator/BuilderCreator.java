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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.acra.ModelUtils;
import org.acra.annotation.PreBuild;
import org.acra.annotation.Transform;
import org.acra.config.ConfigurationBuilder;
import org.acra.definition.DelegateMethod;
import org.acra.definition.Field;
import org.acra.definition.FieldGetter;
import org.acra.definition.FieldSetter;
import org.acra.definition.Method;
import org.acra.definition.Transformer;
import org.acra.definition.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import static org.acra.ModelUtils.FIELD_0;
import static org.acra.ModelUtils.PARAM_0;
import static org.acra.ModelUtils.VAR_0;

/**
 * @author F43nd1r
 * @since 11.06.2017
 */

class BuilderCreator {
    static final String ENABLED = "enabled";

    private final TypeSpec.Builder classBuilder;
    private final MethodSpec.Builder constructor;
    private final BuildMethodCreator build;
    private final Type baseAnnotation;
    private final ModelUtils utils;
    private final Type baseBuilder;
    private final ClassName builder;
    private final List<Method> methods;

    BuilderCreator(Type baseAnnotation, Type baseBuilder, ClassName config, ClassName builder, ModelUtils utils) {
        this.baseAnnotation = baseAnnotation;
        this.utils = utils;
        this.baseBuilder = baseBuilder;
        this.builder = builder;
        methods = new ArrayList<>();
        final Type configurationBuilder = utils.getType(ConfigurationBuilder.class);
        classBuilder = TypeSpec.classBuilder(builder.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(configurationBuilder.getName());
        utils.addClassJavadoc(classBuilder, baseAnnotation.getElement());
        constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)), PARAM_0).addAnnotation(NonNull.class).build())
                .addJavadoc("@param $L class annotated with {@link $T}\n", PARAM_0, baseAnnotation.getName());
        build = new BuildMethodCreator(utils.getOnlyMethod(configurationBuilder.getElement()), config);
        final Field enabled = new Field(ENABLED, utils.getBooleanType(), Collections.emptyList(), null, null);
        enabled.addTo(classBuilder, utils);
        methods.add(new FieldGetter(enabled));
        methods.add(new FieldSetter(enabled, builder));
    }

    private void addConvenienceConstructor() {
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeName.OBJECT, PARAM_0).addAnnotation(NonNull.class).build())
                .addStatement("this($L.getClass())", PARAM_0)
                .addJavadoc("@param $L object annotated with {@link $T}\n", PARAM_0, baseAnnotation.getName())
                .build());
    }

    private void handleAnnotationMethods() {
        constructor.addStatement("final $1T $2L = $3L.getAnnotation($1T.class)", baseAnnotation.getName(), VAR_0, PARAM_0)
                .addStatement("$L = $L != null", ENABLED, VAR_0)
                .beginControlFlow("if ($L)", ENABLED);
        for (ExecutableElement method : utils.getMethods(baseAnnotation.getElement())) {
            final Field field = Field.from(method, utils);
            field.addTo(classBuilder, utils);
            methods.add(new FieldGetter(field));
            methods.add(new FieldSetter(field, builder));
            build.addValidation(field, method);
            constructor.addStatement("$L = $L.$L()", field.getName(), VAR_0, method.getSimpleName().toString());
        }
        constructor.endControlFlow();
    }

    private boolean handleBaseBuilder() {
        if (!baseBuilder.getName().equals(TypeName.OBJECT)) {
            classBuilder.addField(FieldSpec.builder(baseBuilder.getName(), ModelUtils.FIELD_0, Modifier.PRIVATE, Modifier.FINAL).build());
            final List<ExecutableElement> constructors = utils.getConstructors(baseBuilder.getElement());
            if (constructors.stream().anyMatch(c -> c.getParameters().size() == 0)) {
                constructor.addStatement("$L = new $T()", FIELD_0, baseBuilder.getName());
            } else if (constructors.stream().anyMatch(c -> c.getParameters().size() == 1 && utils.hasClassParameter(c))) {
                constructor.addStatement("$L = new $T($L)", FIELD_0, baseBuilder.getName(), PARAM_0);
            } else {
                utils.error(baseAnnotation.getElement(), "builderSuperClass", "Classes used as base builder must have a constructor which takes no arguments, " +
                        "or exactly one argument of type Class");
            }
            return true;
        }
        return false;
    }

    private void handleBaseBuilderMethods() {
        for (ExecutableElement method : utils.getMethods(baseBuilder.getElement())) {
            if (method.getAnnotation(PreBuild.class) != null) {
                build.addMethodCall(FIELD_0, method.getSimpleName().toString());
            } else if (method.getAnnotation(Transform.class) != null) {
                final String transform = method.getAnnotation(Transform.class).methodName();
                methods.stream().filter(m -> m instanceof FieldGetter).map(FieldGetter.class::cast).filter(m -> m.getName().equals(transform)).findAny()
                        .ifPresent(m -> methods.set(methods.indexOf(m), Transformer.from(method, FIELD_0, m, utils)));
            } else {
                methods.add(DelegateMethod.from(method, FIELD_0, builder, utils));
            }
        }
    }

    private void createMethods() {
        methods.forEach(m -> m.writeTo(classBuilder, utils));
    }

    private void finishBuildMethod() {
        classBuilder.addMethod(build.build());
    }

    private void finishConstructor() {
        classBuilder.addMethod(constructor.build());
    }

    List<Method> create() throws IOException {
        addConvenienceConstructor();
        handleAnnotationMethods();
        if (handleBaseBuilder()) {
            handleBaseBuilderMethods();
        }
        createMethods();
        finishBuildMethod();
        finishConstructor();
        utils.write(classBuilder.build());
        return methods;
    }
}
