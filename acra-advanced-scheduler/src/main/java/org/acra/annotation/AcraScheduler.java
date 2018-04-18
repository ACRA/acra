/*
 * Copyright (c) 2018
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
import com.evernote.android.job.JobRequest;

import java.lang.annotation.*;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration
public @interface AcraScheduler {
    /**
     * Network constraint for report sending
     *
     * @return networkType required to allow report sending
     * @since 5.2.0
     */
    @NonNull JobRequest.NetworkType requiresNetworkType() default JobRequest.NetworkType.ANY;

    /**
     * Charging constraint for report sending
     *
     * @return if reports should only be sent while charging
     * @since 5.2.0
     */
    boolean requiresCharging() default false;

    /**
     * Idle constraint for report sending
     *
     * @return if reports should only be sent while the device is idle
     * @since 5.2.0
     */
    boolean requiresDeviceIdle() default false;

    /**
     * Battery constraint for report sending
     *
     * @return if reports should only be sent while battery isn't low
     * @since 5.2.0
     */
    boolean requiresBatteryNotLow() default false;
}
