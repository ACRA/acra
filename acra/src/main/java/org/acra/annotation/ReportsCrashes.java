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

import static org.acra.ReportField.*;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Provide configuration elemets to the
 * {@link ACRA#init(android.app.Application)} method. The only mandatory
 * configuration item is the {@link #formKey()} parameter which is the Id of
 * your Google Documents form which will receive reports.
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
    String formKey();

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
     * Resource id for the user email address input label in the crash dialog.
     * If not provided, disables the input field.
     */
    int resDialogEmailPrompt() default 0;

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
     * {@link Context#MODE_WORLD_WRITEABLE}. Default is
     * {@link Context#MODE_PRIVATE}.
     * 
     * @see Context#getSharedPreferences(String, int)
     */
    int sharedPreferencesMode() default Context.MODE_PRIVATE;

    /**
     * If enabled, DropBox events collection will include system tags:
     * <ul>
     * <li>system_app_anr</li>
     * <li>system_app_wtf</li>
     * <li>system_app_crash</li>
     * <li>system_server_anr</li>
     * <li>system_server_wtf</li>
     * <li>system_server_crash</li>
     * <li>BATTERY_DISCHARGE_INFO</li>
     * <li>SYSTEM_RECOVERY_LOG</li>
     * <li>SYSTEM_BOOT</li>
     * <li>SYSTEM_LAST_KMSG</li>
     * <li>APANIC_CONSOLE</li>
     * <li>APANIC_THREADS</li>
     * <li>SYSTEM_RESTART</li>
     * <li>SYSTEM_TOMBSTONE</li>
     * <li>data_app_strictmode</li>
     * </ul>
     * Requires {@link #includeDropBox()} true.
     * 
     * @return
     */
    boolean includeDropBoxSystemTags() default false;

    /**
     * You can provide here a list of tags that will be fetched when collecting
     * DropBox entries.
     */
    String[] additionalDropBoxTags() default {};

    /**
     * How many minutes will be looked back when collecting events from
     * DropBoxManager.
     */
    int dropboxCollectionMinutes() default 5;

    /**
     * <p>
     * Arguments to be passed to the logcat command line. Default is { "-t",
     * "200", "-v", "time" } for:
     * </p>
     * 
     * <pre>
     * logcat -t 200 -v time
     * </pre>
     * 
     * <p>
     * Do not include -b arguments for buffer selection, use
     * {@link #includeEventsLogcat()} and {@link #includeRadioLogcat()} to
     * activate alternative logcat buffers reporting. They will use the same
     * other arguments as those provided here.
     * </p>
     * 
     * <p>
     * See <a href=
     * "http://developer.android.com/intl/fr/guide/developing/tools/adb.html#logcatoptions"
     * >Listing of logcat Command Options</a>.
     * </p>
     */
    String[] logcatArguments() default { "-t", "200", "-v", "time" };

    /**
     * A special String value to allow the usage of a pseudo-null default value
     * in annotation parameters.
     */
    public static final String NULL_VALUE = "ACRA-NULL-STRING";

    /**
     * When using the {@link #formUri()} parameter to send reports to a custom
     * server-side script, you can set here and in
     * {@link #formUriBasicAuthPassword()} the credentials for a BASIC HTTP
     * authentication.
     */
    String formUriBasicAuthLogin() default NULL_VALUE;

    /**
     * When using the {@link #formUri()} parameter to send reports to a custom
     * server-side script, you can set here and in
     * {@link #formUriBasicAuthLogin()} the credentials for a BASIC HTTP
     * authentication.
     */
    String formUriBasicAuthPassword() default NULL_VALUE;

    /**
     * Specifies the list of fields to be included in reports with their order.
     */
    ReportField[] customReportContent() default {};

    static final ReportField[] DEFAULT_REPORT_FIELDS = { REPORT_ID, APP_VERSION_CODE, APP_VERSION_NAME, PACKAGE_NAME,
            FILE_PATH, PHONE_MODEL, BRAND, PRODUCT, ANDROID_VERSION, BUILD, TOTAL_MEM_SIZE, AVAILABLE_MEM_SIZE,
            CUSTOM_DATA, STACK_TRACE, INITIAL_CONFIGURATION, CRASH_CONFIGURATION, DISPLAY, USER_COMMENT, USER_EMAIL,
            USER_APP_START_DATE, USER_CRASH_DATE, DUMPSYS_MEMINFO, DROPBOX, LOGCAT, EVENTSLOG, RADIOLOG, DEVICE_ID,
            INSTALLATION_ID, DEVICE_FEATURES, ENVIRONMENT, SETTINGS_SYSTEM, SETTINGS_SECURE };

    /**
     * Add your crash reports mailbox here if you want to send reports via
     * email. This allows to get rid of the INTERNET permission. Reports content
     * can be customized with {@link #reportFields()}. Default fields are:
     * {@link ReportField#USER_COMMENT}, {@link ReportField#ANDROID_VERSION},
     * {@link ReportField#APP_VERSION_NAME} , {@link ReportField#BRAND},
     * {@link ReportField#PHONE_MODEL}, {@link ReportField#CUSTOM_DATA},
     * {@link ReportField#STACK_TRACE}
     */
    String mailTo() default "";

    /**
     * Default list of {@link ReportField}s to be sent in email reports {@see
     * #mailTo()}. You can set your own list with {@link #reportFields()}.
     */
    final static ReportField[] DEFAULT_MAIL_REPORT_FIELDS = { ReportField.USER_COMMENT, ReportField.ANDROID_VERSION,
            ReportField.APP_VERSION_NAME, ReportField.BRAND, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA,
            ReportField.STACK_TRACE };

    /**
     * Controls whether unapproved reports are deleted on application start or
     * not. Default is true. This is a change from versions of ACRA before 3.2
     * as in NOTIFICATION mode reports were previously kept until the user
     * explicitly opens the Notification dialog AND choose to send or discard
     * the report. Until then, on application restart, ACRA was issuing a new
     * crash notification for previous reports pending for approval. This could
     * be misunderstood by the user with a new crash, resulting in bad
     * appreciation of the application.
     */
    boolean deleteUnapprovedReportsOnApplicationStart() default true;
}
