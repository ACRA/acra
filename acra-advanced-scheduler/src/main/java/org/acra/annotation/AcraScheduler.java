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
import androidx.work.NetworkType;

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
    String EXTRA_APP_RESTARTED = "acra.restarted";
    /**
     * Network constraint for report sending
     *
     * @return networkType required to allow report sending
     * @since 5.2.0
     */
    @NonNull NetworkType requiresNetworkType() default NetworkType.NOT_REQUIRED;

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

    /**
     * Restarts the last activity immediately after a crash.
     * If an activity is restarted, the {@link org.acra.annotation.AcraScheduler#EXTRA_APP_RESTARTED} extra will contain a boolean true.
     * Note that this might interact badly with the crash dialog.
     *
     * @return if acra should attempt to restart the app after a crash
     * @since 5.2.0
     */
    boolean restartAfterCrash() default false;
}
