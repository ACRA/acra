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
package org.acra.processor.util

import com.squareup.kotlinpoet.CodeBlock
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.SimpleAnnotationValueVisitor8

/**
 * @author F43nd1r
 * @since 12.01.2018
 */
class ToCodeBlockVisitor : SimpleAnnotationValueVisitor8<CodeBlock, Unit?>() {
    override fun defaultAction(o: Any, u: Unit?): CodeBlock {
        return CodeBlock.of("%L", o)
    }

    override fun visitString(s: String, u: Unit?): CodeBlock {
        return CodeBlock.of("%S", s)
    }

    override fun visitEnumConstant(c: VariableElement, u: Unit?): CodeBlock {
        return CodeBlock.of("%T.%L", c.asType(), c.simpleName)
    }

    override fun visitType(t: TypeMirror, u: Unit?): CodeBlock {
        return CodeBlock.of("%T::class.java", t)
    }

    override fun visitArray(values: List<AnnotationValue?>, u: Unit?): CodeBlock {
        return CodeBlock.of("arrayOf(%L)", values.map { value: AnnotationValue? -> value!!.accept(this, null) }
                .reduceOrNull { c1: CodeBlock, c2: CodeBlock -> CodeBlock.builder().add(c1).add(", ").add(c2).build() } ?: CodeBlock.builder().build())
    }
}