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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.builder.ReportBuilder;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.NotificationConfiguration;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.receiver.DeleteBroadcastReceiver;
import org.acra.sender.SenderService;

import java.io.File;

/**
 * @author F43nd1r
 * @since 15.09.2017
 */

@AutoService(ReportInteraction.class)
public class NotificationInteraction implements ReportInteraction {
    private static final String CHANNEL = "ACRA";

    @Override
    public boolean performInteraction(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull File reportFile) {
        final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
        if (prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {
            return true;
        }
        final NotificationConfiguration notificationConfig = ConfigUtils.getSenderConfiguration(config, NotificationConfiguration.class);
        final NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL, context.getString(notificationConfig.resChannelName()), NotificationManager.IMPORTANCE_DEFAULT));
        }
        final Notification notification = new NotificationCompat.Builder(context, CHANNEL)
                .addAction(notificationConfig.resSendButtonIcon(), context.getString(notificationConfig.resSendButtonText()), getSendIntent(context, config))
                .addAction(notificationConfig.resDeleteButtonIcon(), context.getString(notificationConfig.resDeleteButtonText()), getDeleteIntent(context))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(notificationConfig.resTitle()))
                .setContentText(context.getString(notificationConfig.resText()))
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .build();
        notificationManager.notify(ACRAConstants.NOTIF_CRASH_ID, notification);
        return false;
    }

    private PendingIntent getSendIntent(@NonNull Context context, @NonNull CoreConfiguration config) {
        final Intent intent = new Intent(context, SenderService.class);
        intent.putExtra(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, false);
        intent.putExtra(SenderService.EXTRA_APPROVE_REPORTS_FIRST, true);
        intent.putExtra(SenderService.EXTRA_ACRA_CONFIG, config);
        return PendingIntent.getService(context, 999, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getDeleteIntent(@NonNull Context context) {
        final Intent intent = new Intent(context, DeleteBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 998, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
