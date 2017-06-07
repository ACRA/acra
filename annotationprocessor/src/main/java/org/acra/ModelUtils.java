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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.acra.annotation.AnyNonDefault;
import org.acra.annotation.Configuration;
import org.acra.annotation.Instantiatable;
import org.acra.annotation.NoPropagation;
import org.acra.annotation.NonEmpty;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Collection of constants and helper methods to generate ACRA classes
 *
 * @author F43nd1r
 * @since 18.03.2017
 */

class ModelUtils {
    final String configurationPackage;
    static final String PREFIX_SETTER = "set";
    static final String PARAM_0 = "arg0";
    static final String VAR_0 = "var0";
    final Type application;
    final ClassName nonNull;
    final Type configurationBuilderFactory;
    final Type configurationBuilder;
    final TypeName configuration;
    final TypeName acraConfigurationException;
    private static final ClassName ANNOTATION_NO_PROPAGATION = ClassName.get(NoPropagation.class);

    final Types typeUtils;
    final Elements elementUtils;
    private final TypeMirror map;
    private final TypeMirror set;
    private final TypeElement immutableMap;
    private final TypeElement immutableSet;
    private final TypeElement immutableList;
    private final ProcessingEnvironment processingEnv;
    private final DateFormat dateFormat;

    ModelUtils(ProcessingEnvironment processingEnv, Configuration settings) {
        this.processingEnv = processingEnv;
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        map = elementUtils.getTypeElement(Map.class.getName()).asType();
        set = elementUtils.getTypeElement(Set.class.getName()).asType();
        immutableMap = (TypeElement) typeUtils.asElement(getTypeMirror(settings::mapWrapper));
        immutableSet = (TypeElement) typeUtils.asElement(getTypeMirror(settings::setWrapper));
        immutableList = (TypeElement) typeUtils.asElement(getTypeMirror(settings::listWrapper));
        dateFormat = DateFormat.getDateTimeInstance();
        application = getType(getTypeMirror(settings::applicationClass));
        nonNull = (ClassName) TypeName.get(getTypeMirror(settings::nonNull));
        configurationBuilderFactory = getType(getTypeMirror(settings::configurationBuilderFactory));
        configurationBuilder = getType(getTypeMirror(settings::configurationBuilder));
        configuration = TypeName.get(getTypeMirror(settings::configuration));
        acraConfigurationException = TypeName.get(getTypeMirror(settings::configurationException));
        configurationPackage = settings.packageName();
    }

    TypeMirror getTypeMirror(Supplier<Class> supplier) {
        try {
            return elementUtils.getTypeElement(supplier.get().getName()).asType();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }

    /**
     * Returns an immutable type extending this type, or if the type is an array a immutable list type
     *
     * @param type the type
     * @return the immutable counterpart (might be type, if type is already immutable or no immutable type was found)
     */
    TypeMirror getImmutableType(TypeMirror type) {
        if (typeUtils.isAssignable(typeUtils.erasure(type), map)) {
            return getWithParams(immutableMap, type);
        } else if (typeUtils.isAssignable(typeUtils.erasure(type), set)) {
            return getWithParams(immutableSet, type);
        } else if (type.getKind() == TypeKind.ARRAY) {
            return typeUtils.getDeclaredType(immutableList, ((ArrayType) type).getComponentType());
        }
        return type;
    }

    /**
     * Creates a type based on base, but with the type parameters from parameterType
     *
     * @param baseType      base
     * @param parameterType parameterType
     * @return the parametrized type
     */
    private TypeMirror getWithParams(TypeElement baseType, TypeMirror parameterType) {
        final List<? extends TypeMirror> parameters = ((DeclaredType) parameterType).getTypeArguments();
        return typeUtils.getDeclaredType(baseType, parameters.toArray(new TypeMirror[parameters.size()]));
    }

    /**
     * Writes the given class to a respective file in the configuration package
     *
     * @param typeSpec the class
     * @throws IOException if writing fails
     */
    void write(TypeSpec typeSpec) throws IOException {
        JavaFile.builder(configurationPackage, typeSpec)
                .skipJavaLangImports(true)
                .indent("    ")
                .addFileComment("Copyright (c) " + Calendar.getInstance().get(Calendar.YEAR) + "\n\n" +
                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n\n" +
                        "http://www.apache.org/licenses/LICENSE-2.0\n\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.")
                .build()
                .writeTo(processingEnv.getFiler());
    }

    private static final List<TypeName> excludeAnnotationsFromBuilder = Arrays.asList(ClassName.get(NonEmpty.class),
            ClassName.get(AnyNonDefault.class), ClassName.get(Instantiatable.class));

    /**
     * @param method a method
     * @return annotationSpecs for all relevant annotations on the method
     */
    static List<AnnotationSpec> getAnnotations(ExecutableElement method) {
        return method.getAnnotationMirrors().stream().map(AnnotationSpec::get)
                .filter(annotationSpec -> !excludeAnnotationsFromBuilder.contains(annotationSpec.type)).collect(Collectors.toList());
    }

    /**
     * Box the type if it is primitive
     *
     * @param type the type to box
     * @return the boxed type or type, if it is not primitive
     */
    TypeMirror getBoxedType(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return typeUtils.boxedClass((PrimitiveType) type).asType();
        } else {
            return type;
        }
    }

    /**
     * Capitalizes the first letter in the given string
     *
     * @param word the string
     * @return the string with a capitalized first letter
     */
    String capitalizeFirst(final String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    /**
     * Determines if a method is relevant for ACRAConfiguration generation
     * A method is not relevant, if it starts with "set", or is annotated with @Hide
     *
     * @param method the method to check
     * @return if the method is relevant
     */
    boolean shouldRetain(MethodDefinition method) {
        return !method.getName().startsWith(PREFIX_SETTER) && !method.getAnnotations().stream().anyMatch(a -> a.type.equals(ANNOTATION_NO_PROPAGATION));
    }

    /**
     * @param method a method
     * @return false if the method is deprecated
     */
    boolean isNotDeprecated(ExecutableElement method) {
        return method.getAnnotation(Deprecated.class) == null;
    }

    void addClassJavadoc(TypeSpec.Builder builder, TypeElement base) {
        builder.addJavadoc("Class generated based on {@link $L} ($L)\n", base.getQualifiedName(), dateFormat.format(Calendar.getInstance().getTime()));
    }

    MethodSpec.Builder addMethodJavadoc(MethodSpec.Builder builder, ExecutableElement base) {
        final String baseComment = elementUtils.getDocComment(base);
        if (baseComment == null) return builder;
        final String name = base.getSimpleName().toString();
        return builder.addJavadoc(baseComment.replaceAll("(\n|^) ", "$1").replaceAll("@return ((.|\n)*)$", "@param " + name + " $1@return this instance\n"));
    }

    ExecutableElement getOnlyMethod(TypeElement typeElement) {
        List<ExecutableElement> elements = typeElement.getEnclosedElements().stream().filter(element -> element.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast).collect(Collectors.toList());
        if (elements.size() == 1) {
            return elements.get(0);
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Needs exactly one method", typeElement);
            throw new IllegalArgumentException();
        }
    }

    void error(Element element, String annotationField, String message) {
        AnnotationMirror mirror = element.getAnnotationMirrors().stream()
                .filter(m -> ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString().equals(Configuration.class.getName()))
                .findAny().orElseThrow(IllegalArgumentException::new);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element, mirror, mirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().toString().equals(annotationField)).findAny().map(Map.Entry::getValue).orElse(null));
        throw new IllegalArgumentException();
    }

    Type getType(TypeMirror mirror) {
        return new Type((TypeElement) typeUtils.asElement(mirror));
    }

    boolean isConstructor(Element element) {
        return element.getKind() == ElementKind.CONSTRUCTOR;
    }

    List<ExecutableElement> getConstructors(Element element) {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.CONSTRUCTOR).map(ExecutableElement.class::cast).collect(Collectors.toList());
    }

    List<ExecutableElement> getMethods(Element element) {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.METHOD).map(ExecutableElement.class::cast).collect(Collectors.toList());
    }
}
