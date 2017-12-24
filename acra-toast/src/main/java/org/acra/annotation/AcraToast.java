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

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author F43nd1r
 * @since 02.06.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration
public @interface AcraToast {

    /**
     * toast text triggered when the application crashes
     *
     * @return Resource id for the Toast text triggered when the application crashes.
     * @see android.widget.Toast#makeText(Context, int, int)
     * @since 5.0.0
     */
    @StringRes int resText();

    /**
     * One of {@link android.widget.Toast#LENGTH_LONG} and {@link android.widget.Toast#LENGTH_SHORT}
     *
     * @return toast length
     * @see android.widget.Toast#makeText(Context, int, int)
     * @since 5.0.0
     */
    @IntRange(from = 0, to = 1) int length() default Toast.LENGTH_LONG;
}
