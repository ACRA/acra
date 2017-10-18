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

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.acra.annotation.AnyNonDefault;
import org.acra.annotation.Configuration;
import org.acra.annotation.Instantiatable;
import org.acra.annotation.NonEmpty;
import org.acra.collections.ImmutableList;
import org.acra.collections.ImmutableMap;
import org.acra.collections.ImmutableSet;
import org.acra.definition.Type;

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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Collection of constants and helper methods to generate ACRA classes
 *
 * @author F43nd1r
 * @since 18.03.2017
 */

public class ModelUtils {
    public static final String PARAM_0 = "arg0";
    public static final String VAR_0 = "var0";
    public static final String FIELD_0 = "field0";
    public static final String PACKAGE = "org.acra.config";
    private static final ClassName IMMUTABLE_MAP = ClassName.get(ImmutableMap.class);
    private static final ClassName IMMUTABLE_SET = ClassName.get(ImmutableSet.class);
    private static final ClassName IMMUTABLE_LIST = ClassName.get(ImmutableList.class);
    private static final ClassName MAP = ClassName.get(Map.class);
    private static final ClassName SET = ClassName.get(Set.class);
    private static final ClassName LIST = ClassName.get(List.class);
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

    private final Types typeUtils;
    private final Elements elementUtils;
    private final ProcessingEnvironment processingEnv;

    ModelUtils(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    public Type getType(Supplier<Class> supplier) {
        TypeMirror mirror;
        try {
            mirror = elementUtils.getTypeElement(supplier.get().getName()).asType();
        } catch (MirroredTypeException e) {
            mirror = e.getTypeMirror();
        }
        return getType(mirror);
    }

    public Type getType(TypeMirror mirror) {
        return new Type(mirror.getKind() == TypeKind.TYPEVAR || mirror.getKind().isPrimitive() || mirror.getKind() == TypeKind.ARRAY  || mirror.getKind() == TypeKind.VOID
                ? null : MoreTypes.asTypeElement(typeUtils, mirror), mirror, TypeName.get(mirror));
    }

    public Type getType(Class c) {
        return new Type(elementUtils.getTypeElement(c.getName()));
    }

    public Type getBooleanType(){
        return new Type(null, typeUtils.getPrimitiveType(TypeKind.BOOLEAN), TypeName.BOOLEAN);
    }

    /**
     * Returns an immutable type extending this type, or if the type is an array a immutable list type
     *
     * @param type the type
     * @return the immutable counterpart (might be type, if type is already immutable or no immutable type was found)
     */
    public TypeName getImmutableType(TypeName type) {
        if (type instanceof ParameterizedTypeName) {
            final TypeName genericType = ((ParameterizedTypeName) type).rawType;
            if (MAP.equals(genericType)) {
                return getWithParams(IMMUTABLE_MAP, (ParameterizedTypeName) type);
            } else if (SET.equals(genericType)) {
                return getWithParams(IMMUTABLE_SET, (ParameterizedTypeName) type);
            } else if (LIST.equals(genericType)) {
                return getWithParams(IMMUTABLE_LIST, (ParameterizedTypeName) type);
            }
        } else if (type instanceof ArrayTypeName) {
            return ParameterizedTypeName.get(IMMUTABLE_LIST, ((ArrayTypeName) type).componentType);
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
    private TypeName getWithParams(ClassName baseType, ParameterizedTypeName parameterType) {
        return ParameterizedTypeName.get(baseType, parameterType.typeArguments.toArray(new TypeName[parameterType.typeArguments.size()]));
    }

    /**
     * Writes the given class to a respective file in the configuration package
     *
     * @param typeSpec the class
     * @throws IOException if writing fails
     */
    public void write(TypeSpec typeSpec) throws IOException {
        JavaFile.builder(PACKAGE, typeSpec)
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
    public List<AnnotationSpec> getAnnotations(ExecutableElement method) {
        return method.getAnnotationMirrors().stream().map(AnnotationSpec::get)
                .filter(annotationSpec -> !excludeAnnotationsFromBuilder.contains(annotationSpec.type)).collect(Collectors.toList());
    }

    public void addClassJavadoc(TypeSpec.Builder builder, TypeElement base) {
        builder.addJavadoc("Class generated based on {@link $L} ($L)\n", base.getQualifiedName(), DATE_FORMAT.format(Calendar.getInstance().getTime()));
    }

    public ExecutableElement getOnlyMethod(TypeElement typeElement) {
        final List<ExecutableElement> elements = getMethods(typeElement);
        if (elements.size() == 1) {
            return elements.get(0);
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Needs exactly one method", typeElement);
            throw new IllegalArgumentException();
        }
    }

    public void error(Element element, String annotationField, String message) {
        final AnnotationMirror mirror = element.getAnnotationMirrors().stream()
                .filter(m -> MoreTypes.isTypeOf(Configuration.class, m.getAnnotationType()))
                .findAny().orElseThrow(IllegalArgumentException::new);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element, mirror, mirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().toString().equals(annotationField)).findAny().map(Map.Entry::getValue).orElse(null));
        throw new IllegalArgumentException();
    }

    public List<ExecutableElement> getConstructors(Element element) {
        return ElementFilter.constructorsIn(element.getEnclosedElements());
    }

    public List<ExecutableElement> getMethods(Element element) {
        return ElementFilter.methodsIn(element.getEnclosedElements());
    }

    public boolean hasClassParameter(ExecutableElement method) {
        return method.getParameters().stream().map(VariableElement::asType).map(typeUtils::asElement).map(Element::toString).anyMatch(Class.class.getName()::equals);
    }

    public TypeMirror erasure(TypeMirror t) {
        return typeUtils.erasure(t);
    }

    public String getJavadoc(Element element) {
        return elementUtils.getDocComment(element);
    }

    public List<ParameterSpec> getParameters(ExecutableElement method){
        return method.getParameters().stream().map(p -> ParameterSpec.builder(TypeName.get(p.asType()), p.getSimpleName().toString()).build()).collect(Collectors.toList());
    }

    public List<TypeVariableName> getTypeParameters(ExecutableElement method){
        return method.getTypeParameters().stream().map(TypeVariableName::get).collect(Collectors.toList());
    }
}
