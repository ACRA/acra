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
package org.acra.startup

import android.content.Context
import android.os.Handler
import org.acra.config.CoreConfiguration
import org.acra.file.CrashReportFileNameParser
import org.acra.file.ReportLocator
import org.acra.interaction.ReportInteractionExecutor
import org.acra.log.warn
import org.acra.plugins.loadEnabled
import org.acra.scheduler.SchedulerStarter
import java.util.*

/**
 * @author lukas
 * @since 15.09.18
 */
class StartupProcessorExecutor(private val context: Context, private val config: CoreConfiguration, private val schedulerStarter: SchedulerStarter) {
    private val reportLocator: ReportLocator = ReportLocator(context)
    private val fileNameParser: CrashReportFileNameParser = CrashReportFileNameParser()

    fun processReports(isAcraEnabled: Boolean) {
        val now = Calendar.getInstance()
        //application is not ready in onAttachBaseContext, so delay this. also run it on a background thread because we're doing disk I/O
        Handler(context.mainLooper).post {
            Thread {
                val reports = reportLocator.unapprovedReports.map { Report(it, false) } + reportLocator.approvedReports.map { Report(it, true) }
                config.pluginLoader.loadEnabled<StartupProcessor>(config).forEach { it.processReports(context, config, reports) }
                var send = false
                for (report in reports) {
                    // ignore reports that were just created for now, they might be handled in another thread
                    if (fileNameParser.getTimestamp(report.file.name).before(now)) {
                        when {
                            report.delete -> if (!report.file.delete()) warn { "Could not delete report ${report.file}" }
                            report.approved -> send = true
                            report.approve && isAcraEnabled -> {
                                    if (ReportInteractionExecutor(context, config).performInteractions(report.file)){
                                        schedulerStarter.scheduleReports(report.file, false)
                                    }
                                }
                            }
                        }
                    }
                }
                if (send && isAcraEnabled) {
                    schedulerStarter.scheduleReports(null, false)
                }
            }.start()
        }
    }

}