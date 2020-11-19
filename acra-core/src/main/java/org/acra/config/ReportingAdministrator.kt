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
package org.acra.config

import android.content.Context
import org.acra.builder.LastActivityManager
import org.acra.builder.ReportBuilder
import org.acra.data.CrashReportData
import org.acra.plugins.Plugin

/**
 * Controls if reports are sent
 *
 * @author F43nd1r
 * @since 26.10.2017
 */
interface ReportingAdministrator : Plugin {
    /**
     * Determines if report collection should start
     *
     * @param context       a context
     * @param config        the current config
     * @param reportBuilder the reportBuilder for the report about to be collected
     * @return if this report should be collected
     */
    fun shouldStartCollecting(context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder): Boolean {
        return true
    }

    /**
     * Determines if a collected report should be sent
     *
     * @param context         a context
     * @param config          the current config
     * @param crashReportData the collected report
     * @return if this report should be sent
     */
    fun shouldSendReport(context: Context, config: CoreConfiguration, crashReportData: CrashReportData): Boolean {
        return true
    }

    /**
     * notifies the user about a dropped report
     *
     * @param context a context
     * @param config  the current config
     */
    fun notifyReportDropped(context: Context, config: CoreConfiguration) {}
    fun shouldFinishActivity(context: Context, config: CoreConfiguration, lastActivityManager: LastActivityManager?): Boolean {
        return true
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
    fun shouldKillApplication(context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder,
                              crashReportData: CrashReportData?): Boolean {
        return true
    }
}