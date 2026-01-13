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
package org.acra.builder

import android.content.Context
import android.os.Debug
import android.os.Looper
import android.os.StrictMode
import android.widget.Toast
import org.acra.ACRAConstants
import org.acra.ReportField
import org.acra.config.CoreConfiguration
import org.acra.config.ReportingAdministrator
import org.acra.data.CrashReportData
import org.acra.data.CrashReportDataFactory
import org.acra.file.CrashReportPersister
import org.acra.file.ReportLocator
import org.acra.interaction.ReportInteractionExecutor
import org.acra.log.debug
import org.acra.log.error
import org.acra.log.info
import org.acra.log.warn
import org.acra.plugins.loadEnabled
import org.acra.scheduler.SchedulerStarter
import org.acra.util.ProcessFinisher
import org.acra.util.ToastSender.sendToast
import java.io.File
import java.lang.RuntimeException

/**
 * Collates, records and initiates the sending of a report.
 *
 * @since 4.8.0
 */
class ReportExecutor(private val context: Context, private val config: CoreConfiguration, private val crashReportDataFactory: CrashReportDataFactory,
        // A reference to the system's previous default UncaughtExceptionHandler
        // kept in order to execute the default exception handling after sending the report.
                     private val defaultExceptionHandler: Thread.UncaughtExceptionHandler?, private val processFinisher: ProcessFinisher,
                     private val schedulerStarter: SchedulerStarter,
                     private val lastActivityManager: LastActivityManager) {
    private val reportingAdministrators: List<ReportingAdministrator> = config.pluginLoader.loadEnabled(config)
    var isEnabled = false

    /**
     * pass-through to default handler
     *
     * @param t the crashed thread
     * @param e the uncaught exception
     */
    fun handReportToDefaultExceptionHandler(t: Thread, e: Throwable) {
        if (defaultExceptionHandler != null) {
            info { "ACRA is disabled for " + context.packageName + " - forwarding uncaught Exception on to default ExceptionHandler" }
            defaultExceptionHandler.uncaughtException(t, e)
        } else {
            error { "ACRA is disabled for ${context.packageName} - no default ExceptionHandler" }
            error(e) { "ACRA caught a ${e.javaClass.simpleName} for ${context.packageName}" }
        }
    }

    /**
     * Try to create a report. Also starts [LegacySenderService]
     *
     * @param reportBuilder The report builder used to assemble the report
     */
    fun execute(reportBuilder: ReportBuilder) {
        if (!isEnabled) {
            warn { "ACRA is disabled. Report not sent." }
            return
        }
        var blockingAdministrator: ReportingAdministrator? = null
        for (administrator in reportingAdministrators) {
            try {
                if (!administrator.shouldStartCollecting(context, config, reportBuilder)) {
                    blockingAdministrator = administrator
                }
            } catch (t: Exception) {
                warn(t) { "ReportingAdministrator ${administrator.javaClass.name} threw exception" }
            }
        }
        val crashReportData: CrashReportData?
        if (blockingAdministrator == null) {
            crashReportData = crashReportDataFactory.createCrashData(reportBuilder)
            for (administrator in reportingAdministrators) {
                try {
                    if (!administrator.shouldSendReport(context, config, crashReportData)) {
                        blockingAdministrator = administrator
                    }
                } catch (t: Exception) {
                    warn(t) { "ReportingAdministrator ${administrator.javaClass.name} threw exception" }
                }
            }
        } else {
            crashReportData = null
            debug { "Not collecting crash report because of ReportingAdministrator " + blockingAdministrator.javaClass.name }
        }
        if (reportBuilder.isEndApplication) {
            var finishActivity = true
            for (administrator in reportingAdministrators) {
                try {
                    if (!administrator.shouldFinishActivity(context, config, lastActivityManager)) {
                        finishActivity = false
                    }
                } catch (t: Exception) {
                    warn(t) { "ReportingAdministrator " + administrator.javaClass.name + " threw exception" }
                }
            }
            if (finishActivity) {
                // Finish the last activity early to prevent restarts on android 7+
                processFinisher.finishLastActivity(reportBuilder.uncaughtExceptionThread)
            }
        }
        if (blockingAdministrator == null) {
            val oldPolicy = StrictMode.allowThreadDiskWrites()
            val reportFile = getReportFileName(crashReportData!!)
            saveCrashReportFile(reportFile, crashReportData)
            val executor = ReportInteractionExecutor(context, config)
            if (reportBuilder.isSendSilently) {
                //if no interactions are present we can send all reports
                sendReport(reportFile, executor.hasInteractions())
            } else {
                if (executor.performInteractions(reportFile)) {
                    sendReport(reportFile, false)
                }
            }
            StrictMode.setThreadPolicy(oldPolicy)
        } else {
            debug { "Not sending crash report because of ReportingAdministrator ${blockingAdministrator.javaClass.name}" }
            try {
                blockingAdministrator.notifyReportDropped(context, config)
            } catch (t: Exception) {
                warn(t) { "ReportingAdministrator ${blockingAdministrator.javaClass.name} threw exeption" }
            }
        }
        debug { "Wait for Interactions + worker ended. Kill Application ? ${reportBuilder.isEndApplication}" }
        if (reportBuilder.isEndApplication) {
            var endApplication = true
            for (administrator in reportingAdministrators) {
                try {
                    if (!administrator.shouldKillApplication(context, config, reportBuilder, crashReportData)) {
                        endApplication = false
                    }
                } catch (t: Exception) {
                    warn(t) { "ReportingAdministrator ${administrator.javaClass.name} threw exception" }
                }
            }
            if (endApplication) {
                if (Debug.isDebuggerConnected()) {
                    //Killing a process with a debugger attached would kill the whole application including our service, so we can't do that.
                    val warning = "Warning: Acra may behave differently with a debugger attached"
                    Thread {
                        Looper.prepare()
                        sendToast(context, warning, Toast.LENGTH_LONG)
                        Looper.loop()
                    }.start()
                    warn { warning }
                } else {
                    endApplication(reportBuilder.uncaughtExceptionThread, reportBuilder.exception?: RuntimeException())
                }
            }
        }
    }

    /**
     * End the application.
     */
    private fun endApplication(uncaughtExceptionThread: Thread?, th: Throwable) {
        val letDefaultHandlerEndApplication: Boolean = config.alsoReportToAndroidFramework
        if (uncaughtExceptionThread != null && letDefaultHandlerEndApplication && defaultExceptionHandler != null) {
            // Let the system default handler do it's job and display the force close dialog.
            debug { "Handing Exception on to default ExceptionHandler" }
            defaultExceptionHandler.uncaughtException(uncaughtExceptionThread, th)
        } else {
            processFinisher.endApplication()
        }
    }

    /**
     * Starts a Process to start sending outstanding error reports.
     *
     * @param onlySendSilentReports If true then only send silent reports.
     */
    private fun sendReport(report: File, onlySendSilentReports: Boolean) {
        if (isEnabled) {
            schedulerStarter.scheduleReports(report, onlySendSilentReports)
        } else {
            warn { "Would be sending reports, but ACRA is disabled" }
        }
    }

    private fun getReportFileName(crashData: CrashReportData): File {
        val timestamp = crashData.getString(ReportField.USER_CRASH_DATE)
        val isSilent = crashData.getString(ReportField.IS_SILENT)
        val fileName = timestamp + (if (java.lang.Boolean.parseBoolean(isSilent)) ACRAConstants.SILENT_SUFFIX else "") + ACRAConstants.REPORTFILE_EXTENSION
        val reportLocator = ReportLocator(context)
        return File(reportLocator.unapprovedFolder, fileName)
    }

    /**
     * Store a report
     *
     * @param file      the file to store in
     * @param crashData the content
     */
    private fun saveCrashReportFile(file: File, crashData: CrashReportData) {
        try {
            debug { "Writing crash report file $file" }
            CrashReportPersister().store(crashData, file)
        } catch (e: Exception) {
            error(e) { "An error occurred while writing the report file..." }
        }
    }

}