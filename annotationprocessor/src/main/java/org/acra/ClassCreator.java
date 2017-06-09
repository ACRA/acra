package org.acra;

import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.acra.annotation.AnyNonDefault;
import org.acra.annotation.Configuration;
import org.acra.annotation.Instantiatable;
import org.acra.annotation.NonEmpty;
import org.acra.annotation.PreBuild;
import org.acra.annotation.Transform;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ClassValidator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static org.acra.ModelUtils.*;

/**
 * @author F43nd1r
 * @since 04.06.2017
 */

public class ClassCreator {
    private final Type baseAnnotation;
    private final Configuration configuration;
    private final String configName;
    private final ModelUtils utils;
    private final String builderName;
    private final String factoryName;
    private final ClassName config;
    private final ClassName builder;

    public ClassCreator(TypeElement baseAnnotation, Configuration configuration, ModelUtils utils) {
        this.baseAnnotation = new Type(baseAnnotation);
        this.configuration = configuration;
        configName = configuration.configName();
        this.utils = utils;
        builderName = configName + "Builder";
        factoryName = builderName + "Factory";
        config = ClassName.get(configuration.packageName(), configName);
        builder = ClassName.get(configuration.packageName(), builderName);

    }

    public void createClasses() throws IOException {
        createConfigClass(createBuilderClass());
        if (configuration.createBuilderFactory()) {
            createFactoryClass();
        }
    }

    private Set<MethodDefinition> createBuilderClass() throws IOException {
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(builderName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(utils.configurationBuilder.getName());
        utils.addClassJavadoc(classBuilder, baseAnnotation.getElement());
        final Type superClass = utils.getType(utils.getTypeMirror(configuration::builderSuperClass));
        final List<? extends TypeParameterElement> typeParameters = superClass.getElement().getTypeParameters();
        if (typeParameters.size() == 0) {
            classBuilder.superclass(superClass.getName());
        } else if (typeParameters.size() == 1) {
            classBuilder.superclass(ParameterizedTypeName.get(((ParameterizedTypeName) superClass.getName()).rawType, builder));
        } else {
            utils.error(baseAnnotation.getElement(), "builderSuperClass", "builderSuperClass may not have more than one type parameter");
        }
        final MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)), PARAM_0).addAnnotation(NonNull.class).build());
        final MethodSpec.Builder build = MethodSpec.overriding(utils.getOnlyMethod(utils.configurationBuilder.getElement()))
                .returns(config);
        final Set<MethodDefinition> methods = new HashSet<>();
        final List<TransformerDefinition> transformerDefinitions = new ArrayList<>();
        if (!superClass.getName().equals(TypeName.OBJECT)) {
            final List<ExecutableElement> constructors = utils.getConstructors(superClass.getElement());
            if (constructors.stream().anyMatch(c -> c.getParameters().size() == 0)) {
                constructor.addStatement("super()");
            } else if (constructors.stream().anyMatch(c -> c.getParameters().size() == 1 && utils.hasClassParameter(c))) {
                constructor.addStatement("super($L)", PARAM_0);
            } else {
                utils.error(baseAnnotation.getElement(), "builderSuperClass", "Classes used as base builder must have a constructor which takes no arguments, " +
                        "or exactly one argument of type Class");
            }
            methods.addAll(handleSuperClassMethods(superClass, build, transformerDefinitions));
        }
        constructor.addStatement("final $1T $2L = $3L.getAnnotation($1T.class)", baseAnnotation.getName(), VAR_0, PARAM_0)
                .beginControlFlow("if ($L != null)", VAR_0);
        methods.addAll(handleMethods(utils.getMethods(baseAnnotation.getElement()), transformerDefinitions, classBuilder, constructor, build));
        classBuilder.addMethod(constructor.endControlFlow().build());
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeName.OBJECT, PARAM_0).addAnnotation(NonNull.class).build())
                .addStatement("this($L.getClass())", PARAM_0)
                .build());
        classBuilder.addMethod(build.addStatement("return new $T(this)", config).build());
        utils.write(configuration.packageName(), classBuilder.build());
        return methods;
    }

    private List<MethodDefinition> handleSuperClassMethods(Type type, MethodSpec.Builder build, List<TransformerDefinition> definitionsOut) {
        final List<MethodDefinition> result = new ArrayList<>();
        for (ExecutableElement method : utils.getMethods(type.getElement())) {
            if (method.getAnnotation(PreBuild.class) != null) {
                build.addStatement("$L()", method.getSimpleName().toString());
            } else if (method.getAnnotation(Transform.class) != null) {
                definitionsOut.add(TransformerDefinition.from(method));
            } else if (utils.shouldRetain(MethodDefinition.from(method))) {
                result.add(MethodDefinition.from(method));
            }
        }
        return result;
    }

    private Set<MethodDefinition> handleMethods(List<ExecutableElement> methods, List<TransformerDefinition> transformerDefinitions,
                                                TypeSpec.Builder classBuilder, MethodSpec.Builder constructor, MethodSpec.Builder build) {
        final Set<MethodDefinition> result = new HashSet<>();
        final List<ExecutableElement> anyNonDefault = new ArrayList<>();
        for (ExecutableElement method : methods) {
            final String name = method.getSimpleName().toString();
            final TypeMirror type = method.getReturnType();
            final TypeName typeName = TypeName.get(type);
            final TypeName boxedType = TypeName.get(utils.getBoxedType(type));
            final List<AnnotationSpec> annotations = ModelUtils.getAnnotations(method);
            final TransformerDefinition transformer = transformerDefinitions.stream().filter(d -> d.getTransformMethodName().equals(name)).findAny().orElse(null);
            classBuilder.addField(FieldSpec.builder(boxedType, name, Modifier.PRIVATE)
                    .addAnnotations(annotations)
                    .build());
            classBuilder.addMethod(utils.addMethodJavadoc(MethodSpec.methodBuilder(PREFIX_SETTER + utils.capitalizeFirst(name)), method)
                    .returns(builder)
                    .addParameter(ParameterSpec.builder(typeName, name).addAnnotations(annotations).build())
                    .varargs(type.getKind() == TypeKind.ARRAY)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("this.$1L = $1L", name)
                    .addStatement("return this")
                    .build());
            final Object defaultValue = method.getDefaultValue();
            if (defaultValue == null) {
                build.beginControlFlow("if ($L == null)", name)
                        .addStatement("throw new $T(\"$L has to be set\")", ACRAConfigurationException.class, name)
                        .endControlFlow();
            }
            classBuilder.addMethod(MethodSpec.methodBuilder(name)
                    .returns(transformer == null ? typeName : TypeName.get(transformer.getType()))
                    .addAnnotations(annotations)
                    .addCode(createGetterCode(method, transformer))
                    .build());
            result.add(new MethodDefinition(name, transformer == null ? type : transformer.getType(), annotations, defaultValue != null));
            constructor.addStatement("$L = $L.$L()", name, VAR_0, method.getSimpleName().toString());
            if (method.getAnnotation(NonEmpty.class) != null) {
                build.beginControlFlow("if ($L().length == 0)", name)
                        .addStatement("throw new $T(\"$L cannot be empty\")", ACRAConfigurationException.class, name)
                        .endControlFlow();
            }
            if (method.getAnnotation(Instantiatable.class) != null) {
                build.addStatement("$T.check($L())", ClassName.get(ClassValidator.class), name);
            }
            if (method.getAnnotation(AnyNonDefault.class) != null) {
                anyNonDefault.add(method);
            }
        }
        if (anyNonDefault.size() > 0) {
            build.beginControlFlow("if ($L)", anyNonDefault.stream().map(m -> m.getSimpleName().toString() + "() == " + m.getDefaultValue()).collect(Collectors.joining(" && ")))
                    .addStatement("throw new $T(\"One of $L must not be default\")", ACRAConfigurationException.class,
                            anyNonDefault.stream().map(m -> m.getSimpleName().toString()).collect(Collectors.joining(", ")))
                    .endControlFlow();
        }
        return result;
    }

    private CodeBlock createGetterCode(ExecutableElement method, MethodDefinition transformer) {
        final List<Object> params = new ArrayList<>();
        params.add(method.getSimpleName().toString());
        String result = "$" + params.size() + "L";
        final Object defaultValue = method.getDefaultValue();
        if (defaultValue != null) {
            result = "$" + params.size() + "L != null ? " + result + " : ";
            if (method.getReturnType().getKind() == TypeKind.ARRAY) {
                params.add(utils.erasure(method.getReturnType()));
                result += "new $" + params.size() + "T";
            }
            params.add(defaultValue);
            result += "$" + params.size() + "L";
        }
        if (transformer != null) {
            params.add(transformer.getName());
            result = "$" + params.size() + "L(" + result + ")";
        }
        result = "return " + result;
        return CodeBlock.builder().addStatement(result, params.toArray()).build();
    }


    private void createConfigClass(Set<MethodDefinition> methodDefinitions) throws IOException {
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(configName)
                .addSuperinterface(Serializable.class)
                .addSuperinterface(org.acra.config.Configuration.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        utils.addClassJavadoc(classBuilder, baseAnnotation.getElement());
        final CodeBlock.Builder constructor = CodeBlock.builder();
        for (MethodDefinition method : methodDefinitions) {
            final String name = method.getName();
            final TypeName type = utils.getImmutableType(method.getType());
            if (!type.equals(TypeName.get(method.getType()))) {
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
        classBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(builder, PARAM_0).addAnnotation(NonNull.class).build())
                .addCode(constructor.build())
                .build());
        utils.write(configuration.packageName(), classBuilder.build());
    }

    private void createFactoryClass() throws IOException {
        utils.write(configuration.packageName(), TypeSpec.classBuilder(factoryName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(utils.configurationBuilderFactory.getName())
                .addAnnotation(AnnotationSpec.builder(AutoService.class).addMember("value", "$T.class", utils.configurationBuilderFactory.getName()).build())
                .addMethod(MethodSpec.overriding(utils.getOnlyMethod(utils.configurationBuilderFactory.getElement()))
                        .addStatement("return new $T($L)", builder, PARAM_0)
                        .build())
                .build());
    }
}
