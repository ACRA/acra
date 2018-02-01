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

package org.acra.config;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

/**
 * A factory for configuration builders
 *
 * @author F43nd1r
 * @since 01.06.2017
 */
@Keep
public interface ConfigurationBuilderFactory {
    /**
     * creates a new builder
     *
     * @param annotatedContext the context holding the annotation from which the builder should pull its values
     * @return a new builder with values from the annotation
     */
    @NonNull
    ConfigurationBuilder create(@NonNull Context annotatedContext);
}
