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

import android.support.annotation.NonNull;
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
 * Limiter configuration
 *
 * @author F43nd1r
 * @since 26.10.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration
public @interface AcraLimiter {
    /**
     * Unit of {@link org.acra.annotation.AcraLimiter#period()}
     *
     * @return a time unit
     * @since 5.0.0
     */
    @NonNull TimeUnit periodUnit() default TimeUnit.DAYS;

    /**
     * Reports which have been collected before this will not be considered for any limits except {@link org.acra.annotation.AcraLimiter#failedReportLimit()}
     *
     * @return number of {@link org.acra.annotation.AcraLimiter#periodUnit()}s in which to limit reports
     * @since 5.0.0
     */
    long period() default 7;

    /**
     * general limit of reports
     *
     * @return maximum number of reports per period
     * @since 5.0.0
     */
    int overallLimit() default 25;

    /**
     * limit for reports with the same stacktrace
     *
     * @return maximum number of reports with the same stacktrace per period
     * @since 5.0.0
     */
    int stacktraceLimit() default 3;

    /**
     * limit for reports with the same exception class
     *
     * @return maximum number of reports with the same exception class per period
     * @since 5.0.0
     */
    int exceptionClassLimit() default 10;

    /**
     * limit for unsent reports
     *
     * @return maximum number of unsent reports to keep
     * @since 5.0.0
     */
    int failedReportLimit() default 5;

    /**
     * toast shown when a report was not collected or sent because a limit was exceeded
     *
     * @return Resource id for the toast shown when a crash was ignored
     * @since 5.0.0
     */
    @StringRes int resIgnoredCrashToast() default ACRAConstants.DEFAULT_RES_VALUE;
}
