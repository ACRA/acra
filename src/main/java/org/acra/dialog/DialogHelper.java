/*
 *  Copyright 2016
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
package org.acra.dialog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportPersister;
import org.acra.sender.SenderServiceStarter;
import org.acra.util.ToastSender;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

/**
 * Helps to load a CrashReportDialog. You normally don't use this directly but instead extend from {@link BaseCrashReportDialog}
 *
 * @author F43nd1r
 * @since 4.9.0
 */
public final class DialogHelper {

    private final Context context;
    private File reportFile;
    private ACRAConfiguration config;
    private Throwable exception;

    public DialogHelper(Context context) {
        this.context = context;
    }

    /**
     * Call this in your onCreate. Finish your activity if this returns false or throws an exception.
     *
     * @param intent the intent that started the activity
     * @return if the activity should continue to initialize. Returns false if the intent requests to cancel all reports
     * @throws InvalidIntentException
     */
    public boolean loadFrom(@NonNull Intent intent) throws InvalidIntentException {
        if (ACRA.DEV_LOGGING) {
            ACRA.log.d(LOG_TAG, "CrashReportDialog extras=" + intent.getExtras());
        }

        final Serializable sConfig = intent.getSerializableExtra(ACRAConstants.EXTRA_REPORT_CONFIG);
        final Serializable sReportFile = intent.getSerializableExtra(ACRAConstants.EXTRA_REPORT_FILE);
        final Serializable sException = intent.getSerializableExtra(ACRAConstants.EXTRA_REPORT_EXCEPTION);
        final boolean forceCancel = intent.getBooleanExtra(ACRAConstants.EXTRA_FORCE_CANCEL, false);

        if (forceCancel) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Forced reports deletion.");
            cancelReports();
            return false;
        } else if ((sConfig instanceof ACRAConfiguration) && (sReportFile instanceof File) && (sException instanceof Throwable)) {
            config = (ACRAConfiguration) sConfig;
            reportFile = (File) sReportFile;
            exception = (Throwable) sException;
            return true;
        } else {
            throw new InvalidIntentException("Illegal or incomplete call of BaseCrashReportDialog.");
        }
    }


    /**
     * Cancel any pending crash reports.
     */
    public void cancelReports() {
        new BulkReportDeleter(context.getApplicationContext()).deleteReports(false, 0);
    }


    /**
     * Send crash report given user's comment and email address. If none should be empty strings
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the client.
     */
    public void sendCrash(@Nullable String comment, @Nullable String userEmail) {
        if (reportFile == null) {
            ACRA.log.w(LOG_TAG, "You can't call sendCrash if loadFrom wasn't called or didn't return true.");
            return;
        }
        final CrashReportPersister persister = new CrashReportPersister();
        try {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Add user comment to " + reportFile);
            final CrashReportData crashData = persister.load(reportFile);
            crashData.put(USER_COMMENT, comment == null ? "" : comment);
            crashData.put(USER_EMAIL, userEmail == null ? "" : userEmail);
            persister.store(crashData, reportFile);
        } catch (IOException e) {
            ACRA.log.w(LOG_TAG, "User comment not added: ", e);
        }

        // Start the report sending task
        final SenderServiceStarter starter = new SenderServiceStarter(context.getApplicationContext(), config);
        starter.startService(false, true);

        // Optional Toast to thank the user
        final int toastId = config.resDialogOkToast();
        if (toastId != 0) {
            ToastSender.sendToast(context.getApplicationContext(), toastId, Toast.LENGTH_LONG);
        }
    }

    /**
     * @return ACRAs configuration
     */
    public ACRAConfiguration getConfig() {
        return config;
    }

    /**
     * @return the exception, if any
     */
    @Nullable
    public Throwable getException() {
        return exception;
    }

}
