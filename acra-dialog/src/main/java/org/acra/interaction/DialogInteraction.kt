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
package org.acra.interaction

import android.content.Context
import android.content.Intent
import com.google.auto.service.AutoService
import org.acra.ACRA
import org.acra.config.ConfigUtils.getPluginConfiguration
import org.acra.config.CoreConfiguration
import org.acra.config.DialogConfiguration
import org.acra.log.debug
import org.acra.plugins.HasConfigPlugin
import org.acra.prefs.SharedPreferencesFactory
import java.io.File

/**
 * @author F43nd1r
 * @since 02.06.2017
 */
@AutoService(ReportInteraction::class)
class DialogInteraction : HasConfigPlugin(DialogConfiguration::class.java), ReportInteraction {
    override fun performInteraction(context: Context, config: CoreConfiguration, reportFile: File): Boolean {
        if (SharedPreferencesFactory(context, config).create().getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {
            return true
        }
        // Create a new activity task with the confirmation dialog.
        // This new task will be persisted on application restart
        // right after its death.
        debug {  "Creating CrashReportDialog for $reportFile" }
        val dialogIntent = createCrashReportDialogIntent(context, config, reportFile)
        dialogIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(dialogIntent)
        return false
    }

    /**
     * Creates an Intent that can be used to create and show a CrashReportDialog.
     *
     * @param reportFile Error report file to display in the crash report dialog.
     */
    private fun createCrashReportDialogIntent(context: Context, config: CoreConfiguration, reportFile: File): Intent {
        debug {  "Creating DialogIntent for $reportFile" }
        val dialogIntent = Intent(context, getPluginConfiguration(config, DialogConfiguration::class.java).reportDialogClass)
        dialogIntent.putExtra(EXTRA_REPORT_FILE, reportFile)
        dialogIntent.putExtra(EXTRA_REPORT_CONFIG, config)
        return dialogIntent
    }

    companion object {
        /**
         * Used in the intent starting CrashReportDialog to provide the name of the
         * latest generated report file in order to be able to associate the user
         * comment.
         */
        const val EXTRA_REPORT_FILE = "REPORT_FILE"

        /**
         * Used in the intent starting CrashReportDialog to provide the AcraConfig to use when gathering the crash info.
         *
         *
         * This can be used by any BaseCrashReportDialog subclass to custom the dialog.
         */
        const val EXTRA_REPORT_CONFIG = "REPORT_CONFIG"
    }
}