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
import android.support.annotation.Nullable;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

import java.util.Collection;

/**
 * @author F43nd1r
 * @since 12.01.2018
 */

class AbstractElement implements Element {
    private final String name;
    private final TypeName type;
    private final Collection<AnnotationSpec> annotations;

    AbstractElement(@NonNull String name, @Nullable TypeName type, @NonNull Collection<AnnotationSpec> annotations) {
        this.type = type;
        this.name = name;
        this.annotations = annotations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TypeName getType() {
        return type;
    }

    @Override
    public Collection<AnnotationSpec> getAnnotations() {
        return annotations;
    }
}
