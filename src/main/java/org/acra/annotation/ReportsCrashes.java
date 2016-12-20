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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.builder.NoOpReportPrimer;
import org.acra.builder.ReportPrimer;
import org.acra.config.DefaultRetryPolicy;
import org.acra.config.RetryPolicy;
import org.acra.dialog.BaseCrashReportDialog;
import org.acra.dialog.CrashReportDialog;
import org.acra.file.Directory;
import org.acra.security.KeyStoreFactory;
import org.acra.security.NoKeyStoreFactory;
import org.acra.sender.DefaultReportSenderFactory;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.acra.sender.ReportSenderFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide configuration elements to the
 * {@link ACRA#init(android.app.Application)} method. The only mandatory
 * configuration item is the {@link #formUri()} parameter which is the Uri
 * to the server that will receive your reports.
 * 
 * @author Kevin Gaudin
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ReportsCrashes {

    /**
     * The Uri of your own server-side script that will receive reports. This is
     * to use if you don't want to send reports to Google Docs but to your own,
     * self-hosted script.
     * 
     * @return URI of a custom server to which to post reports.
     */
    @NonNull String formUri() default ACRAConstants.DEFAULT_STRING_VALUE;

    /**
     * <p>
     * The interaction mode you want to implement. Default is
     * {@link ReportingInteractionMode#SILENT} which does not require any
     * resources configuration.
     * </p>
     * <p>
     * Other modes have resources requirements:
     * </p>
     * <ul>
     * <li>{@link ReportingInteractionMode#TOAST} requires
     * {@link #resToastText()} to be provided to define the text that you want
     * to be displayed to the user when a report is being sent.</li>
     * <li>{@link ReportingInteractionMode#NOTIFICATION} requires
     * {@link #resNotifTickerText()}, {@link #resNotifTitle()},
     * {@link #resNotifText()}, {@link #resDialogText()}.</li>
     * <li>{@link ReportingInteractionMode#DIALOG} requires
     * {@link #resDialogText()}.</li>
     * </ul>
     * <p>
     * Default is {@link ReportingInteractionMode#SILENT}
     * </p>
     * 
     * @return the interaction mode that you want ACRA to implement.
     */
    @NonNull ReportingInteractionMode mode() default ReportingInteractionMode.SILENT;

    /**
     * @return Resource id for the label of positive button in the crash dialog.
     *         If not provided, defaults to 'OK'.
     */
    @StringRes int resDialogPositiveButtonText() default ACRAConstants.DEFAULT_DIALOG_POSITIVE_BUTTON_TEXT;

    /**
     * @return Resource id for the label of negative button in the crash dialog.
     *         If not provided, defaults to 'cancel'.
     */
    @StringRes int resDialogNegativeButtonText() default ACRAConstants.DEFAULT_DIALOG_NEGATIVE_BUTTON_TEXT;

    /**
     * @return Resource id for the user comment input label in the crash dialog.
     *         If not provided, disables the input field.
     */
    @StringRes int resDialogCommentPrompt() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the user email address input label in the crash
     *         dialog. If not provided, disables the input field.
     */
    @StringRes int resDialogEmailPrompt() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the icon in the crash dialog. Default value is
     *         the system alert icon.
     */
    @DrawableRes int resDialogIcon() default ACRAConstants.DEFAULT_DIALOG_ICON;

    /**
     * @return Resource id for the Toast text triggered when the user accepts to
     *         send a report in the crash dialog.
     */
    @StringRes int resDialogOkToast() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the text in the crash dialog.
     */
    @StringRes int resDialogText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the title in the crash dialog.
     */
    @StringRes int resDialogTitle() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     *
     * @return resource id for the crash dialog theme
     */
    @StyleRes int resDialogTheme() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the icon in the status bar notification. Default
     *         is the system error notification icon.
     */
    @DrawableRes int resNotifIcon() default ACRAConstants.DEFAULT_NOTIFICATION_ICON;

    /**
     * @return Resource id for the text in the status bar notification.
     */
    @StringRes int resNotifText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the ticker text in the status bar notification.
     */
    @StringRes int resNotifTickerText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Resource id for the title in the status bar notification.
     */
    @StringRes int resNotifTitle() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * Resource id for the Toast text triggered when the application crashes if
     * the {@link ReportingInteractionMode#TOAST} mode is used. Can also be used
     * in {@link ReportingInteractionMode#NOTIFICATION} and
     * {@link ReportingInteractionMode#DIALOG} modes to display a Toast message
     * while the report is being created, before the dialog/notification
     * appears. This allows the user to know what is happening just before the
     * application is terminated.
     * 
     * @return Resource id for the Toast text triggered when the application
     *         crashes.
     */
    @StringRes int resToastText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return Name of the SharedPreferences that will host ACRA settings you
     *         can make accessible to your users through a preferences screen:
     *         <ul>
     *         <li>
     *         {@link ACRA#PREF_DISABLE_ACRA} or {@link ACRA#PREF_ENABLE_ACRA}</li>
     *         <li>
     *         {@link ACRA#PREF_ALWAYS_ACCEPT}</li>
     *         <li>
     *         {@link ACRA#PREF_ENABLE_DEVICE_ID}</li>
     *         <li>
     *         {@link ACRA#PREF_ENABLE_SYSTEM_LOGS}</li>
     *         </ul>
     *         preference. Default is to use the application default
     *         SharedPreferences, as retrieved with
     *         {@link PreferenceManager#getDefaultSharedPreferences(Context)}.
     */
    @NonNull String sharedPreferencesName() default ACRAConstants.DEFAULT_STRING_VALUE;

    /**
     * If using a custom {@link ReportsCrashes#sharedPreferencesName()}, pass
     * here the mode that you need for the SharedPreference file creation:
     * {@link Context#MODE_PRIVATE}, {@link Context#MODE_WORLD_READABLE} or
     * {@link Context#MODE_WORLD_WRITEABLE}. Default is
     * {@link Context#MODE_PRIVATE}.
     * 
     * @return Mode to use with the SharedPreference creation.
     * @see Context#getSharedPreferences(String, int)
     */
    int sharedPreferencesMode() default ACRAConstants.DEFAULT_SHARED_PREFERENCES_MODE;

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
     * 
     * @return True if system tags are to be included as part of DropBox events.
     */
    boolean includeDropBoxSystemTags() default ACRAConstants.DEFAULT_INCLUDE_DROPBOX_SYSTEM_TAGS;

    /**
     * @return Array of tags that you want to be fetched when collecting DropBox
     *         entries.
     */
    @NonNull String[] additionalDropBoxTags() default {};

    /**
     * @return Number of minutes to look back when collecting events from
     *         DropBoxManager.
     */
    int dropboxCollectionMinutes() default ACRAConstants.DEFAULT_DROPBOX_COLLECTION_MINUTES;

    /**
     * <p>
     * Arguments to be passed to the logcat command line. Default is { "-t",
     * "100", "-v", "time" } for:
     * </p>
     * 
     * <pre>
     * logcat -t 100 -v time
     * </pre>
     * 
     * <p>
     * Do not include -b arguments for buffer selection, include
     * {@link ReportField#EVENTSLOG} and {@link ReportField#RADIOLOG} in
     * {@link ReportsCrashes#customReportContent()} to activate alternative
     * logcat buffers reporting. They will use the same other arguments as those
     * provided here.
     * </p>
     * 
     * <p>
     * See <a href=
     * "http://developer.android.com/intl/fr/guide/developing/tools/adb.html#logcatoptions"
     * >Listing of logcat Command Options</a>.
     * </p>
     * 
     * @return Array of arguments to supply if retrieving the log as part of the
     *         report.
     */
    @NonNull String[] logcatArguments() default { "-t", "" + ACRAConstants.DEFAULT_LOGCAT_LINES, "-v", "time" };

    /**
     * When using the {@link #formUri()} parameter to send reports to a custom
     * server-side script, you can set here and in
     * {@link #formUriBasicAuthPassword()} the credentials for a BASIC HTTP
     * authentication.
     * 
     * @return Login to use when posting reports to a custom server.
     */
    @NonNull String formUriBasicAuthLogin() default ACRAConstants.NULL_VALUE;

    /**
     * When using the {@link #formUri()} parameter to send reports to a custom
     * server-side script, you can set here and in
     * {@link #formUriBasicAuthLogin()} the credentials for a BASIC HTTP
     * authentication.
     * 
     * @return Password to use when posting reports to a custom server.
     */
    @NonNull String formUriBasicAuthPassword() default ACRAConstants.NULL_VALUE;

    /**
     * <p>
     * Redefines the list of {@link ReportField}s collected and sent in your
     * reports. If you modify this list, you have to create a new Google Drive
     * Spreadsheet &amp; Form which will be based on these fields as column headers.
     * </p>
     * <p>
     * The fields order is significant. You can also use this property to modify
     * fields order in your reports.
     * </p>
     * <p>
     * The default list is the following, except if you send reports by mail
     * using {@link #mailTo()}.
     * </p>
     * <ul>
     * <li>
     * {@link ReportField#REPORT_ID}</li>
     * <li>
     * {@link ReportField#APP_VERSION_CODE}</li>
     * <li>
     * {@link ReportField#APP_VERSION_NAME}</li>
     * <li>
     * {@link ReportField#PACKAGE_NAME}</li>
     * <li>
     * {@link ReportField#FILE_PATH}</li>
     * <li>
     * {@link ReportField#PHONE_MODEL}</li>
     * <li>
     * {@link ReportField#BRAND}</li>
     * <li>
     * {@link ReportField#PRODUCT}</li>
     * <li>
     * {@link ReportField#ANDROID_VERSION}</li>
     * <li>
     * {@link ReportField#BUILD}</li>
     * <li>
     * {@link ReportField#TOTAL_MEM_SIZE}</li>
     * <li>
     * {@link ReportField#AVAILABLE_MEM_SIZE}</li>
     * <li>
     * {@link ReportField#CUSTOM_DATA}</li>
     * <li>
     * {@link ReportField#IS_SILENT}</li>
     * <li>
     * {@link ReportField#STACK_TRACE}</li>
     * <li>
     * {@link ReportField#INITIAL_CONFIGURATION}</li>
     * <li>
     * {@link ReportField#CRASH_CONFIGURATION}</li>
     * <li>
     * {@link ReportField#DISPLAY}</li>
     * <li>
     * {@link ReportField#USER_COMMENT}</li>
     * <li>
     * {@link ReportField#USER_EMAIL}</li>
     * <li>
     * {@link ReportField#USER_APP_START_DATE}</li>
     * <li>
     * {@link ReportField#USER_CRASH_DATE}</li>
     * <li>
     * {@link ReportField#DUMPSYS_MEMINFO}</li>
     * <li>
     * {@link ReportField#LOGCAT}</li>
     * <li>
     * {@link ReportField#INSTALLATION_ID}</li>
     * <li>
     * {@link ReportField#DEVICE_FEATURES}</li>
     * <li>
     * {@link ReportField#ENVIRONMENT}</li>
     * <li>
     * {@link ReportField#SHARED_PREFERENCES}</li>
     * <li>
     * {@link ReportField#SETTINGS_SYSTEM}</li>
     * <li>
     * {@link ReportField#SETTINGS_SECURE}</li>
     * <li>
     * {@link ReportField#SETTINGS_GLOBAL}</li>
     * </ul>
     * 
     * @return ReportField Array listing the fields to be included in the
     *         report.
     */
    @NonNull ReportField[] customReportContent() default {};

    /**
     * <p>
     * Add your crash reports mailbox here if you want to send reports via
     * email. This allows to get rid of the INTERNET permission. Reports content
     * can be customized with {@link #customReportContent()} . Default fields
     * are:
     * </p>
     * <ul>
     * <li>
     * {@link ReportField#USER_COMMENT}</li>
     * <li>
     * {@link ReportField#ANDROID_VERSION}</li>
     * <li>
     * {@link ReportField#APP_VERSION_NAME}</li>
     * <li>
     * {@link ReportField#BRAND}</li>
     * <li>
     * {@link ReportField#PHONE_MODEL}</li>
     * <li>
     * {@link ReportField#CUSTOM_DATA}</li>
     * <li>
     * {@link ReportField#STACK_TRACE}</li>
     * </ul>
     * 
     * @return email address to which to send reports.
     */
    @NonNull String mailTo() default ACRAConstants.DEFAULT_STRING_VALUE;

    /**
     * Controls whether unapproved reports are deleted on application start or not.
     * Default is true.
     *
     * Silent and Toast reports are automatically approved.
     * Dialog and Notification reports required explicit approval by the user before they are sent.
     *
     * On application restart the user is prompted with approval for any unsent reports.
     * So you generally don't want to accumulate unapproved reports, otherwise you will prompt them multiple times.
     *
     * If this is set to true then all unapproved reports bar one will be deleted on application start.
     * The last report is always retained because that is the report that probably just happened.
     *
     * If set to false then on restart the user will be prompted with approval for each unapproved report.
     *
     * @return true if ACRA should delete unapproved reports on application start.
     */
    boolean deleteUnapprovedReportsOnApplicationStart() default ACRAConstants.DEFAULT_DELETE_UNAPPROVED_REPORTS_ON_APPLICATION_START;

    /**
     * This property can be used to determine whether old (out of date) reports
     * should be sent or not. By default they are discarded.
     * 
     * @return true if ACRA should delete any unsent reports on startup if the
     *         application has been updated since the last time the application
     *         was started.
     */
    boolean deleteOldUnsentReportsOnApplicationStart() default ACRAConstants.DEFAULT_DELETE_OLD_UNSENT_REPORTS_ON_APPLICATION_START;

    /**
     * @return Value in milliseconds for timeout attempting to connect to a network (default 5000ms).
     */
    int connectionTimeout() default ACRAConstants.DEFAULT_CONNECTION_TIMEOUT;

    /**
     * If the request is retried due to timeout, the socketTimeout will double
     * before retrying the request.
     * 
     * @return Value in milliseconds for timeout receiving a response to a network request (default 8000ms).
     */
    int socketTimeout() default ACRAConstants.DEFAULT_SOCKET_TIMEOUT;

    /**
     * Set this to true if you prefer displaying the native force close dialog after the ACRA is done.
     * Recommended: Keep this set to false if using {@link ReportingInteractionMode#DIALOG} for notification.
     * 
     * @return true if the native force close dialog should be displayed.
     */
    boolean alsoReportToAndroidFramework() default ACRAConstants.DEFAULT_REPORT_TO_ANDROID_FRAMEWORK;

    /**
     * Add here your {@link SharedPreferences} identifier Strings if you use
     * others than your application's default. They will be added to the
     * {@link ReportField#SHARED_PREFERENCES} field.
     * 
     * @return String Array containing the names of the additional preferences.
     */
    @NonNull String[] additionalSharedPreferences() default {};

    /**
     * Set this to true if you want to include only logcat lines related to your
     * Application process.
     * 
     * @return true if you want to filter logcat with your process id.
     */
    boolean logcatFilterByPid() default ACRAConstants.DEFAULT_LOGCAT_FILTER_BY_PID;

    /**
     * Set this to true if you want to read logcat lines in a non blocking way for your
     * thread. It has a default timeout of 3 seconds.
     *
     * @return true if you want that reading of logcat lines to not block current thread.
     */
    boolean nonBlockingReadForLogcat() default  ACRAConstants.DEFAULT_NON_BLOCKING_READ_FOR_LOGCAT;

    /**
     * Set this to false if you want to disable sending reports in development
     * mode. Only signed application packages will send reports. Default value
     * is true.
     * 
     * @return false if reports should not be sent.
     */
    boolean sendReportsInDevMode() default ACRAConstants.DEFAULT_SEND_REPORTS_IN_DEV_MODE;

    /**
     * @return true.
     * @deprecated since 4.8.3 no replacement. Now that we are using the SenderService in a separate process we always send at shutdown.
     */
    boolean sendReportsAtShutdown() default true;

    /**
     * Provide here regex patterns to be evaluated on each SharedPreference key
     * to exclude KV pairs from the collected SharedPreferences. This allows you
     * to exclude sensitive user data like passwords to be collected.
     * 
     * @return an array of regex patterns, every matching key is not collected.
     */
    @NonNull String[] excludeMatchingSharedPreferencesKeys() default {};

    /**
     * Provide here regex patterns to be evaluated on each Settings.System,
     * Settings.Secure and Settings.Global key to exclude KV pairs from the
     * collected SharedPreferences. This allows you to exclude sensitive data to
     * be collected.
     * 
     * @return an array of regex patterns, every matching key is not collected.
     */
    @NonNull String[] excludeMatchingSettingsKeys() default {};

    /**
     * The default value will be a BuildConfig class residing in the same package as the Application class.
     *
     * @return BuildConfig class from which to read any BuildConfig attributes.
     */
    @NonNull Class buildConfigClass() default Object.class;

    /**
     * The default {@link org.acra.sender.ReportSenderFactory} creates an {@link org.acra.sender.EmailIntentSender}
     * if the 'mailTo' parameter is defined or an {@link org.acra.sender.HttpSender} if the 'formUri' parameter
     * is defined (and internet permission has been granted.
     *
     * @return List of the {@link org.acra.sender.ReportSenderFactory} with which to construct the
     *         {@link org.acra.sender.ReportSender}s that will send the crash reports.
     */
    @NonNull Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses() default {DefaultReportSenderFactory.class};

    /**
     * To use in combination with {@link ReportField#APPLICATION_LOG} to set the
     * path/name of your application log file. If the string does not contain
     * any path separator, the file is assumed as being in
     * {@link Context#getFilesDir()}.
     * 
     * @return a String containing the path/name of your application log file.
     *         If the string does not contain any path separator, the file is
     *         assumed as being in {@link Context#getFilesDir()}.
     */
    @NonNull String applicationLogFile() default ACRAConstants.DEFAULT_APPLICATION_LOGFILE;

    /**
     * To use in combination with {@link ReportField#APPLICATION_LOG} to set the
     * number of latest lines of your application log file to be collected.
     * Default value is 100.
     * 
     * @return number of lines to collect.
     */
    int applicationLogFileLines() default ACRAConstants.DEFAULT_APPLICATION_LOGFILE_LINES;

    /**
     * To use in combination with {@link ReportField#APPLICATION_LOG} to set the root
     * for the path provided in {@link #applicationLogFile()}
     *
     * @return the directory of the application log file
     */
    @NonNull Directory applicationLogFileDir() default Directory.FILES_LEGACY;

    /**
     * @return Class for the CrashReportDialog used when prompting the user for crash details.
     *          If not provided, defaults to CrashReportDialog.class
     */
    @NonNull Class<? extends BaseCrashReportDialog> reportDialogClass() default CrashReportDialog.class;

    /**
     * @return Class that is ued to provide any extra details for a crash.
     */
    @NonNull Class<? extends ReportPrimer> reportPrimerClass() default NoOpReportPrimer.class;

    /**
     * <p>
     * The {@link Method} to be used when posting with {@link #formUri()}.
     * </p>
     *
     * @return HTTP method used when posting reports.
     */
    @NonNull Method httpMethod() default Method.POST;

    @NonNull Type reportType() default Type.FORM;

    /**
     * @return Class which creates a keystore that can contain trusted certificates
     */
    @NonNull Class<? extends KeyStoreFactory> keyStoreFactoryClass() default NoKeyStoreFactory.class;

    /**
     * @return path to a custom trusted certificate. Must start with "asset://" if the file is in the assets folder
     */
    @NonNull String certificatePath() default ACRAConstants.DEFAULT_STRING_VALUE;

    /**
     * @return resource id of a custom trusted certificate.
     */
    @RawRes int resCertificate() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return specify the type of the certificate set in either {@link #certificatePath()} or {@link #resCertificate()}
     */
    @NonNull String certificateType() default ACRAConstants.DEFAULT_CERTIFICATE_TYPE;

    /**
     * @return a Class that decides if a report should be resent (usually if one or more senders failed).
     * @since 4.9.1
     */
    @NonNull Class<? extends RetryPolicy> retryPolicyClass() default DefaultRetryPolicy.class;

    /**
     * @return true if all services running in a process should be stopped before it is killed.
     * @since 4.9.2
     */
    boolean stopServicesOnCrash() default false;
}
