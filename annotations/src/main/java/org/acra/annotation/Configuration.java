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

package org.acra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation annotated with this will be used as basis for Configurations and their Builders
 *
 * @author F43nd1r
 * @since 17.03.2017
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Configuration {
    Class baseBuilderClass() default Object.class;
    boolean isPlugin() default true;
}
