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

package org.acra.definition;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.acra.ModelUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * @author F43nd1r
 * @since 04.06.2017
 */

public class Transformer implements Method {
    private final String name;
    private final String delegate;
    private final Type returnType;
    private final Transformable transformable;

    public static Transformer from(ExecutableElement method, String field, Transformable transformable, ModelUtils utils) {
        return new Transformer(method.getSimpleName().toString(), field, utils.getType(method.getReturnType()), transformable);
    }

    private Transformer(String name, String delegate, Type returnType, Transformable transformable) {
        this.name = name;
        this.delegate = delegate;
        this.returnType = returnType;
        this.transformable = transformable;
    }


    @Override
    public boolean shouldPropagate() {
        return transformable.shouldPropagate();
    }

    @Override
    public void writeTo(TypeSpec.Builder builder, ModelUtils utils) {
        final Pair<String, List<Object>> baseStatement = transformable.getStatementWithParams(utils);
        final List<Object> params = baseStatement.getValue();
        params.add(delegate);
        params.add(name);
        final String statement = "return $" + (params.size() - 1) + "L.$" + params.size() + "L(" + baseStatement.getKey() + ")";
        builder.addMethod(MethodSpec.methodBuilder(transformable.getName())
                .addAnnotations(transformable.getAnnotations())
                .returns(returnType.getName())
                .addStatement(statement, params.toArray())
                .build());
    }

    @Override
    public String getName() {
        return transformable.getName();
    }

    @Override
    public List<AnnotationSpec> getAnnotations() {
        return transformable.getAnnotations();
    }

    @Override
    public TypeName getReturnType() {
        return returnType.getName();
    }
}
