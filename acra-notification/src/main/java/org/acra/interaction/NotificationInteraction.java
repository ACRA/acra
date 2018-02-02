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
import android.widget.RemoteViews;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.NotificationConfiguration;
import org.acra.notification.R;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.receiver.NotificationBroadcastReceiver;
import org.acra.sender.SenderService;

import java.io.File;

/**
 * @author F43nd1r
 * @since 15.09.2017
 */

@AutoService(ReportInteraction.class)
public class NotificationInteraction extends BaseReportInteraction {
    public static final String INTENT_ACTION_SEND = "org.acra.intent.send";
    public static final String INTENT_ACTION_DISCARD = "org.acra.intent.discard";
    public static final String KEY_COMMENT = "comment";
    public static final String EXTRA_REPORT_FILE = "REPORT_FILE";
    public static final int NOTIFICATION_ID = 666;
    private static final int ACTION_SEND = 667;
    private static final int ACTION_DISCARD = 668;
    private static final String CHANNEL = "ACRA";

    public NotificationInteraction() {
        super(NotificationConfiguration.class);
    }

    @Override
    public boolean performInteraction(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull File reportFile) {
        final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
        if (prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {
            return true;
        }
        final NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        //can't post notifications
        if (notificationManager == null) {
            return true;
        }
        final NotificationConfiguration notificationConfig = ConfigUtils.getPluginConfiguration(config, NotificationConfiguration.class);
        //We have to create a channel on Oreo+, because notifications without one aren't allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(CHANNEL, notificationConfig.channelName(), notificationConfig.resChannelImportance());
            channel.setSound(null, null);
            if (notificationConfig.channelDescription() != null) {
                channel.setDescription(notificationConfig.channelDescription());
            }
            notificationManager.createNotificationChannel(channel);
        }
        //configure base notification
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(notificationConfig.title())
                .setContentText(notificationConfig.text())
                .setSmallIcon(notificationConfig.resIcon())
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        //add ticker if set
        if (notificationConfig.tickerText() != null) {
            notification.setTicker(notificationConfig.tickerText());
        }
        final PendingIntent sendIntent = getSendIntent(context, config, reportFile);
        final PendingIntent discardIntent = getDiscardIntent(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && notificationConfig.sendWithCommentButtonText() != null) {
            final RemoteInput.Builder remoteInput = new RemoteInput.Builder(KEY_COMMENT);
            if (notificationConfig.commentPrompt() != null) {
                remoteInput.setLabel(notificationConfig.commentPrompt());
            }
            notification.addAction(new NotificationCompat.Action.Builder(notificationConfig.resSendWithCommentButtonIcon(), notificationConfig.sendWithCommentButtonText(), sendIntent)
                    .addRemoteInput(remoteInput.build()).build());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final RemoteViews bigView = getBigView(context, notificationConfig);
            notification.addAction(notificationConfig.resSendButtonIcon(), notificationConfig.sendButtonText(), sendIntent)
                    .addAction(notificationConfig.resDiscardButtonIcon(), notificationConfig.discardButtonText(), discardIntent)
                    .setCustomContentView(getSmallView(context, notificationConfig, sendIntent, discardIntent))
                    .setCustomBigContentView(bigView)
                    .setCustomHeadsUpContentView(bigView)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        }
        //On old devices we have no notification buttons, so we have to set the intent to the only possible interaction: click
        if (notificationConfig.sendOnClick() || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            notification.setContentIntent(sendIntent);
        }
        notification.setDeleteIntent(discardIntent);
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

    @NonNull
    private RemoteViews getSmallView(@NonNull Context context, @NonNull NotificationConfiguration notificationConfig, @NonNull PendingIntent sendIntent, @NonNull PendingIntent discardIntent) {
        final RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        view.setTextViewText(R.id.text, notificationConfig.text());
        view.setTextViewText(R.id.title, notificationConfig.title());
        view.setImageViewResource(R.id.button_send, notificationConfig.resSendButtonIcon());
        view.setImageViewResource(R.id.button_discard, notificationConfig.resDiscardButtonIcon());
        view.setOnClickPendingIntent(R.id.button_send, sendIntent);
        view.setOnClickPendingIntent(R.id.button_discard, discardIntent);
        return view;
    }

    @NonNull
    private RemoteViews getBigView(@NonNull Context context, @NonNull NotificationConfiguration notificationConfig) {
        final RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notification_big);
        view.setTextViewText(R.id.text, notificationConfig.text());
        view.setTextViewText(R.id.title, notificationConfig.title());
        return view;
    }
}
