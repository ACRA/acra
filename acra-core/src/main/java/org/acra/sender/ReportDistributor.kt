/*
 *  Copyright 2012 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.sender

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import org.acra.config.CoreConfiguration
import org.acra.config.DefaultRetryPolicy
import org.acra.config.RetryPolicy.FailedSender
import org.acra.data.CrashReportData
import org.acra.file.CrashReportPersister
import org.acra.log.debug
import org.acra.log.info
import org.acra.log.warn
import org.acra.util.IOUtils
import org.acra.util.InstanceCreator
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Distributes reports to all Senders.
 *
 * @param context       ApplicationContext in which the reports are being sent.
 * @param config        Configuration to use while sending.
 * @param reportSenders List of ReportSender to use to send the crash reports.
 * @param extras        additional information set in a [org.acra.scheduler.DefaultSenderScheduler]
 *
 * @author William Ferguson
 * @since 4.8.0
 */
internal class ReportDistributor(private val context: Context, private val config: CoreConfiguration, private val reportSenders: List<ReportSender>, private val extras: Bundle) {
    /**
     * Send report via all senders.
     *
     * @param reportFile Report to send.
     * @return if distributing was successful
     */
    fun distribute(reportFile: File): Boolean {
        info { "Sending report $reportFile" }
        try {
            val persister = CrashReportPersister()
            val previousCrashReport = persister.load(reportFile)
            sendCrashReport(previousCrashReport)
            IOUtils.deleteFile(reportFile)
            return true
        } catch (e: RuntimeException) {
            org.acra.log.error(e) { "Failed to send crash reports for $reportFile" }
            IOUtils.deleteFile(reportFile)
        } catch (e: IOException) {
            org.acra.log.error(e) { "Failed to send crash reports for $reportFile" }
            IOUtils.deleteFile(reportFile)
        } catch (e: JSONException) {
            org.acra.log.error(e) { "Failed to send crash reports for $reportFile" }
            IOUtils.deleteFile(reportFile)
        } catch (e: ReportSenderException) {
            org.acra.log.error(e) { "Failed to send crash reports for $reportFile" }
            // An issue occurred while sending this report but we can still try to
            // send other reports. Report sending is limited by ACRAConstants.MAX_SEND_REPORTS
            // so there's not much to fear about overloading a failing server.
        }
        return false
    }

    /**
     * Sends the report with all configured ReportSenders. If at least one
     * sender completed its job, the report is considered as sent and will not
     * be sent again for failing senders.
     *
     * @param errorContent Crash data.
     * @throws ReportSenderException if unable to send the crash report.
     */
    @Throws(ReportSenderException::class)
    private fun sendCrashReport(errorContent: CrashReportData) {
        if (!isDebuggable || config.sendReportsInDevMode) {
            val failedSenders: MutableList<FailedSender> = LinkedList()
            for (sender in reportSenders) {
                try {
                    debug { "Sending report using " + sender.javaClass.name }
                    sender.send(context, errorContent, extras)
                    debug { "Sent report using " + sender.javaClass.name }
                } catch (e: ReportSenderException) {
                    failedSenders.add(FailedSender(sender, e))
                }
            }
            when {
                failedSenders.isEmpty() -> debug { "Report was sent by all senders" }
                InstanceCreator.create(config.retryPolicyClass) { DefaultRetryPolicy() }.shouldRetrySend(reportSenders, failedSenders) -> throw ReportSenderException(
                        "Policy marked this task as incomplete. ACRA will try to send this report again.", failedSenders[0].exception)
                else -> warn {
                    val builder = StringBuilder("ReportSenders of classes [")
                    for (failedSender in failedSenders) {
                        builder.append(failedSender.sender.javaClass.name)
                        builder.append(", ")
                    }
                    builder.append("] failed, but Policy marked this task as complete. ACRA will not send this report again.")
                    builder.toString()
                }
            }
        }
    }

    /**
     * Returns true if the application is debuggable.
     *
     * @return true if the application is debuggable.
     */
    private val isDebuggable: Boolean
        get() {
            return try {
                context.packageManager.getApplicationInfo(context.packageName, 0).flags and ApplicationInfo.FLAG_DEBUGGABLE > 0
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
}