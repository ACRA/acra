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

package org.acra.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.RemoteInput;

import org.acra.ACRA;
import org.acra.data.CrashReportData;
import org.acra.config.CoreConfiguration;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportPersister;
import org.acra.interaction.NotificationInteraction;
import org.acra.sender.SenderService;
import org.acra.sender.SenderServiceStarter;
import org.acra.util.SystemServices;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.USER_COMMENT;

/**
 * @author F43nd1r
 * @since 15.09.2017
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        try {
            final NotificationManager notificationManager = SystemServices.getNotificationManager(context);
            notificationManager.cancel(NotificationInteraction.NOTIFICATION_ID);
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case NotificationInteraction.INTENT_ACTION_SEND:
                        final Object reportFileObject = intent.getSerializableExtra(NotificationInteraction.EXTRA_REPORT_FILE);
                        final Object configObject = intent.getSerializableExtra(SenderService.EXTRA_ACRA_CONFIG);
                        if (configObject instanceof CoreConfiguration && reportFileObject instanceof File) {
                            final CoreConfiguration config = (CoreConfiguration) configObject;
                            final File reportFile = (File) reportFileObject;
                            //Grab user comment from notification intent
                            final Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                            if (remoteInput != null) {
                                final CharSequence comment = remoteInput.getCharSequence(NotificationInteraction.KEY_COMMENT);
                                if (comment != null && !"".equals(comment.toString())) {
                                    final CrashReportPersister persister = new CrashReportPersister();
                                    try {
                                        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Add user comment to " + reportFile);
                                        final CrashReportData crashData = persister.load(reportFile);
                                        crashData.put(USER_COMMENT, comment.toString());
                                        persister.store(crashData, reportFile);
                                    } catch (@NonNull IOException | JSONException e) {
                                        ACRA.log.w(LOG_TAG, "User comment not added: ", e);
                                    }
                                }
                            }
                            new SenderServiceStarter(context, config).startService(false, true);
                        }
                        break;
                    case NotificationInteraction.INTENT_ACTION_DISCARD:
                        if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Discarding reports");
                        new BulkReportDeleter(context).deleteReports(false, 0);
                        break;
                }
            }

        } catch (Throwable t) {
            ACRA.log.e(LOG_TAG, "Failed to handle notification action", t);
        }
    }
}
