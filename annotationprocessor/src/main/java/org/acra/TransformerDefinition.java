package org.acra;

import com.squareup.javapoet.AnnotationSpec;

import org.acra.annotation.Transform;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author F43nd1r
 * @since 04.06.2017
 */

public class TransformerDefinition extends MethodDefinition {
    private final String transformMethodName;

    TransformerDefinition(String name, TypeMirror type, List<AnnotationSpec> annotations, boolean hasDefault, String transformMethodName) {
        super(name, type, annotations, hasDefault);
        this.transformMethodName = transformMethodName;
    }

    public static TransformerDefinition from(ExecutableElement method) {
        return new TransformerDefinition(method.getSimpleName().toString(), method.getReturnType(), ModelUtils.getAnnotations(method),
                method.getDefaultValue() != null, method.getAnnotation(Transform.class).methodName());
    }

    public String getTransformMethodName() {
        return transformMethodName;
    }
}
