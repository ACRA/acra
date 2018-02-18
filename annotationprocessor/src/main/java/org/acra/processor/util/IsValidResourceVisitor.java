package org.acra.processor.util;

import javax.lang.model.util.SimpleAnnotationValueVisitor8;

public class IsValidResourceVisitor extends SimpleAnnotationValueVisitor8<Boolean, Void> {
    @Override
    protected Boolean defaultAction(Object o, Void aVoid) {
        return false;
    }

    @Override
    public Boolean visitInt(int i, Void aVoid) {
        return i != 0;
    }
}
