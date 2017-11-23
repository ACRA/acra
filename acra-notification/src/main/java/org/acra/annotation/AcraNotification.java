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

package org.acra.annotation;

import android.app.PendingIntent;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import org.acra.ACRAConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author F43nd1r
 * @since 15.09.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration
public @interface AcraNotification {

    /**
     * icon of the notification
     *
     * @return Resource id for the icon in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#setSmallIcon(int)
     * @since 5.0.0
     */
    @DrawableRes int resIcon() default android.R.drawable.stat_sys_warning;

    /**
     * title of the notification
     *
     * @return Resource id for the title in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#setContentTitle(CharSequence)
     * @since 5.0.0
     */
    @StringRes int resTitle();

    /**
     * text in the notification
     *
     * @return Resource id for the text in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#setContentText(CharSequence)
     * @since 5.0.0
     */
    @StringRes int resText();

    /**
     * ticker text for the notification
     *
     * @return Resource id for the ticker text in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#setTicker(CharSequence)
     * @since 5.0.0
     */
    @StringRes int resTickerText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * send button text shown in expanded view
     *
     * @return Resource id for the send button text in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#addAction(int, CharSequence, PendingIntent)
     * @since 5.0.0
     */
    @StringRes int resSendButtonText() default android.R.string.ok;

    /**
     * send button icon shown in collapsed and sometimes expanded view
     *
     * @return Resource id for the send button icon in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#addAction(int, CharSequence, PendingIntent)
     * @since 5.0.0
     */
    @DrawableRes int resSendButtonIcon() default android.R.drawable.ic_menu_send;

    /**
     * discard button text shown in expanded view
     *
     * @return Resource id for the discard button text in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#addAction(int, CharSequence, PendingIntent)
     * @since 5.0.0
     */
    @StringRes int resDiscardButtonText() default android.R.string.cancel;

    /**
     * discard button icon shown in collapsed and sometimes expanded view
     *
     * @return Resource id for the discard button icon in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#addAction(int, CharSequence, PendingIntent)
     * @since 5.0.0
     */
    @DrawableRes int resDiscardButtonIcon() default android.R.drawable.ic_menu_delete;

    /**
     * notification channel name.
     * To learn about notification channels, visit the <a href="https://developer.android.com/guide/topics/ui/notifiers/notifications.html#ManageChannels">notification guide</a>
     *
     * @return Resource id for the notification channel name
     * @see android.app.NotificationChannel#NotificationChannel(String, CharSequence, int)
     * @since 5.0.0
     */
    @StringRes int resChannelName();

    /**
     * notification channel description
     *
     * @return Resource id for the notification channel description
     * @see android.app.NotificationChannel#setDescription(String)
     * @since 5.0.0
     */
    @StringRes int resChannelDescription() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * notification channel importance. Must be one of
     * <ul>
     * <li>{@link android.app.NotificationManager#IMPORTANCE_NONE}</li>
     * <li>{@link android.app.NotificationManager#IMPORTANCE_LOW}</li>
     * <li>{@link android.app.NotificationManager#IMPORTANCE_DEFAULT}</li>
     * <li>{@link android.app.NotificationManager#IMPORTANCE_HIGH}</li>
     * <li>{@link android.app.NotificationManager#IMPORTANCE_MAX}</li>
     * </ul>
     *
     * @return notification channel importance. Default is {@link android.app.NotificationManager#IMPORTANCE_HIGH}
     * @see android.app.NotificationChannel#NotificationChannel(String, CharSequence, int)
     * @since 5.0.0
     */
    int resChannelImportance() default 4;

    /**
     * in-line comment button text.
     * No effect on pre-nougat devices.
     *
     * @return Resource id for the send with comment button text in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#addAction(int, CharSequence, PendingIntent)
     * @since 5.0.0
     */
    @StringRes int resSendWithCommentButtonText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * in-line comment button icon.
     * No effect on pre-nougat devices.
     *
     * @return Resource id for the send with comment button icon in the status bar notification.
     * @see android.support.v4.app.NotificationCompat.Builder#addAction(int, CharSequence, PendingIntent)
     * @since 5.0.0
     */
    @DrawableRes int resSendWithCommentButtonIcon() default android.R.drawable.ic_menu_send;

    /**
     * in-line comment prompt label.
     * No effect on pre-nougat devices.
     *
     * @return Resource id for the user comment input label in the notification action.
     * @see android.support.v4.app.RemoteInput.Builder#setLabel(CharSequence)
     * @since 5.0.0
     */
    @StringRes int resCommentPrompt() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * enable to send report even on normal click, not only on button click
     *
     * @return if a normal click on the notification should send the report
     * @since 5.0.0
     */
    boolean sendOnClick() default false;
}
