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

import java.util.ArrayList;
import java.util.List;

/**
 * @author F43nd1r
 * @since 12.06.2017
 */

public class FieldGetter extends FieldMethod implements org.acra.definition.Transformable {
    public FieldGetter(Field field) {
        super(field);
    }

    @Override
    public boolean shouldPropagate() {
        return true;
    }

    @Override
    public void writeTo(TypeSpec.Builder builder, ModelUtils utils) {
        final Pair<String, List<Object>> statement = getStatementWithParams(utils);
        builder.addMethod(MethodSpec.methodBuilder(getField().getName())
                .addAnnotations(getField().getAnnotations())
                .returns(getField().getType().getName())
                .addStatement("return " + statement.getKey(), statement.getValue().toArray())
                .build());
    }

    @Override
    public String getName() {
        return getField().getName();
    }

    @Override
    public List<AnnotationSpec> getAnnotations() {
        return getField().getAnnotations();
    }

    @Override
    public TypeName getReturnType() {
        return getField().getType().getName();
    }

    @Override
    public Pair<String, List<Object>> getStatementWithParams(ModelUtils utils) {
        final List<Object> params = new ArrayList<>();
        params.add(getField().getName());
        String result = "$" + params.size() + "L";
        return Pair.of(result, params);
    }
}
