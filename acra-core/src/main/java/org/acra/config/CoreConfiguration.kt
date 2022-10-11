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

import com.faendir.kotlin.autodsl.AutoDsl
import org.acra.ACRAConstants
import org.acra.ReportField
import org.acra.annotation.AcraDsl
import org.acra.attachment.AttachmentUriProvider
import org.acra.attachment.DefaultAttachmentProvider
import org.acra.data.StringFormat
import org.acra.file.Directory
import org.acra.plugins.PluginLoader
import org.acra.plugins.ServicePluginLoader
import java.io.Serializable

@AutoDsl(dslMarker = AcraDsl::class)
class CoreConfiguration(

    /**
     * Name of the SharedPreferences that will host ACRA settings which you can make accessible to your users through a preferences screen:
     *
     * - [org.acra.ACRA.PREF_DISABLE_ACRA] or [org.acra.ACRA.PREF_ENABLE_ACRA]
     * - [org.acra.ACRA.PREF_ALWAYS_ACCEPT]
     * - [org.acra.ACRA.PREF_ENABLE_DEVICE_ID]
     * - [org.acra.ACRA.PREF_ENABLE_SYSTEM_LOGS]
     *
     * Default is to use the application default SharedPreferences, as retrieved with [android.preference.PreferenceManager.getDefaultSharedPreferences]
     */
    val sharedPreferencesName: String? = null,

    /**
     * If enabled, DropBox events collection will include system tags:
     *
     * - system_app_anr
     * - system_app_wtf
     * - system_app_crash
     * - system_server_anr
     * - system_server_wtf
     * - system_server_crash
     * - BATTERY_DISCHARGE_INFO
     * - SYSTEM_RECOVERY_LOG
     * - SYSTEM_BOOT
     * - SYSTEM_LAST_KMSG
     * - APANIC_CONSOLE
     * - APANIC_THREADS
     * - SYSTEM_RESTART
     * - SYSTEM_TOMBSTONE
     * - data_app_strictmode
     *
     */
    val includeDropBoxSystemTags: Boolean = false,

    /**
     * Custom tags to be included in DropBox event collection
     */
    val additionalDropBoxTags: List<String> = emptyList(),

    /**
     * DropBox event collection will look back this many minutes
     */
    val dropboxCollectionMinutes: Int = 5,

    /**
     * Arguments to be passed to the logcat command line.
     *
     * Do not include -b arguments for buffer selection, include [ReportField.EVENTSLOG] and [ReportField.RADIOLOG] in [reportContent] to activate alternative logcat buffers reporting.
     * They will use the same other arguments as those provided here.
     *
     * See [Listing of logcat Command Options](http://developer.android.com/intl/fr/guide/developing/tools/adb.html#logcatoptions)
     */
    val logcatArguments: List<String> = listOf("-t", ACRAConstants.DEFAULT_LOG_LINES.toString(), "-v", "time"),

    /**
     * Redefines the list of [ReportField]s collected and sent in your reports.
     *
     * You can also use this property to modify fields order in your reports.
     */
    val reportContent: List<ReportField> = ACRAConstants.DEFAULT_REPORT_FIELDS.toList(),

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
     */
    val deleteUnapprovedReportsOnApplicationStart: Boolean = true,

    /**
     * Set this to true if you prefer displaying the native force close dialog after ACRA is done.
     * Recommended: Keep this set to false if using interactions with user input.
     */
    val alsoReportToAndroidFramework: Boolean = false,

    /**
     * Add here your [android.content.SharedPreferences] identifier Strings if you use others than your application's default. They will be added to the [ReportField.SHARED_PREFERENCES] field.
     */
    val additionalSharedPreferences: List<String> = emptyList(),

    /**
     * Set this to true if you want to include only logcat lines related to your Application process. Note that this is always done by android starting with API 16 (Jellybean)
     */
    val logcatFilterByPid: Boolean = true,

    /**
     * Set this to true if you want to read logcat lines in a non blocking way for your thread. It has a default timeout of 3 seconds.
     */
    val logcatReadNonBlocking: Boolean = false,

    /**
     * Set this to false if you want to disable sending reports in development mode. Only signed application packages will send reports.
     */
    val sendReportsInDevMode: Boolean = true,

    /**
     * Provide here regex patterns to be evaluated on each [android.content.SharedPreferences] key to exclude KV pairs from the collected SharedPreferences.
     * This allows you to exclude sensitive user data like passwords from being collected.
     *
     * If you only want to include some keys, you may use regular expressions to do so:
     * <table summary="examples">
     *    <tr><td>only keys foo and bar</td><td><pre>"^(?!foo|bar).*$"</pre></td></tr>
     *    <tr><td>only keys <i>containing</i> foo and bar</td><td><pre>"^((?!foo|bar).)*$"</pre></td></tr>
     * </table>
     */
    val excludeMatchingSharedPreferencesKeys: List<String> = emptyList(),

    /**
     * Provide here regex patterns to be evaluated on each [android.provider.Settings.System}, [android.provider.Settings.Secure] and [android.provider.Settings.Global] key to exclude KV pairs from being collected.
     * This allows you to exclude sensitive data from being collected.
     *
     * If you only want to include some keys, you may use regular expressions to do so:
     * <table summary="examples">
     *    <tr><td>only keys foo and bar</td><td><pre>"^(?!foo|bar).*$"</pre></td></tr>
     *    <tr><td>only keys <i>containing</i> foo and bar</td><td><pre>"^((?!foo|bar).)*$"</pre></td></tr>
     * </table>
     */
    val excludeMatchingSettingsKeys: List<String> = emptyList(),

    /**
     * The default value will be a BuildConfig class residing in the same package as the Application class.
     * You only have to set this option if your BuildConfig class is obfuscated.
     */
    val buildConfigClass: Class<*>? = null,

    /**
     * To use in combination with [ReportField.APPLICATION_LOG] to set the path/name of your application log file.
     */
    val applicationLogFile: String? = null,

    /**
     * To use in combination with [ReportField.APPLICATION_LOG] to set the number of latest lines of your application log file to be collected.
     * Default value is 100.
     */
    val applicationLogFileLines: Int = ACRAConstants.DEFAULT_LOG_LINES,

    /**
     * To use in combination with [ReportField.APPLICATION_LOG] to set the root for the path provided in [applicationLogFile]
     */
    val applicationLogFileDir: Directory = Directory.FILES_LEGACY,

    /**
     * Implement a custom [RetryPolicy] to decide if a failed report should be resent or not.
     * @since 4.9.1
     */
    val retryPolicyClass: Class<out RetryPolicy> = DefaultRetryPolicy::class.java,

    /**
     * If you have services which might crash on startup android will try to restart them indefinitely. Set this to true to prevent that.
     * @since 4.9.2
     */
    val stopServicesOnCrash: Boolean = false,

    /**
     * Allows to attach files to crash reports.
     * <p>
     * ACRA contains a file provider under the following Uri:
     * <code>content://[applicationId].acra/[Directory]/[Path]</code>
     * where <code>[applicationId]</code> is your application package name, <code>[Directory]</code> is one of the enum constants in [Directory] in lower case and <code>[Path]</code> is the relative path to the file in that directory
     * e.g. content://org.acra.test.acra/files/thisIsATest.txt
     * </p>
     * Side effects:
     *
     * - POST mode: requests will be sent with content-type multipart/form-data
     * - PUT mode: There will be additional requests with the attachments. Naming scheme: <reportId>-<filename>
     * - EMAIL mode: Some email clients do not support attachments, so some emails may lack these attachments. Note that attachments might be readable to email clients when they are sent.
     *
     * @since 4.9.3
     */
    val attachmentUris: List<String> = emptyList(),

    /**
     * Allows [attachmentUris] configuration at runtime instead of compile time.
     * @since 4.9.3
     */
    val attachmentUriProvider: Class<out AttachmentUriProvider> = DefaultAttachmentProvider::class.java,

    /**
     * Toast shown when a report is sent successfully
     * @since 5.0.0
     */
    val reportSendSuccessToast: String? = null,

    /**
     * Toast shown when report sending fails
     * @since 5.0.0
     */
    val reportSendFailureToast: String? = null,

    /**
     * Format in which the report should be sent
     * @since 5.0.0
     */
    val reportFormat: StringFormat = StringFormat.JSON,

    /**
     * Allow parallel collection. Increases performance but might pollute e.g. logcat output
     * @since 5.0.1
     */
    val parallel: Boolean = true,

    /**
     * Allows custom plugin loading
     */
    val pluginLoader: PluginLoader = ServicePluginLoader(),

    /**
     * Plugin configurations
     */
    val pluginConfigurations: List<Configuration> = emptyList(),
) : Serializable, Configuration {
    override fun enabled(): Boolean = true
}
