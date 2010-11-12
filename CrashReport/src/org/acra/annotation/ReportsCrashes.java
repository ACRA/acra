/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
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
package org.acra.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Provide configuration elemets to the
 * {@link ACRA#init(android.app.Application)} method. The only mandatory
 * configuration item is the {@link #formId()} parameter which is the Id of your
 * Google Documents form which will receive reports.
 * 
 * @author Kevin Gaudin
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReportsCrashes {
    /**
     * The id of the Google Doc form.
     * 
     * @return
     */
    String formId();

    /**
     * The Uri of your own server-side script that will receive reports. This is
     * to use if you don't want to send reports to Google Docs but to your own
     * script.
     * 
     * @return
     */
    String formUri() default "";

    /**
     * <p>
     * The interaction mode you want to implement. Default is
     * {@link ReportingInteractionMode#SILENT} which does not require any
     * resources configuration.
     * </p>
     * <p>
     * Other modes have resources requirements:
     * <ul>
     * <li>{@link ReportingInteractionMode#TOAST} requires
     * {@link #resToastText()} to be provided to define the text that you want
     * to be displayed to the user when a report is being sent.</li>
     * <li>{@link ReportingInteractionMode#NOTIFICATION} requires
     * {@link #resNotifTickerText()}, {@link #resNotifTitle()},
     * {@link #resNotifText()}, {@link #resDialogText()}</li>
     * </ul>
     * </p>
     * 
     * @return
     */
    ReportingInteractionMode mode() default ReportingInteractionMode.SILENT;

    /**
     * Resource id for the user comment input label in the crash dialog. If not
     * provided, disables the input field.
     */
    int resDialogCommentPrompt() default 0;

    /**
     * Resource id for the icon in the crash dialog.
     */
    int resDialogIcon() default android.R.drawable.ic_dialog_alert;

    /**
     * Resource id for the Toast text triggered when the user accepts to send a
     * report in the crash dialog.
     */
    int resDialogOkToast() default 0;

    /**
     * Resource id for the text in the crash dialog.
     */
    int resDialogText() default 0;

    /**
     * Resource id for the title in the crash dialog.
     */
    int resDialogTitle() default 0;

    /**
     * Resource id for the icon in the status bar notification.
     */
    int resNotifIcon() default android.R.drawable.stat_notify_error;

    /**
     * Resource id for the text in the status bar notification.
     */
    int resNotifText() default 0;

    /**
     * Resource id for the ticker text in the status bar notification.
     */
    int resNotifTickerText() default 0;

    /**
     * Resource id for the title in the status bar notification.
     */
    int resNotifTitle() default 0;

    /**
     * Resource id for the Toast text triggered when the application crashes if
     * the notification+dialog mode is not used.
     */
    int resToastText() default 0;

    /**
     * Name of the SharedPreferences that will host the
     * {@link ACRA#PREF_DISABLE_ACRA} or {@link ACRA#PREF_ENABLE_ACRA}
     * preference. Default is to use the default SharedPreferences, as retrieved
     * with {@link PreferenceManager#getDefaultSharedPreferences(Context)}.
     */
    String sharedPreferencesName() default "";

    /**
     * If using a custom {@link ReportsCrashes#sharedPreferencesName()}, pass
     * here the mode that you need for the SharedPreference file creation:
     * {@link Context#MODE_PRIVATE}, {@link Context#MODE_WORLD_READABLE} or
     * {@link Context#MODE_WORLD_WRITEABLE}. Default is {@link Context#MODE_PRIVATE}.
     * 
     * @see Context#getSharedPreferences(String, int)
     */
    int sharedPreferencesMode() default Context.MODE_PRIVATE;
}
