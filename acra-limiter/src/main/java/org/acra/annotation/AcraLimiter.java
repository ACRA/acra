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

import android.support.annotation.StringRes;

import org.acra.ACRAConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author F43nd1r
 * @since 26.10.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration
public @interface AcraLimiter {
    TimeUnit periodUnit() default TimeUnit.DAYS;

    long period() default 7;

    int overallLimit() default 25;

    int stacktraceLimit() default 3;

    int exceptionClassLimit() default 10;

    int failedReportLimit() default 5;

    @StringRes int resIgnoredCrashToast() default ACRAConstants.DEFAULT_RES_VALUE;
}
