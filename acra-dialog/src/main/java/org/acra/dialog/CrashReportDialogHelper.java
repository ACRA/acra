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

package org.acra.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportPersister;
import org.acra.interaction.DialogInteraction;
import org.acra.scheduler.SchedulerStarter;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

/**
 * Use this class to integrate your custom crash report dialog with ACRA.
 * @author f43nd1r
 * @since 5.4.0
 */
public class CrashReportDialogHelper {

    private final File reportFile;
    private final CoreConfiguration config;
    private final Context context;
    private CrashReportData reportData;

    /**
     * Call this in your {@link android.app.Activity#onCreate(Bundle)}.
     * The intent must contain two extras:
     * <ol>
     * <li>{@link DialogInteraction#EXTRA_REPORT_FILE}</li>
     * <li>{@link DialogInteraction#EXTRA_REPORT_CONFIG}</li>
     * </ol>
     *
     * @param context a context
     * @param intent  the intent which started this activity
     * @throws IllegalArgumentException if the intent cannot be parsed or does not contain the correct data
     */
    public CrashReportDialogHelper(@NonNull Context context, @NonNull Intent intent) throws IllegalArgumentException {
        this.context = context;
        final Serializable sConfig = intent.getSerializableExtra(DialogInteraction.EXTRA_REPORT_CONFIG);
        final Serializable sReportFile = intent.getSerializableExtra(DialogInteraction.EXTRA_REPORT_FILE);

        if ((sConfig instanceof CoreConfiguration) && (sReportFile instanceof File)) {
            config = (CoreConfiguration) sConfig;
            reportFile = (File) sReportFile;
        } else {
            ACRA.log.w(LOG_TAG, "Illegal or incomplete call of " + getClass().getSimpleName());
            throw new IllegalArgumentException();
        }
    }

    /**
     * loads the current report data
     *
     * @return report data
     * @throws IOException if there was a problem with the report file
     */
    @WorkerThread
    @NonNull
    public CrashReportData getReportData() throws IOException {
        if (reportData == null) {
            try {
                reportData = new CrashReportPersister().load(reportFile);
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
        return reportData;
    }


    /**
     * Cancel any pending crash reports.
     */
    public void cancelReports() {
        new Thread(() -> new BulkReportDeleter(context).deleteReports(false, 0)).start();
    }


    /**
     * Send crash report given user's comment and email address.
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the user.
     */
    public void sendCrash(@Nullable String comment, @Nullable String userEmail) {
        new Thread(() -> {
            try {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Add user comment to " + reportFile);
                final CrashReportData crashData = getReportData();
                crashData.put(USER_COMMENT, comment == null ? "" : comment);
                crashData.put(USER_EMAIL, userEmail == null ? "" : userEmail);
                new CrashReportPersister().store(crashData, reportFile);
            } catch (IOException | JSONException e) {
                ACRA.log.w(LOG_TAG, "User comment not added: ", e);
            }

            // Start the report sending task
            new SchedulerStarter(context, config).scheduleReports(reportFile, false);
        }).start();
    }

    /**
     * Provides the configuration
     *
     * @return the main config
     */
    public CoreConfiguration getConfig() {
        return config;
    }
}
