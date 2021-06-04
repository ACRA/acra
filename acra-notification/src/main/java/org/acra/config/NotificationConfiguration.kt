/*
 * Copyright (c) 2021
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

package org.acra.config

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.faendir.kotlin.autodsl.AutoDsl
import org.acra.ACRAConstants
import org.acra.annotation.AcraDsl
import org.acra.ktx.plus

/**
 * @author F43nd1r
 * @since 15.09.2017
 */
@AutoDsl(dslMarker = AcraDsl::class)
class NotificationConfiguration(
    /**
     * enables this plugin
     */
    val enabled: Boolean = true,
    /**
     * icon of the notification
     *
     * @see androidx.core.app.NotificationCompat.Builder#setSmallIcon(int)
     * @since 5.0.0
     */
    @DrawableRes
    val resIcon: Int = android.R.drawable.stat_sys_warning,

    /**
     * title of the notification
     *
     * @see androidx.core.app.NotificationCompat.Builder.setContentTitle
     * @since 5.0.0
     */
    val title: String,

    /**
     * text in the notification
     *
     * @see androidx.core.app.NotificationCompat.Builder.setContentText
     * @since 5.0.0
     */
    val text: String,

    /**
     * ticker text for the notification
     *
     * @see androidx.core.app.NotificationCompat.Builder.setTicker
     * @since 5.0.0
     */
    val tickerText: String? = null,

    /**
     * send button text shown in expanded view
     *
     * defaults to [android.R.string.ok]
     *
     * @see androidx.core.app.NotificationCompat.Builder.addAction
     * @since 5.0.0
     */
    val sendButtonText: String? = null,

    /**
     * send button icon shown in collapsed and sometimes expanded view
     *
     * @see androidx.core.app.NotificationCompat.Builder.addAction
     * @since 5.0.0
     */
    @DrawableRes
    val resSendButtonIcon: Int = android.R.drawable.ic_menu_send,

    /**
     * discard button text shown in expanded view
     *
     * defaults to [android.R.string.cancel]
     *
     * @see androidx.core.app.NotificationCompat.Builder.addAction
     * @since 5.0.0
     */
    val discardButtonText: String? = null,

    /**
     * discard button icon shown in collapsed and sometimes expanded view
     *
     * @see androidx.core.app.NotificationCompat.Builder.addAction
     * @since 5.0.0
     */
    @DrawableRes
    val resDiscardButtonIcon: Int = android.R.drawable.ic_menu_delete,

    /**
     * notification channel name.
     * To learn about notification channels, visit the [notification guide](https://developer.android.com/guide/topics/ui/notifiers/notifications.html#ManageChannels)
     *
     * @see android.app.NotificationChannel
     * @since 5.0.0
     */
    val channelName: String,

    /**
     * notification channel description
     *
     * @see android.app.NotificationChannel.setDescription
     * @since 5.0.0
     */
    val channelDescription: String? = null,

    /**
     * notification channel importance. Must be one of
     * - [android.app.NotificationManager#IMPORTANCE_NONE]
     * - [android.app.NotificationManager#IMPORTANCE_LOW]
     * - [android.app.NotificationManager#IMPORTANCE_DEFAULT]
     * - [android.app.NotificationManager#IMPORTANCE_HIGH]
     * - [android.app.NotificationManager#IMPORTANCE_MAX]
     *
     * Default is [android.app.NotificationManager#IMPORTANCE_HIGH]
     *
     * @see android.app.NotificationChannel
     * @since 5.0.0
     */
    val channelImportance: Int = 4,

    /**
     * in-line comment button text.
     * No effect on pre-nougat devices.
     *
     * @see androidx.core.app.NotificationCompat.Builder.addAction
     * @since 5.0.0
     */
    val sendWithCommentButtonText: String? = null,

    /**
     * in-line comment button icon.
     * No effect on pre-nougat devices.
     *
     * @see androidx.core.app.NotificationCompat.Builder.addAction
     * @since 5.0.0
     */
    @DrawableRes
    val resSendWithCommentButtonIcon: Int = android.R.drawable.ic_menu_send,

    /**
     * in-line comment prompt label.
     * No effect on pre-nougat devices.
     *
     * @see androidx.core.app.RemoteInput.Builder.setLabel
     * @since 5.0.0
     */
    val commentPrompt: String? = null,

    /**
     * enable to send report even on normal click, not only on button click
     *
     * @since 5.0.0
     */
    val sendOnClick: Boolean = false,
) : Configuration {
    override fun enabled(): Boolean = enabled
}

fun CoreConfigurationBuilder.notification(initializer: NotificationConfigurationBuilder.() -> Unit) {
    pluginConfigurations += NotificationConfigurationBuilder().apply(initializer).build()
}
