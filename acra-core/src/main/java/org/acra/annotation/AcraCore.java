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

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.attachment.AttachmentUriProvider;
import org.acra.attachment.DefaultAttachmentProvider;
import org.acra.config.BaseCoreConfigurationBuilder;
import org.acra.config.DefaultRetryPolicy;
import org.acra.config.RetryPolicy;
import org.acra.data.StringFormat;
import org.acra.file.Directory;
import org.acra.sender.DefaultReportSenderFactory;
import org.acra.sender.ReportSenderFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Main ACRA configuration
 *
 * @author F43nd1r
 * @since 01.06.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration(baseBuilderClass = BaseCoreConfigurationBuilder.class, isPlugin = false)
public @interface AcraCore {

    /**
     * Name of the SharedPreferences that will host ACRA settings which you can make accessible to your users through a preferences screen:
     * <ul>
     * <li>{@link org.acra.ACRA#PREF_DISABLE_ACRA} or {@link org.acra.ACRA#PREF_ENABLE_ACRA}</li>
     * <li>{@link org.acra.ACRA#PREF_ALWAYS_ACCEPT}</li>
     * <li>{@link org.acra.ACRA#PREF_ENABLE_DEVICE_ID}</li>
     * <li>{@link org.acra.ACRA#PREF_ENABLE_SYSTEM_LOGS}</li>
     * </ul>
     * Default is to use the application default SharedPreferences, as retrieved with {@link android.preference.PreferenceManager#getDefaultSharedPreferences(android.content.Context)}
     *
     * @return SharedPreferences name.
     */
    @NonNull String sharedPreferencesName() default ACRAConstants.DEFAULT_STRING_VALUE;

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
     * @return if system tags are to be included as part of DropBox events.
     */
    boolean includeDropBoxSystemTags() default false;

    /**
     * Custom tags to be included in DropBox event collection
     *
     * @return tags that you want to be fetched when collecting DropBox events.
     */
    @NonNull String[] additionalDropBoxTags() default {};

    /**
     * DropBox event collection will look back this many minutes
     *
     * @return Number of minutes to look back.
     */
    int dropboxCollectionMinutes() default 5;

    /**
     * <p>
     * Arguments to be passed to the logcat command line. Default is { "-t", "100", "-v", "time" } for:
     * </p>
     * <pre>logcat -t 100 -v time</pre>
     * <p>
     * Do not include -b arguments for buffer selection, include {@link ReportField#EVENTSLOG} and {@link ReportField#RADIOLOG} in {@link #reportContent()} to activate alternative logcat buffers reporting.
     * They will use the same other arguments as those provided here.
     * </p>
     * <p>
     * See <a href="http://developer.android.com/intl/fr/guide/developing/tools/adb.html#logcatoptions">Listing of logcat Command Options</a>.
     * </p>
     *
     * @return arguments to supply if retrieving the log as part of the report.
     */
    @NonNull String[] logcatArguments() default {"-t", "" + ACRAConstants.DEFAULT_LOG_LINES, "-v", "time"};

    /**
     * <p>
     * Redefines the list of {@link ReportField}s collected and sent in your reports.
     * </p>
     * <p>
     * You can also use this property to modify fields order in your reports.
     * </p>
     * <p>
     * The default list is {@link org.acra.ACRAConstants#DEFAULT_REPORT_FIELDS}
     *
     * @return fields to be included in the report.
     */
    @NonNull ReportField[] reportContent() default {};

    /**
     * Controls whether unapproved reports are deleted on application start or not.
     * <p>
     * Silent and Toast reports are automatically approved.
     * Dialog and Notification reports require explicit approval by the user before they are sent.
     * </p>
     * <p>
     * On application restart the user is prompted with approval for one unsent report.
     * So you generally don't want to accumulate unapproved reports, otherwise you will prompt them multiple times.
     * </p>
     * <p>
     * If this is set to true then all unapproved reports bar one will be deleted on application start.
     * The last report is always retained because that is the report that probably just happened.
     * </p>
     *
     * @return if ACRA should delete unapproved reports on application start.
     */
    boolean deleteUnapprovedReportsOnApplicationStart() default true;

    /**
     * This property can be used to determine whether old (out of date) reports should be sent or not.
     *
     * @return if ACRA should delete any unsent reports on startup if the application has been updated since the last time the application was started.
     */
    boolean deleteOldUnsentReportsOnApplicationStart() default true;

    /**
     * Set this to true if you prefer displaying the native force close dialog after ACRA is done.
     * Recommended: Keep this set to false if using interactions with user input.
     *
     * @return if the native force close dialog should be displayed.
     */
    boolean alsoReportToAndroidFramework() default false;

    /**
     * Add here your {@link android.content.SharedPreferences} identifier Strings if you use others than your application's default. They will be added to the {@link ReportField#SHARED_PREFERENCES} field.
     *
     * @return names of additional preferences.
     */
    @NonNull String[] additionalSharedPreferences() default {};

    /**
     * Set this to true if you want to include only logcat lines related to your Application process. Note that this is always done by android starting with API 16 (Jellybean)
     *
     * @return true if you want to filter logcat with your process id.
     */
    boolean logcatFilterByPid() default true;

    /**
     * Set this to true if you want to read logcat lines in a non blocking way for your thread. It has a default timeout of 3 seconds.
     *
     * @return if reading of logcat lines should not block the current thread.
     */
    boolean logcatReadNonBlocking() default false;

    /**
     * Set this to false if you want to disable sending reports in development mode. Only signed application packages will send reports.
     *
     * @return if reports should only be sent from signed packages.
     */
    boolean sendReportsInDevMode() default true;

    /**
     * Provide here regex patterns to be evaluated on each {@link android.content.SharedPreferences} key to exclude KV pairs from the collected SharedPreferences.
     * This allows you to exclude sensitive user data like passwords from being collected.
     *
     * @return regex patterns, every matching key is not collected.
     */
    @NonNull String[] excludeMatchingSharedPreferencesKeys() default {};

    /**
     * Provide here regex patterns to be evaluated on each {@link android.provider.Settings.System}, {@link android.provider.Settings.Secure} and {@link android.provider.Settings.Global} key to exclude KV pairs from being collected.
     * This allows you to exclude sensitive data from being collected.
     *
     * @return regex patterns, every matching key is not collected.
     */
    @NonNull String[] excludeMatchingSettingsKeys() default {};

    /**
     * The default value will be a BuildConfig class residing in the same package as the Application class.
     * You only have to set this option if your BuildConfig class is obfuscated.
     *
     * @return BuildConfig class from which to read any BuildConfig attributes.
     */
    @NonNull Class buildConfigClass() default Object.class;

    /**
     * The default {@link org.acra.sender.ReportSenderFactory} automatically discovers other ReportSenderFactories.
     *
     * @return {@link org.acra.sender.ReportSenderFactory}s with which to construct the {@link org.acra.sender.ReportSender}s that will send the crash reports.
     */
    @NonEmpty @Instantiatable @NonNull Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses() default {DefaultReportSenderFactory.class};

    /**
     * To use in combination with {@link ReportField#APPLICATION_LOG} to set the path/name of your application log file.
     *
     * @return path/name of your application log file.
     */
    @NonNull String applicationLogFile() default ACRAConstants.DEFAULT_STRING_VALUE;

    /**
     * To use in combination with {@link ReportField#APPLICATION_LOG} to set the number of latest lines of your application log file to be collected.
     * Default value is 100.
     *
     * @return number of lines to collect.
     */
    int applicationLogFileLines() default ACRAConstants.DEFAULT_LOG_LINES;

    /**
     * To use in combination with {@link ReportField#APPLICATION_LOG} to set the root for the path provided in {@link #applicationLogFile()}
     *
     * @return the directory of the application log file
     */
    @NonNull Directory applicationLogFileDir() default Directory.FILES_LEGACY;

    /**
     * Implement a custom {@link RetryPolicy} to decide if a failed report should be resent or not.
     *
     * @return a class that decides if a report should be resent (usually if one or more senders failed).
     * @since 4.9.1
     */
    @Instantiatable @NonNull Class<? extends RetryPolicy> retryPolicyClass() default DefaultRetryPolicy.class;

    /**
     * If you have services which might crash on startup android will try to restart them indefinitely. Set this to true to prevent that.
     *
     * @return if all services running in a process should be stopped before it is killed.
     * @since 4.9.2
     */
    boolean stopServicesOnCrash() default false;

    /**
     * Allows to attach files to crash reports.
     * <p>
     * ACRA contains a file provider under the following Uri:
     * <code>content://[applicationId].acra/[Directory]/[Path]</code>
     * where <code>[applicationId]</code> is your application package name, <code>[Directory]</code> is one of the enum constants in {@link Directory} in lower case and <code>[Path]</code> is the relative path to the file in that directory
     * e.g. content://org.acra.test.acra/files/thisIsATest.txt
     * </p>
     * Side effects:
     * <ul>
     * <li>POST mode: requests will be sent with content-type multipart/mixed</li>
     * <li>PUT mode: There will be additional requests with the attachments. Naming scheme: [report-id]-[filename]</li>
     * <li>EMAIL mode: Some email clients do not support attachments, so some email may lack these attachments. Note that attachments might be readable to email clients when they are sent.</li>
     * </ul>
     *
     * @return uris to be attached to crash reports.
     * @since 4.9.3
     */
    @NonNull String[] attachmentUris() default {};

    /**
     * Allows {@link #attachmentUris()} configuration at runtime instead of compile time.
     *
     * @return a class that decides which uris should be attached to reports
     * @since 4.9.3
     */
    @Instantiatable @NonNull Class<? extends AttachmentUriProvider> attachmentUriProvider() default DefaultAttachmentProvider.class;

    /**
     * Toast shown when a report is sent successfully
     *
     * @return Resource id for the Toast text triggered when a report was sent successfully.
     * @since 5.0.0
     */
    @StringRes int resReportSendSuccessToast() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * Toast shown when report sending fails
     *
     * @return Resource id for the Toast text triggered when no report was sent successfully.
     * @since 5.0.0
     */
    @StringRes int resReportSendFailureToast() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * Format in which the report should be sent
     *
     * @return report format
     * @since 5.0.0
     */
    @NonNull StringFormat reportFormat() default StringFormat.JSON;

    /**
     * Allow parallel collection. Increases performance but might pollute e.g. logcat output
     * @return if parallel collection should be active
     * @since 5.0.1
     */
    boolean parallel() default true;
}
