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

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.auto.service.AutoService
import org.acra.ACRA
import org.acra.config.CoreConfiguration
import org.acra.config.NotificationConfiguration
import org.acra.config.getPluginConfiguration
import org.acra.notification.R
import org.acra.plugins.HasConfigPlugin
import org.acra.prefs.SharedPreferencesFactory
import org.acra.receiver.NotificationBroadcastReceiver
import org.acra.sender.LegacySenderService
import java.io.File

/**
 * @author F43nd1r
 * @since 15.09.2017
 */
@AutoService(ReportInteraction::class)
class NotificationInteraction : HasConfigPlugin(NotificationConfiguration::class.java), ReportInteraction {
    override fun performInteraction(context: Context, config: CoreConfiguration, reportFile: File): Boolean {
        val prefs = SharedPreferencesFactory(context, config).create()
        if (prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {
            return true
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return true
        //can't post notifications
        val notificationConfig = config.getPluginConfiguration<NotificationConfiguration>()
        //We have to create a channel on Oreo+, because notifications without one aren't allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            val channel = NotificationChannel(CHANNEL, notificationConfig.channelName, notificationConfig.channelImportance)
            channel.setSound(null, null)
            if (notificationConfig.channelDescription?.isNotEmpty() == true) {
                channel.description = notificationConfig.channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }
        //configure base notification
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(notificationConfig.title)
            .setContentText(notificationConfig.text)
            .setSmallIcon(notificationConfig.resIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        //add ticker if set
        notificationConfig.tickerText?.takeIf { it.isNotBlank() }?.let { notification.setTicker(it) }
        //add color if set
        notificationConfig.color?.let { notification.color = it }
        val sendIntent = getSendIntent(context, config, reportFile)
        val discardIntent = getDiscardIntent(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && notificationConfig.sendWithCommentButtonText?.isNotEmpty() == true) {
            val remoteInput = RemoteInput.Builder(KEY_COMMENT)
            notificationConfig.commentPrompt?.takeIf { it.isNotBlank() }?.let { remoteInput.setLabel(it) }
            notification.addAction(
                NotificationCompat.Action.Builder(notificationConfig.resSendWithCommentButtonIcon, notificationConfig.sendWithCommentButtonText, sendIntent)
                    .addRemoteInput(remoteInput.build()).build()
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val bigView = getBigView(context, notificationConfig)
            notification.addAction(notificationConfig.resSendButtonIcon, notificationConfig.sendButtonText ?: context.getString(android.R.string.ok), sendIntent)
                .addAction(notificationConfig.resDiscardButtonIcon, notificationConfig.discardButtonText ?: context.getString(android.R.string.cancel), discardIntent)
                .setCustomContentView(getSmallView(context, notificationConfig, sendIntent, discardIntent))
                .setCustomBigContentView(bigView)
                .setCustomHeadsUpContentView(bigView)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }
        //On old devices we have no notification buttons, so we have to set the intent to the only possible interaction: click
        if (notificationConfig.sendOnClick || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            notification.setContentIntent(sendIntent)
        }
        notification.setDeleteIntent(discardIntent)
        notificationManager.notify(NOTIFICATION_ID, notification.build())
        return false
    }

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    private fun getSendIntent(context: Context, config: CoreConfiguration, reportFile: File): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = INTENT_ACTION_SEND
        intent.putExtra(LegacySenderService.EXTRA_ACRA_CONFIG, config)
        intent.putExtra(EXTRA_REPORT_FILE, reportFile)
        return PendingIntent.getBroadcast(context, ACTION_SEND, intent, pendingIntentFlags)
    }

    private fun getDiscardIntent(context: Context): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = INTENT_ACTION_DISCARD
        return PendingIntent.getBroadcast(context, ACTION_DISCARD, intent, pendingIntentFlags)
    }

    private fun getSmallView(context: Context, notificationConfig: NotificationConfiguration, sendIntent: PendingIntent, discardIntent: PendingIntent): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.notification_small)
        view.setTextViewText(R.id.text, notificationConfig.text)
        view.setTextViewText(R.id.title, notificationConfig.title)
        view.setImageViewResource(R.id.button_send, notificationConfig.resSendButtonIcon)
        view.setImageViewResource(R.id.button_discard, notificationConfig.resDiscardButtonIcon)
        view.setOnClickPendingIntent(R.id.button_send, sendIntent)
        view.setOnClickPendingIntent(R.id.button_discard, discardIntent)
        return view
    }

    private fun getBigView(context: Context, notificationConfig: NotificationConfiguration): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.notification_big)
        view.setTextViewText(R.id.text, notificationConfig.text)
        view.setTextViewText(R.id.title, notificationConfig.title)
        return view
    }

    companion object {
        const val INTENT_ACTION_SEND = "org.acra.intent.send"
        const val INTENT_ACTION_DISCARD = "org.acra.intent.discard"
        const val KEY_COMMENT = "comment"
        const val EXTRA_REPORT_FILE = "REPORT_FILE"
        const val NOTIFICATION_ID = 666
        private const val ACTION_SEND = 667
        private const val ACTION_DISCARD = 668
        private const val CHANNEL = "ACRA"
    }
}