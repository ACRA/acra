/*
 * Copyright (c) 2019
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
package org.acra.dialog

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.file.BulkReportDeleter
import org.acra.file.CrashReportPersister
import org.acra.interaction.DialogInteraction
import org.acra.log.debug
import org.acra.log.error
import org.acra.log.warn
import org.acra.scheduler.SchedulerStarter
import org.json.JSONException
import java.io.File
import java.io.IOException

/**
 * Use this class to integrate your custom crash report dialog with ACRA.
 *
 * Call this in your [android.app.Activity.onCreate].
 * The intent must contain two extras:
 *
 *  1. [DialogInteraction.EXTRA_REPORT_FILE]
 *  1. [DialogInteraction.EXTRA_REPORT_CONFIG]
 *
 *
 * @param context a context
 * @param intent  the intent which started this activity
 * @throws IllegalArgumentException if the intent cannot be parsed or does not contain the correct data
 * @author f43nd1r
 * @since 5.4.0
 */
class CrashReportDialogHelper(private val context: Context, intent: Intent) {
    private val reportFile: File

    /**
     * Provides the configuration
     *
     * @return the main config
     */
    val config: CoreConfiguration

    init {
        val sConfig = intent.getSerializableExtra(DialogInteraction.EXTRA_REPORT_CONFIG)
        val sReportFile = intent.getSerializableExtra(DialogInteraction.EXTRA_REPORT_FILE)
        if (sConfig is CoreConfiguration && sReportFile is File) {
            config = sConfig
            reportFile = sReportFile
        } else {
            error { "Illegal or incomplete call of " + javaClass.simpleName }
            throw IllegalArgumentException()
        }
    }

    /**
     * loads the current report data
     *
     * @return report data
     * @throws IOException if there was a problem with the report file
     */
    @get:Throws(IOException::class)
    @get:WorkerThread
    val reportData: CrashReportData by lazy {
        try {
            CrashReportPersister().load(reportFile)
        } catch (e: JSONException) {
            throw IOException(e)
        }
    }

    /**
     * Cancel any pending crash reports.
     */
    fun cancelReports() {
        Thread { BulkReportDeleter(context).deleteReports(false, 0) }.start()
    }

    /**
     * Send crash report given user's comment and email address.
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the user.
     */
    fun sendCrash(comment: String?, userEmail: String?) {
        Thread {
            try {
                debug {  "Add user comment to $reportFile"}
                val crashData = reportData
                crashData.put(ReportField.USER_COMMENT, comment ?: "")
                crashData.put(ReportField.USER_EMAIL, userEmail ?: "")
                CrashReportPersister().store(crashData, reportFile)
            } catch (e: IOException) {
                warn(e) {  "User comment not added: "}
            } catch (e: JSONException) {
                warn(e) {  "User comment not added: "}
            }

            // Start the report sending task
            SchedulerStarter(context, config).scheduleReports(reportFile, false)
        }.start()
    }
}