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
import android.os.Bundle;
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
 * Does all the work for dialog implementations
 *
 * @author F43nd1r
 * @since 4.9.1
 */
class CrashReportDelegate implements ICrashReportDialog {

    private final Context context;
    private File reportFile;
    private ACRAConfiguration config;
    private Throwable exception;

    CrashReportDelegate(Context context) {
        this.context = context;
    }

    /**
     * Initializes this instance with data from the intent
     *
     * @param intent Intent which started the dialog
     * @return if the activity should continue
     */
    final boolean loadFromIntent(@NonNull Intent intent) {


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
        } else if ((sConfig instanceof ACRAConfiguration) && (sReportFile instanceof File) && ((sException instanceof Throwable) || sException == null)) {
            config = (ACRAConfiguration) sConfig;
            reportFile = (File) sReportFile;
            exception = (Throwable) sException;
            return true;
        } else {
            ACRA.log.w(LOG_TAG, "Illegal or incomplete call of BaseCrashReportDialog.");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        //no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void cancelReports() {
        new BulkReportDeleter(context).deleteReports(false, 0);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final void sendCrash(@Nullable String comment, @Nullable String userEmail) {
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
        final SenderServiceStarter starter = new SenderServiceStarter(context, config);
        starter.startService(false, true);

        // Optional Toast to thank the user
        final int toastId = config.resDialogOkToast();
        if (toastId != 0) {
            ToastSender.sendToast(context, toastId, Toast.LENGTH_LONG);
        }
    }

    @Override
    public final ACRAConfiguration getConfig() {
        return config;
    }

    @Override
    public final Throwable getException() {
        return exception;
    }
}
