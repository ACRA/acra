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
     * @return Resource id for the icon in the status bar notification. Default
     * is the system error notification icon.
     */
    @DrawableRes int resIcon() default android.R.drawable.stat_sys_warning;

    /**
     * @return Resource id for the title in the status bar notification.
     */
    @StringRes int resTitle();

    /**
     * @return Resource id for the text in the status bar notification.
     */
    @StringRes int resText();

    /**
     * @return Resource id for the ticker text in the status bar notification.
     */
    @StringRes int resTickerText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the send button text in the status bar notification.
     */
    @StringRes int resSendButtonText() default android.R.string.ok;

    /**
     * @return Resource id for the send button icon in the status bar notification.
     */
    @DrawableRes int resSendButtonIcon() default android.R.drawable.ic_menu_send;

    /**
     * @return Resource id for the discard button text in the status bar notification.
     */
    @StringRes int resDiscardButtonText() default android.R.string.cancel;

    /**
     * @return Resource id for the discard button icon in the status bar notification.
     */
    @DrawableRes int resDiscardButtonIcon() default android.R.drawable.ic_menu_delete;

    /**
     * @return Resource id for the notification channel name
     */
    @StringRes int resChannelName();

    /**
     * @return Resource id for the notification channel description
     */
    @StringRes int resChannelDescription() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return notification channel importance. Default is {@link android.app.NotificationManager#IMPORTANCE_HIGH}
     */
    int resChannelImportance() default 4;

    /**
     * @return Resource id for the send with comment button text in the status bar notification.
     * No effect on pre-nougat devices.
     */
    @StringRes int resSendWithCommentButtonText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the send with comment button icon in the status bar notification.
     * No effect on pre-nougat devices.
     */
    @DrawableRes int resSendWithCommentButtonIcon() default android.R.drawable.ic_menu_send;

    /**
     * @return Resource id for the user comment input label in the notification action.
     * No effect on pre-nougat devices.
     */
    @StringRes int resCommentPrompt() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return if a normal click on the notification should send the report
     */
    boolean sendOnClick() default false;
}
