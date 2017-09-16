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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.builder.ReportBuilder;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.NotificationConfiguration;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.receiver.NotificationBroadcastReceiver;
import org.acra.sender.SenderService;

import java.io.File;

/**
 * @author F43nd1r
 * @since 15.09.2017
 */

@AutoService(ReportInteraction.class)
public class NotificationInteraction implements ReportInteraction {
    public static final String INTENT_ACTION_SEND = "org.acra.intent.send";
    public static final String INTENT_ACTION_DISCARD = "org.acra.intent.discard";
    public static final String KEY_COMMENT = "comment";
    public static final String EXTRA_REPORT_FILE = "REPORT_FILE";
    public static final int NOTIFICATION_ID = 666;
    private static final int ACTION_SEND = 667;
    private static final int ACTION_DISCARD = 668;
    private static final String CHANNEL = "ACRA";

    @Override
    public boolean performInteraction(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull File reportFile) {
        final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
        if (prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {
            return true;
        }
        final NotificationConfiguration notificationConfig = ConfigUtils.getSenderConfiguration(config, NotificationConfiguration.class);
        final NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        //We have to create a channel on Oreo+, because notifications without one aren't allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(CHANNEL, context.getString(notificationConfig.resChannelName()), notificationConfig.resChannelImportance());
            if (notificationConfig.resChannelDescription() != ACRAConstants.DEFAULT_RES_VALUE) {
                channel.setDescription(context.getString(notificationConfig.resChannelDescription()));
            }
            notificationManager.createNotificationChannel(channel);
        }
        //configure base notification
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(notificationConfig.resTitle()))
                .setContentText(context.getString(notificationConfig.resText()))
                .setSmallIcon(notificationConfig.resIcon());
        //add ticker if set
        if (notificationConfig.resTickerText() != ACRAConstants.DEFAULT_RES_VALUE) {
            notification.setTicker(context.getString(notificationConfig.resTickerText()));
        }
        final PendingIntent sendIntent = getSendIntent(context, config, reportFile);
        final PendingIntent discardIntent = getDiscardIntent(context);
        final NotificationCompat.Action.Builder send = new NotificationCompat.Action.Builder(notificationConfig.resSendButtonIcon(), context.getString(notificationConfig.resSendButtonText()), sendIntent);
        if (notificationConfig.resCommentPrompt() != ACRAConstants.DEFAULT_RES_VALUE) {
            send.addRemoteInput(new RemoteInput.Builder(KEY_COMMENT).setLabel(context.getString(notificationConfig.resCommentPrompt())).build());
        }
        //add actions. On old devices we have no notification buttons, so we have to set the intents to the only possible interactions: click and swipe
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.addAction(send.build()).addAction(notificationConfig.resDiscardButtonIcon(), context.getString(notificationConfig.resDiscardButtonText()), discardIntent);
        } else {
            notification.setContentIntent(sendIntent).setDeleteIntent(discardIntent);
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build());
        return false;
    }

    private PendingIntent getSendIntent(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull File reportFile) {
        final Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(INTENT_ACTION_SEND);
        intent.putExtra(SenderService.EXTRA_ACRA_CONFIG, config);
        intent.putExtra(EXTRA_REPORT_FILE, reportFile);
        return PendingIntent.getBroadcast(context, ACTION_SEND, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getDiscardIntent(@NonNull Context context) {
        final Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(INTENT_ACTION_DISCARD);
        return PendingIntent.getBroadcast(context, ACTION_DISCARD, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
