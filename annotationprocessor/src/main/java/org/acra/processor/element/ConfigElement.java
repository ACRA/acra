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

package org.acra.processor.element;

import android.support.annotation.NonNull;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.acra.processor.util.Types;

import javax.lang.model.element.Modifier;

import static org.acra.processor.util.Strings.PARAM_0;

/**
 * @author F43nd1r
 * @since 10.01.2018
 */

public interface ConfigElement extends Element {

    default void addToConfig(@NonNull TypeSpec.Builder builder, @NonNull MethodSpec.Builder constructor) {
        //add field
        final TypeName type = Types.getImmutableType(getType());
        builder.addField(FieldSpec.builder(type, getName(), Modifier.PRIVATE, Modifier.FINAL).addAnnotations(getAnnotations()).build());
        if (!type.equals(getType())) {
            constructor.addStatement("$1L = new $2T($3L.$1L())", getName(), type, PARAM_0);
        } else {
            constructor.addStatement("$1L = $2L.$1L()", getName(), PARAM_0);
        }
        //add getter
        builder.addMethod(MethodSpec.methodBuilder(getName())
                .addAnnotations(getAnnotations())
                .returns(type)
                .addStatement("return $L", getName())
                .addModifiers(Modifier.PUBLIC)
                .build());
    }
}
