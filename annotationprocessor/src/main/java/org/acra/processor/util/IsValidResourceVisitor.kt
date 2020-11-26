package org.acra.processor.util

import javax.lang.model.util.SimpleAnnotationValueVisitor8

class IsValidResourceVisitor : SimpleAnnotationValueVisitor8<Boolean, Unit?>() {
    override fun defaultAction(o: Any, u: Unit?): Boolean {
        return false
    }

    override fun visitInt(i: Int, u: Unit?): Boolean {
        return i != 0
    }
}