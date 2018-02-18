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

package org.acra.config;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.builder.ReportBuilder;
import org.acra.data.CrashReportData;

/**
 * Controls if reports are sent
 *
 * @author F43nd1r
 * @since 26.10.2017
 */
@Keep
public interface ReportingAdministrator {
    /**
     * Determines if report collection should start
     *
     * @param context       a context
     * @param config        the current config
     * @param reportBuilder the reportBuilder for the report about to be collected
     * @return if this report should be collected
     */
    default boolean shouldStartCollecting(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder) {
        return true;
    }

    /**
     * Determines if a collected report should be sent
     *
     * @param context         a context
     * @param config          the current config
     * @param crashReportData the collected report
     * @return if this report should be sent
     */
    default boolean shouldSendReport(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull CrashReportData crashReportData) {
        return true;
    }

    /**
     * notifies the user about a dropped report
     *
     * @param context a context
     * @param config  the current config
     */
    default void notifyReportDropped(@NonNull Context context, @NonNull CoreConfiguration config) {
    }

    /**
     * Determines if the application should be killed
     *
     * @param context         a context
     * @param config          the current config
     * @param reportBuilder   the reportBuilder for the report about to be collected
     * @param crashReportData the collected report
     * @return if the application should be killed
     */
    default boolean shouldKillApplication(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @Nullable CrashReportData crashReportData) {
        return true;
    }

    /**
     * controls if this instance is active
     *
     * @param config the current config
     * @return if this instance should be called
     */
    default boolean enabled(@NonNull CoreConfiguration config) {
        return true;
    }
}
