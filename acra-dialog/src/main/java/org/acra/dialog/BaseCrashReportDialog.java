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

package org.acra.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportPersister;
import org.acra.interaction.DialogInteraction;
import org.acra.sender.SenderServiceStarter;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

/**
 * Activity which implements the base functionality for a CrashReportDialog.
 * <p>
 * Activities which extend from this class can override init to create a custom view.
 * <p>
 * The methods sendCrash(comment, userEmail) and cancelReports() can be used to send or cancel
 * sending of reports respectively.
 * <p>
 * This Activity must be instantiated with 2 arguments:
 * <ol>
 * <li>{@link DialogInteraction#EXTRA_REPORT_FILE}</li>
 * <li>{@link DialogInteraction#EXTRA_REPORT_CONFIG}</li>
 * </ol>
 *
 * @author F43nd1r &amp; Various
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseCrashReportDialog extends FragmentActivity {

    private File reportFile;
    private CoreConfiguration config;

    /**
     * NB if you were previously creating and showing your dialog in this method,
     * you should move that code to {@link #init(Bundle)}.
     *
     * @param savedInstanceState If the activity is being re-initialized then this Bundle contains the data it most recently supplied in {@link #onSaveInstanceState}.
     */
    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        preInit(savedInstanceState);
        super.onCreate(savedInstanceState);


        if (ACRA.DEV_LOGGING) {
            ACRA.log.d(LOG_TAG, "CrashReportDialog extras=" + getIntent().getExtras());
        }

        final Serializable sConfig = getIntent().getSerializableExtra(DialogInteraction.EXTRA_REPORT_CONFIG);
        final Serializable sReportFile = getIntent().getSerializableExtra(DialogInteraction.EXTRA_REPORT_FILE);

        if ((sConfig instanceof CoreConfiguration) && (sReportFile instanceof File)) {
            config = (CoreConfiguration) sConfig;
            reportFile = (File) sReportFile;
            init(savedInstanceState);
        } else {
            ACRA.log.w(LOG_TAG, "Illegal or incomplete call of BaseCrashReportDialog.");
            finish();
        }
    }

    /**
     * Handle any necessary pre-onCreate() setup here.
     *
     * @param savedInstanceState If the activity is being re-initialized then this Bundle contains the data it most recently supplied in {@link #onSaveInstanceState}.
     */
    @SuppressWarnings("EmptyMethod")
    protected void preInit(@Nullable Bundle savedInstanceState) {
    }

    /**
     * Responsible for creating and showing the crash report dialog.
     *
     * @param savedInstanceState If the activity is being re-initialized then this Bundle contains the data it most recently supplied in {@link #onSaveInstanceState}.
     */
    protected void init(@Nullable Bundle savedInstanceState) {
    }


    /**
     * Cancel any pending crash reports.
     */
    protected final void cancelReports() {
        new Thread(() -> new BulkReportDeleter(this).deleteReports(false, 0)).start();
    }


    /**
     * Send crash report given user's comment and email address.
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the client.
     */
    protected final void sendCrash(@Nullable String comment, @Nullable String userEmail) {
        new Thread(() -> {
            final CrashReportPersister persister = new CrashReportPersister();
            try {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Add user comment to " + reportFile);
                final CrashReportData crashData = persister.load(reportFile);
                crashData.put(USER_COMMENT, comment == null ? "" : comment);
                crashData.put(USER_EMAIL, userEmail == null ? "" : userEmail);
                persister.store(crashData, reportFile);
            } catch (IOException | JSONException e) {
                ACRA.log.w(LOG_TAG, "User comment not added: ", e);
            }

            // Start the report sending task
            final SenderServiceStarter starter = new SenderServiceStarter(this, config);
            starter.startService(false, true);
        }).start();
    }

    /**
     * Provides the config to subclasses
     *
     * @return the main config
     */
    protected final CoreConfiguration getConfig() {
        return config;
    }
}
