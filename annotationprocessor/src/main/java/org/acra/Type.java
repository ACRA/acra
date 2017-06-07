package org.acra;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author F43nd1r
 * @since 04.06.2017
 */

public class Type {
    private final TypeElement element;
    private final TypeMirror mirror;
    private final TypeName name;

    public Type(TypeElement element) {
        this.element = element;
        mirror = element.asType();
        name = TypeName.get(mirror);
    }

    public TypeElement getElement() {
        return element;
    }

    public TypeMirror getMirror() {
        return mirror;
    }

    public TypeName getName() {
        return name;
    }
}
