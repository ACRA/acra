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
package org.acra.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import org.acra.ReportField
import org.acra.config.CoreConfiguration
import org.acra.file.BulkReportDeleter
import org.acra.file.CrashReportPersister
import org.acra.interaction.NotificationInteraction
import org.acra.log.debug
import org.acra.log.error
import org.acra.log.warn
import org.acra.scheduler.SchedulerStarter
import org.acra.sender.LegacySenderService
import org.acra.util.SystemServices.getNotificationManager
import org.json.JSONException
import java.io.File
import java.io.IOException

/**
 * @author F43nd1r
 * @since 15.09.2017
 */
class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val notificationManager = getNotificationManager(context)
            notificationManager.cancel(NotificationInteraction.NOTIFICATION_ID)
            if (intent.action != null) {
                when (intent.action) {
                    NotificationInteraction.INTENT_ACTION_SEND -> {
                        val reportFile: Any? = intent.getSerializableExtra(NotificationInteraction.EXTRA_REPORT_FILE)
                        val configObject: Any? = intent.getSerializableExtra(LegacySenderService.EXTRA_ACRA_CONFIG)
                        if (configObject is CoreConfiguration && reportFile is File) {
                            //Grab user comment from notification intent
                            val remoteInput = RemoteInput.getResultsFromIntent(intent)
                            if (remoteInput != null) {
                                val comment = remoteInput.getCharSequence(NotificationInteraction.KEY_COMMENT)
                                if (comment != null && "" != comment.toString()) {
                                    val persister = CrashReportPersister()
                                    try {
                                        debug { "Add user comment to $reportFile" }
                                        val crashData = persister.load(reportFile)
                                        crashData.put(ReportField.USER_COMMENT, comment.toString())
                                        persister.store(crashData, reportFile)
                                    } catch (e: IOException) {
                                        warn(e) { "User comment not added: " }
                                    } catch (e: JSONException) {
                                        warn(e) { "User comment not added: " }
                                    }
                                }
                            }
                            SchedulerStarter(context, configObject).scheduleReports(reportFile, false)
                        }
                    }
                    NotificationInteraction.INTENT_ACTION_DISCARD -> {
                        debug { "Discarding reports" }
                        BulkReportDeleter(context).deleteReports(false, 0)
                    }
                }
            }
        } catch (t: Exception) {
            error(t) { "Failed to handle notification action" }
        }
    }
}