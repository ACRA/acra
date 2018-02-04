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

package org.acra.processor.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import com.squareup.javapoet.*;
import org.acra.annotation.*;
import org.acra.collections.ImmutableList;
import org.acra.collections.ImmutableMap;
import org.acra.collections.ImmutableSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author F43nd1r
 * @since 11.01.2018
 */

public final class Types {
    public static final ClassName IMMUTABLE_MAP = ClassName.get(ImmutableMap.class);
    public static final ClassName IMMUTABLE_SET = ClassName.get(ImmutableSet.class);
    public static final ClassName IMMUTABLE_LIST = ClassName.get(ImmutableList.class);
    public static final ClassName MAP = ClassName.get(Map.class);
    public static final ClassName SET = ClassName.get(Set.class);
    public static final ClassName LIST = ClassName.get(List.class);
    public static final ClassName STRING = ClassName.get(String.class);
    public static final AnnotationSpec NULLABLE = AnnotationSpec.builder(Nullable.class).build();
    public static final AnnotationSpec NON_NULL = AnnotationSpec.builder(NonNull.class).build();
    public static final AnnotationSpec STRING_RES = AnnotationSpec.builder(StringRes.class).build();
    public static final ClassName ANY_NON_DEFAULT = ClassName.get(AnyNonDefault.class);
    public static final ClassName BUILDER_METHOD = ClassName.get(BuilderMethod.class);
    public static final ClassName CONFIGURATION = ClassName.get(Configuration.class);
    public static final ClassName CONFIGURATION_VALUE = ClassName.get(ConfigurationValue.class);
    public static final ClassName INSTANTIATABLE = ClassName.get(Instantiatable.class);
    public static final ClassName NON_EMPTY = ClassName.get(NonEmpty.class);
    public static final ClassName PRE_BUILD = ClassName.get(PreBuild.class);
    public static final ClassName TRANSFORM = ClassName.get(Transform.class);
    public static final ClassName CONTEXT = ClassName.bestGuess(Strings.CONTEXT);
    public static final ClassName CONFIGURATION_BUILDER_FACTORY = ClassName.bestGuess(Strings.CONFIGURATION_BUILDER_FACTORY);
    public static final List<ClassName> MARKER_ANNOTATIONS = Arrays.asList(ANY_NON_DEFAULT, BUILDER_METHOD, CONFIGURATION, CONFIGURATION_VALUE, INSTANTIATABLE, NON_EMPTY, PRE_BUILD, TRANSFORM);

    private Types() {
    }

    public static MethodSpec.Builder overriding(ExecutableElement method) {
        return MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addAnnotation(Override.class)
                .addModifiers(method.getModifiers().stream().filter(modifier -> modifier != Modifier.ABSTRACT).collect(Collectors.toList()))
                .returns(TypeName.get(method.getReturnType()))
                .varargs(method.isVarArgs())
                .addExceptions(method.getThrownTypes().stream().map(TypeName::get).collect(Collectors.toList()))
                .addTypeVariables(method.getTypeParameters().stream().map(TypeVariableName::get).collect(Collectors.toList()))
                .addParameters(method.getParameters().stream().map(element -> ParameterSpec.get(element).toBuilder()
                        .addAnnotations(element.getAnnotationMirrors().stream().map(AnnotationSpec::get).collect(Collectors.toList())).build()).collect(Collectors.toList()));
    }

    public static ExecutableElement getOnlyMethod(ProcessingEnvironment processingEnv, String className) {
        final TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(className);
        final List<ExecutableElement> elements = ElementFilter.methodsIn(typeElement.getEnclosedElements());
        if (elements.size() == 1) {
            return elements.get(0);
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Needs exactly one method", typeElement);
            throw new IllegalArgumentException();
        }
    }

    @NonNull
    public static TypeName getImmutableType(TypeName type) {
        if (type instanceof ParameterizedTypeName) {
            final TypeName genericType = ((ParameterizedTypeName) type).rawType;
            if (MAP.equals(genericType)) {
                type = getWithParams(IMMUTABLE_MAP, (ParameterizedTypeName) type);
            } else if (SET.equals(genericType)) {
                return getWithParams(IMMUTABLE_SET, (ParameterizedTypeName) type);
            } else if (LIST.equals(genericType)) {
                type = getWithParams(IMMUTABLE_LIST, (ParameterizedTypeName) type);
            }
        } else if (type instanceof ArrayTypeName) {
            type = ParameterizedTypeName.get(IMMUTABLE_LIST, ((ArrayTypeName) type).componentType);
        }
        return type;
    }

    private static TypeName getWithParams(@NonNull ClassName baseType, @NonNull ParameterizedTypeName parameterType) {
        return ParameterizedTypeName.get(baseType, parameterType.typeArguments.toArray(new TypeName[parameterType.typeArguments.size()]));
    }
}
