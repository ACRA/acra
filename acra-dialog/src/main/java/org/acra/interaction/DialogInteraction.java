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

package org.acra.interaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.config.ConfigUtils;
import org.acra.config.DialogConfiguration;
import org.acra.prefs.SharedPreferencesFactory;

import java.io.File;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 02.06.2017
 */
@AutoService(ReportInteraction.class)
public class DialogInteraction implements ReportInteraction {
    @Override
    public boolean performInteraction(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull File reportFile) {
        final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
        if(prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)){
            return true;
        }
        // Create a new activity task with the confirmation dialog.
        // This new task will be persisted on application restart
        // right after its death.
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Creating CrashReportDialog for " + reportFile);
        final Intent dialogIntent = createCrashReportDialogIntent(context, config, reportFile, reportBuilder);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(dialogIntent);
        return false;
    }

    /**
     * Creates an Intent that can be used to create and show a CrashReportDialog.
     *
     * @param reportFile        Error report file to display in the crash report dialog.
     * @param reportBuilder     ReportBuilder containing the details of the crash.
     */
    @NonNull
    private Intent createCrashReportDialogIntent(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull File reportFile, @NonNull ReportBuilder reportBuilder) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Creating DialogIntent for " + reportFile + " exception=" + reportBuilder.getException());
        final Intent dialogIntent = new Intent(context, ConfigUtils.getSenderConfiguration(config, DialogConfiguration.class).reportDialogClass());
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_FILE, reportFile);
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_EXCEPTION, reportBuilder.getException());
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_CONFIG, config);
        return dialogIntent;
    }
}
