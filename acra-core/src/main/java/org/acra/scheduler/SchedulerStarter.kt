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
package org.acra.scheduler

import android.content.Context
import org.acra.config.CoreConfiguration
import org.acra.file.ReportLocator
import org.acra.log.debug
import org.acra.log.warn
import org.acra.plugins.loadEnabled
import java.io.File

/**
 * @author F43nd1r
 * @since 18.04.18
 */
class SchedulerStarter(context: Context, config: CoreConfiguration) {
    private val locator = ReportLocator(context)
    val senderScheduler: SenderScheduler

    /**
     * Starts a process to start sending outstanding error reports.
     *
     * @param report                If not null, this report will be approved before scheduling.
     * @param onlySendSilentReports If true then only send silent reports.
     */
    fun scheduleReports(report: File?, onlySendSilentReports: Boolean) {
        if (report != null) {
            debug { "Mark ${report.name} as approved." }
            val approvedReport = File(locator.approvedFolder, report.name)
            if (!report.renameTo(approvedReport)) {
                warn { "Could not rename approved report from $report to $approvedReport" }
            }
        }
        debug { "Schedule report sending" }
        senderScheduler.scheduleReportSending(onlySendSilentReports)
    }

    init {
        val schedulerFactories: List<SenderSchedulerFactory> = config.pluginLoader.loadEnabled(config)
        if (schedulerFactories.isEmpty()) {
            senderScheduler = DefaultSenderScheduler(context, config)
        } else {
            senderScheduler = schedulerFactories[0].create(context, config)
            if (schedulerFactories.size > 1) warn { "More than one SenderScheduler found. Will use only " + senderScheduler.javaClass.simpleName }
        }
    }
}