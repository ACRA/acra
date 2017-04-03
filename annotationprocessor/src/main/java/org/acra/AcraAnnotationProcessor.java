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

package org.acra;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.acra.annotation.Configuration;
import org.acra.annotation.ConfigurationBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static org.acra.ModelUtils.*;

/**
 * Creates the BaseConfigurationBuilder class based on the annotation annotated with {@link Configuration}.
 * Creates the ACRAConfiguration class based on the BaseConfigurationBuilder and the class annotated with {@link ConfigurationBuilder}
 *
 * @author F43nd1r
 * @since 18.03.2017
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AcraAnnotationProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private ModelUtils utils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        utils = new ModelUtils(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Arrays.asList(Configuration.class.getName(), ConfigurationBuilder.class.getName()));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            final Set<MethodDefinition> methodDefinitions = process(roundEnv, Configuration.class.getName(), ElementKind.ANNOTATION_TYPE, new HashSet<>(), this::createBuilderClass);
            process(roundEnv, ConfigurationBuilder.class.getName(), ElementKind.CLASS, null, type -> createConfigClass(type, methodDefinitions));
        } catch (Exception e) {
            e.printStackTrace();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate acra classes");
        }
        return true;
    }

    private <T> T process(RoundEnvironment roundEnv, String annotationName, ElementKind kind, T defaultValue, CheckedFunction<TypeElement, T> function) throws IOException {
        final TypeElement annotation = elementUtils.getTypeElement(annotationName);
        final ArrayList<? extends Element> annotatedElements = new ArrayList<>(roundEnv.getElementsAnnotatedWith(annotation));
        if (annotatedElements.size() > 1) {
            for (Element e : annotatedElements) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Only one %s can be annotated with %s", kind.name(), annotationName), e);
            }
        } else if (!annotatedElements.isEmpty()) {
            final Element e = annotatedElements.get(0);
            if (e.getKind() == kind) {
                return function.apply((TypeElement) e);
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("%s is only supported on %s", annotationName, kind.name()), e);
            }
        }
        return defaultValue;
    }

    /**
     * Creates the ACRAConfiguration class
     *
     * @param builder           type of the builder which will be used to determine methods to generate
     * @param methodDefinitions additional methods to be included in the configuration (e.g. from the builder base class)
     * @return null
     * @throws IOException if the class file can't be written
     */
    private Void createConfigClass(TypeElement builder, Set<MethodDefinition> methodDefinitions) throws IOException {
        final Set<MethodDefinition> methods = getRelevantMethods(builder, methodDefinitions);
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(ACRA_CONFIGURATION)
                .addSuperinterface(Serializable.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        utils.addClassJavadoc(classBuilder, builder);
        final CodeBlock.Builder constructor = CodeBlock.builder();
        for (MethodDefinition method : methods) {
            final String name = method.getName();
            final TypeMirror type = utils.getImmutableType(method.getType());
            if (type != method.getType()) {
                constructor.addStatement("$1L = new $2T($3L.$1L())", name, type, PARAM_BUILDER);
            } else {
                constructor.addStatement("$1L = $2L.$1L()", name, PARAM_BUILDER);
            }
            final TypeName typeName = TypeName.get(type);
            classBuilder.addField(FieldSpec.builder(typeName, name, Modifier.PRIVATE).addAnnotations(method.getAnnotations()).build());
            classBuilder.addMethod(MethodSpec.methodBuilder(name)
                    .returns(typeName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotations(method.getAnnotations())
                    .addStatement("return $L", name)
                    .build());
        }
        classBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeName.get(builder.asType()), PARAM_BUILDER)
                        .addAnnotation(AnnotationSpec.builder(ANNOTATION_NON_NULL).build())
                        .build())
                .addCode(constructor.build())
                .build());
        utils.write(classBuilder.build());
        return null;
    }

    /**
     * Collects all relevant methods from a type.
     * For a definition of relevant methods, see {@link ModelUtils#shouldRetain(MethodDefinition)}.
     *
     * @param builder the type to collect methods from
     * @return relevant methods in the type
     */
    private Set<MethodDefinition> getRelevantMethods(TypeElement builder, Set<MethodDefinition> methodDefinitions) {
        final Set<MethodDefinition> result = builder.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.METHOD && !e.getModifiers().contains(Modifier.PRIVATE))
                .map(ExecutableElement.class::cast).map(MethodDefinition::from).collect(Collectors.toCollection(HashSet::new));
        result.addAll(methodDefinitions);
        return result.stream().filter(utils::shouldRetain).collect(Collectors.toSet());
    }

    /**
     * Creates the BaseConfigurationBuilder class
     *
     * @param config the configuration annotation type which will be used to determine methods to generate
     * @return all generated getters
     * @throws IOException if the class file can't be written
     */
    private Set<MethodDefinition> createBuilderClass(TypeElement config) throws IOException {
        final TypeVariableName returnType = TypeVariableName.get("T", ClassName.get(CONFIGURATION_PACKAGE, CONFIGURATION_BUILDER));
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CONFIGURATION_BUILDER)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(returnType);
        utils.addClassJavadoc(classBuilder, config);
        final CodeBlock.Builder constructor = CodeBlock.builder()
                .addStatement("final $1T $2L = $3L.getClass().getAnnotation($1T.class)", config.asType(), VAR_ANNOTATION_CONFIG, PARAM_APP)
                .beginControlFlow("if ($L != null)", VAR_ANNOTATION_CONFIG);
        final Set<MethodDefinition> result = config.getEnclosedElements().stream().filter(element -> element.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast).filter(utils::isNotDeprecated).map(e -> handleMethod(e, classBuilder, constructor, returnType)).collect(Collectors.toSet());
        constructor.endControlFlow();
        classBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(APPLICATION, PARAM_APP)
                        .addAnnotation(AnnotationSpec.builder(ANNOTATION_NON_NULL).build())
                        .build())
                .addCode(constructor.build())
                .build());
        utils.write(classBuilder.build());
        return result;
    }

    /**
     * Derives all code from one method: A setter, a getter, a field and a line in the constructor
     *
     * @param method       the method to derive from
     * @param classBuilder the class to add methods to
     * @param constructor  the constructor in which the field will be initialized
     * @return the generated getter
     */
    private MethodDefinition handleMethod(ExecutableElement method, TypeSpec.Builder classBuilder, CodeBlock.Builder constructor, TypeName returnType) {
        final String name = method.getSimpleName().toString();
        final TypeMirror type = method.getReturnType();
        final TypeName typeName = TypeName.get(type);
        final TypeName boxedType = TypeName.get(utils.getBoxedType(type));
        final List<AnnotationSpec> annotations = ModelUtils.getAnnotations(method);
        classBuilder.addField(FieldSpec.builder(boxedType, name, Modifier.PRIVATE)
                .addAnnotations(annotations)
                .build());
        classBuilder.addMethod(utils.addMethodJavadoc(MethodSpec.methodBuilder(PREFIX_SETTER + utils.capitalizeFirst(name)), method)
                .returns(returnType)
                .addParameter(ParameterSpec.builder(typeName, name).addAnnotations(annotations).build())
                .varargs(type.getKind() == TypeKind.ARRAY)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.$1L = $1L", name)
                .addStatement("return ($T) this", returnType)
                .build());
        final CodeBlock.Builder code = CodeBlock.builder()
                .beginControlFlow("if ($L != null)", name)
                .addStatement("return $L", name)
                .endControlFlow();
        if (type.getKind() == TypeKind.ARRAY) {
            code.addStatement("return new $T$L", typeUtils.erasure(type), method.getDefaultValue());
        } else {
            code.addStatement("return $L", method.getDefaultValue());
        }
        classBuilder.addMethod(MethodSpec.methodBuilder(name)
                .returns(typeName)
                .addAnnotations(annotations)
                .addCode(code.build())
                .build());
        constructor.addStatement("$1L = $2L.$1L()", name, VAR_ANNOTATION_CONFIG);
        return new MethodDefinition(name, type, annotations);
    }


    @FunctionalInterface
    interface CheckedFunction<T, R> {
        R apply(T t) throws IOException;
    }

}
