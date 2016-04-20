/*
 *  Copyright 2011 Kevin Gaudin
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
package org.acra.config;

import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.builder.ReportPrimer;
import org.acra.dialog.BaseCrashReportDialog;
import org.acra.dialog.CrashReportDialog;
import org.acra.security.KeyStoreFactory;
import org.acra.sender.HttpSender;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.acra.sender.ReportSenderFactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the configuration that ACRA will use when handling crash reports.
 *
 * ACRAConfiguration should be considered immutable. To that end, as of 4.8.1
 * all of the set methods have been deprecated.
 *
 * Use {@link ConfigurationBuilder} to programmatically construct an ACRAConfiguration.
 */
@SuppressWarnings("unused")
public final class ACRAConfiguration implements Serializable {

    @Nullable
    private final Class<? extends Annotation> annotationType;

    // TODO Make all of these attributes final in ACRA 4.9 or 5.0
    // consider using immutable Collections for Arrays and Collections, so this can be truly final
    private String[] additionalDropBoxTags;
    private String[] additionalSharedPreferences;
    private int connectionTimeout;
    private Set<ReportField> reportContent;
    private boolean deleteUnapprovedReportsOnApplicationStart;
    private boolean deleteOldUnsentReportsOnApplicationStart;
    private int dropboxCollectionMinutes;
    private boolean alsoReportToAndroidFramework;
    private String formUri;
    private String formUriBasicAuthLogin;
    private String formUriBasicAuthPassword;
    private boolean includeDropBoxSystemTags;

    private String[] logcatArguments;
    private String mailTo;
    private ReportingInteractionMode reportingInteractionMode;
    private Class<? extends BaseCrashReportDialog> reportDialogClass;
    private Class<? extends ReportPrimer> reportPrimerClass;

    @StringRes
    private int resDialogPositiveButtonText;
    @StringRes
    private int resDialogNegativeButtonText;
    @StringRes
    private int resDialogCommentPrompt;
    @StringRes
    private int resDialogEmailPrompt;
    @DrawableRes
    private int resDialogIcon;
    @StringRes
    private int resDialogOkToast;
    @StringRes
    private int resDialogText;
    @StringRes
    private int resDialogTitle;
    @StyleRes
    private int resDialogTheme;
    @DrawableRes
    private int resNotifIcon;
    @StringRes
    private int resNotifText;
    @StringRes
    private int resNotifTickerText;
    @StringRes
    private int resNotifTitle;
    @StringRes
    private int resToastText;
    private int sharedPreferencesMode;
    private String sharedPreferencesName;
    private int socketTimeout;
    private boolean logcatFilterByPid;
    private boolean sendReportsInDevMode;

    private String[] excludeMatchingSharedPreferencesKeys;
    private String[] excludeMatchingSettingsKeys;
    @Nullable
    private Class buildConfigClass;
    private String applicationLogFile;
    private int applicationLogFileLines;

    private Method httpMethod;
    private Type reportType;
    private final Map<String, String> httpHeaders = new HashMap<String, String>();
    private Class<? extends KeyStoreFactory> keyStoreFactoryClass;
    private Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses;
    @RawRes
    private int resCertificate;
    private String certificatePath;
    private String certificateType;

    /**
     * @param builder ConfigurationBuilder with which to initialise this {@link ACRAConfiguration}.
     */
    ACRAConfiguration(@NonNull ConfigurationBuilder builder) {
        //noinspection ConstantConditions (don't rely on annotations alone)
        if (builder == null) {
            throw new NullPointerException("A ConfigurationBuilder must be supplied to ACRAConfiguration");
        }

        annotationType = builder.annotationType;

        additionalDropBoxTags = copyArray(builder.additionalDropBoxTags());
        additionalSharedPreferences = copyArray(builder.additionalSharedPreferences());
        connectionTimeout = builder.connectionTimeout();
        reportContent = new HashSet<ReportField>(builder.reportContent());
        deleteUnapprovedReportsOnApplicationStart = builder.deleteUnapprovedReportsOnApplicationStart();
        deleteOldUnsentReportsOnApplicationStart = builder.deleteOldUnsentReportsOnApplicationStart();
        dropboxCollectionMinutes = builder.dropboxCollectionMinutes();
        alsoReportToAndroidFramework = builder.alsoReportToAndroidFramework();
        formUri = builder.formUri();
        formUriBasicAuthLogin = builder.formUriBasicAuthLogin();
        formUriBasicAuthPassword = builder.formUriBasicAuthPassword();
        includeDropBoxSystemTags = builder.includeDropBoxSystemTags();
        logcatArguments = copyArray(builder.logcatArguments());
        mailTo = builder.mailTo();
        reportingInteractionMode = builder.reportingInteractionMode();
        resDialogIcon = builder.resDialogIcon();
        resDialogPositiveButtonText = builder.resDialogPositiveButtonText();
        resDialogNegativeButtonText = builder.resDialogNegativeButtonText();
        resDialogCommentPrompt = builder.resDialogCommentPrompt();
        resDialogEmailPrompt = builder.resDialogEmailPrompt();
        resDialogOkToast = builder.resDialogOkToast();
        resDialogText = builder.resDialogText();
        resDialogTitle = builder.resDialogTitle();
        resDialogTheme = builder.resDialogTheme();
        resNotifIcon = builder.resNotifIcon();
        resNotifText = builder.resNotifText();
        resNotifTickerText = builder.resNotifTickerText();
        resNotifTitle = builder.resNotifTitle();
        resToastText = builder.resToastText();
        sharedPreferencesMode = builder.sharedPreferencesMode();
        sharedPreferencesName = builder.sharedPreferencesName();
        socketTimeout = builder.socketTimeout();
        logcatFilterByPid = builder.logcatFilterByPid();
        sendReportsInDevMode = builder.sendReportsInDevMode();
        excludeMatchingSharedPreferencesKeys = copyArray(builder.excludeMatchingSharedPreferencesKeys());
        excludeMatchingSettingsKeys = copyArray(builder.excludeMatchingSettingsKeys());
        buildConfigClass = builder.buildConfigClass();
        applicationLogFile = builder.applicationLogFile();
        applicationLogFileLines = builder.applicationLogFileLines();
        reportDialogClass = builder.reportDialogClass();
        reportPrimerClass = builder.reportPrimerClass();
        httpMethod = builder.httpMethod();
        httpHeaders.putAll(builder.httpHeaders());
        reportType = builder.reportType();
        reportSenderFactoryClasses = copyArray(builder.reportSenderFactoryClasses());
        keyStoreFactoryClass = builder.keyStoreFactoryClass();
        resCertificate = builder.resCertificate();
        certificatePath = builder.certificatePath();
        certificateType = builder.certificateType();
    }


    /**
     * Set custom HTTP headers to be sent by the provided {@link HttpSender}.
     * This should be used also by third party senders.
     * 
     * @param headers
     *            A map associating HTTP header names to their values.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setHttpHeaders(@NonNull Map<String, String> headers) {
        this.httpHeaders.clear();
        this.httpHeaders.putAll(headers);
        return this;
    }

    /**
     * Retrieve HTTP headers defined by the application developer. These should
     * be added to requests sent by any third-party sender (over HTTP of
     * course).
     *
     * @return A map associating http header names to their values.
     */
    @NonNull
    public Map<String, String> getHttpHeaders() {
        return Collections.unmodifiableMap(httpHeaders);
    }

    /**
     * @return List of ReportField that ACRA will provide to the server.
     */
    @NonNull
    public List<ReportField> getReportFields() {
        return new ArrayList<ReportField>(reportContent);
    }

    /**
     * @param additionalDropboxTags the additionalDropboxTags to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setAdditionalDropboxTags(@NonNull String[] additionalDropboxTags) {
        this.additionalDropBoxTags = copyArray(additionalDropboxTags);
        return this;
    }

    /**
     * @param additionalSharedPreferences the additionalSharedPreferences to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setAdditionalSharedPreferences(@NonNull String[] additionalSharedPreferences) {
        this.additionalSharedPreferences = copyArray(additionalSharedPreferences);
        return this;
    }

    /**
     * @param connectionTimeout the connectionTimeout to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * @param customReportContent the customReportContent to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setCustomReportContent(@NonNull ReportField[] customReportContent) {
        this.reportContent = new HashSet<ReportField>(Arrays.asList(customReportContent));
        return this;
    }

    /**
     * @param deleteUnapprovedReportsOnApplicationStart the deleteUnapprovedReportsOnApplicationStart to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setDeleteUnapprovedReportsOnApplicationStart(boolean deleteUnapprovedReportsOnApplicationStart) {
        this.deleteUnapprovedReportsOnApplicationStart = deleteUnapprovedReportsOnApplicationStart;
        return this;
    }

    /**
     * @param deleteOldUnsentReportsOnApplicationStart When to delete old (unsent) reports on startup.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setDeleteOldUnsentReportsOnApplicationStart(boolean deleteOldUnsentReportsOnApplicationStart) {
        this.deleteOldUnsentReportsOnApplicationStart = deleteOldUnsentReportsOnApplicationStart;
        return this;
    }

    /**
     * @param dropboxCollectionMinutes the dropboxCollectionMinutes to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setDropboxCollectionMinutes(int dropboxCollectionMinutes) {
        this.dropboxCollectionMinutes = dropboxCollectionMinutes;
        return this;
    }

    /**
     * @param forceCloseDialogAfterToast the alsoReportToAndroidFramework to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setForceCloseDialogAfterToast(boolean forceCloseDialogAfterToast) {
        this.alsoReportToAndroidFramework = forceCloseDialogAfterToast;
        return this;
    }

    /**
     * Modify the formUri of your backend server receiving reports.
     *
     * @param formUri formUri to set.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setFormUri(@Nullable String formUri) {
        this.formUri = formUri;
        return this;
    }

    /**
     * @param formUriBasicAuthLogin the formUriBasicAuthLogin to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setFormUriBasicAuthLogin(@Nullable String formUriBasicAuthLogin) {
        this.formUriBasicAuthLogin = formUriBasicAuthLogin;
        return this;
    }

    /**
     * @param formUriBasicAuthPassword the formUriBasicAuthPassword to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setFormUriBasicAuthPassword(@Nullable String formUriBasicAuthPassword) {
        this.formUriBasicAuthPassword = formUriBasicAuthPassword;
        return this;
    }

    /**
     * @param includeDropboxSystemTags the includeDropboxSystemTags to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setIncludeDropboxSystemTags(boolean includeDropboxSystemTags) {
        this.includeDropBoxSystemTags = includeDropboxSystemTags;
        return this;
    }

    /**
     * @param logcatArguments the logcatArguments to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setLogcatArguments(@NonNull String[] logcatArguments) {
        this.logcatArguments = copyArray(logcatArguments);
        return this;
    }

    /**
     * Modify the mailTo of the mail account receiving reports.
     *
     * @param mailTo mailTo to set.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setMailTo(@Nullable String mailTo) {
        this.mailTo = mailTo;
        return this;
    }

    /**
     * Change the current {@link ReportingInteractionMode}. You must set
     * required configuration items first.
     *
     * @param mode the new mode to set.
     * @return The updated ACRA configuration
     * @throws ACRAConfigurationException if a configuration item is missing for this mode.
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setMode(@NonNull ReportingInteractionMode mode) throws ACRAConfigurationException {
        this.reportingInteractionMode = mode;
        checkCrashResources();
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResDialogPositiveButtonText(@StringRes int resId) {
        resDialogPositiveButtonText = resId;
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResDialogNegativeButtonText(@StringRes int resId) {
        resDialogNegativeButtonText = resId;
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setReportDialogClass(@NonNull Class<? extends BaseCrashReportDialog> reportDialogClass) {
        this.reportDialogClass = reportDialogClass;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogCommentPrompt()} comes from an Android
     * Library Project.
     *
     * @param resId The resource id, see
     *              {@link ReportsCrashes#resDialogCommentPrompt()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResDialogCommentPrompt(@StringRes int resId) {
        resDialogCommentPrompt = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogEmailPrompt()} comes from an Android
     * Library Project.
     *
     * @param resId The resource id, see
     *              {@link ReportsCrashes#resDialogEmailPrompt()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResDialogEmailPrompt(@StringRes int resId) {
        resDialogEmailPrompt = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogIcon()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogIcon()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResDialogIcon(@DrawableRes int resId) {
        resDialogIcon = resId;
        return this;
    }

    /**
     * Use this method BEFORE if the id you wanted to give to
     * {@link ReportsCrashes#resDialogOkToast()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogOkToast()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResDialogOkToast(@StringRes int resId) {
        resDialogOkToast = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogText()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogText()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResDialogText(@StringRes int resId) {
        resDialogText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogTitle()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogTitle()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResDialogTitle(@StringRes int resId) {
        resDialogTitle = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifIcon()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resNotifIcon()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResNotifIcon(@DrawableRes int resId) {
        resNotifIcon = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifText()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resNotifText()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResNotifText(@StringRes int resId) {
        resNotifText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTickerText()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see
     *              {@link ReportsCrashes#resNotifTickerText()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResNotifTickerText(@StringRes int resId) {
        resNotifTickerText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTitle()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resNotifTitle()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResNotifTitle(@StringRes int resId) {
        resNotifTitle = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resToastText()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resToastText()}
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setResToastText(@StringRes int resId) {
        resToastText = resId;
        return this;
    }

    /**
     * @param sharedPreferenceMode the sharedPreferenceMode to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setSharedPreferenceMode(int sharedPreferenceMode) {
        this.sharedPreferencesMode = sharedPreferenceMode;
        return this;
    }

    /**
     * @param sharedPreferenceName the sharedPreferenceName to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setSharedPreferenceName(@NonNull String sharedPreferenceName) {
        this.sharedPreferencesName = sharedPreferenceName;
        return this;
    }

    /**
     * @param socketTimeout the socketTimeout to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * 
     * @param filterByPid
     *            true if you want to collect only logcat lines related to your
     *            application process.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setLogcatFilterByPid(boolean filterByPid) {
        logcatFilterByPid = filterByPid;
        return this;
    }

    /**
     * @param sendReportsInDevMode
     *            false if you want to disable sending reports in development
     *            mode. Reports will be sent only on signed applications.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setSendReportsInDevMode(boolean sendReportsInDevMode) {
        this.sendReportsInDevMode = sendReportsInDevMode;
        return this;
    }

    /**
     * @deprecated since 4.8.1 no replacement. Now that we are using the SenderService in a separate process it is always safe to send at shutdown.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setSendReportsAtShutdown(boolean sendReportsAtShutdown) {
        return this;
    }

    /**
     * 
     * @param excludeMatchingSharedPreferencesKeys
     *            an array of Strings containing regexp defining
     *            SharedPreferences keys that should be excluded from the data
     *            collection.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setExcludeMatchingSharedPreferencesKeys(@NonNull String[] excludeMatchingSharedPreferencesKeys) {
        this.excludeMatchingSharedPreferencesKeys = copyArray(excludeMatchingSharedPreferencesKeys);
        return this;
    }

    /**
     * @param excludeMatchingSettingsKeys
     *            an array of Strings containing regexp defining
     *            Settings.System, Settings.Secure and Settings.Global keys that
     *            should be excluded from the data collection.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setExcludeMatchingSettingsKeys(@NonNull String[] excludeMatchingSettingsKeys) {
        this.excludeMatchingSettingsKeys = copyArray(excludeMatchingSettingsKeys);
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setBuildConfigClass(@Nullable Class buildConfigClass) {
        this.buildConfigClass = buildConfigClass;
        return this;
    }

    /**
     * @param applicationLogFile The path and file name of your application log file, to be
     *                           used with {@link ReportField#APPLICATION_LOG}.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setApplicationLogFile(@NonNull String applicationLogFile) {
        this.applicationLogFile = applicationLogFile;
        return this;
    }

    /**
     * @param applicationLogFileLines The number of lines of your application log to be collected,
     *                                to be used with {@link ReportField#APPLICATION_LOG} and
     *                                {@link ReportsCrashes#applicationLogFile()}.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setApplicationLogFileLines(int applicationLogFileLines) {
        this.applicationLogFileLines = applicationLogFileLines;
        return this;
    }

    /**
     * @param httpMethod The method to be used to send data to the server.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setHttpMethod(@NonNull Method httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * @param type The type of content encoding to be used to send data to the server.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    public ACRAConfiguration setReportType(@NonNull Type type) {
        reportType = type;
        return this;
    }

    /**
     * @param keyStore Set this to the keystore that contains the trusted certificates
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    public void setKeyStore(@Nullable KeyStore keyStore) {
        throw new UnsupportedOperationException("This method is not supported anymore");
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    public void setReportSenderFactoryClasses(@NonNull Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses) {
        this.reportSenderFactoryClasses = copyArray(reportSenderFactoryClasses);
    }

    @NonNull
    public String[] additionalDropBoxTags() {
        return copyArray(additionalDropBoxTags);
    }

    @NonNull
    public String[] additionalSharedPreferences() {
        return copyArray(additionalSharedPreferences);
    }

    /**
     * @deprecated since 4.8.1 no replacement.
     */
    @Nullable
    public Class<? extends Annotation> annotationType() {
        return annotationType; // Why would this ever be needed?
    }

    public int connectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @deprecated since 4.8.6 use {@link #getReportFields()} instead
     * TODO remove with setters in 4.9 / 5.0
     */
    @NonNull
    public ReportField[] customReportContent() {
        return reportContent.toArray(new ReportField[reportContent.size()]);
    }

    public boolean deleteUnapprovedReportsOnApplicationStart() {
        return deleteUnapprovedReportsOnApplicationStart;
    }

    public boolean deleteOldUnsentReportsOnApplicationStart() {
        return deleteOldUnsentReportsOnApplicationStart;
    }

    public int dropboxCollectionMinutes() {
        return dropboxCollectionMinutes;
    }

    public boolean alsoReportToAndroidFramework() {
        return alsoReportToAndroidFramework;
    }

    @Nullable
    public String formUri() {
        return formUri;
    }

    @Nullable
    public String formUriBasicAuthLogin() {
        return formUriBasicAuthLogin;
    }

    @Nullable
    public String formUriBasicAuthPassword() {
        return formUriBasicAuthPassword;
    }

    public boolean includeDropBoxSystemTags() {
        return includeDropBoxSystemTags;
    }

    @NonNull
    public String[] logcatArguments() {
        return copyArray(logcatArguments);
    }

    @Nullable
    public String mailTo() {
        return mailTo;
    }

    @NonNull
    public ReportingInteractionMode mode() {
        return reportingInteractionMode;
    }

    @StringRes
    public int resDialogPositiveButtonText() {
        return resDialogPositiveButtonText;
    }

    @StringRes
    public int resDialogNegativeButtonText() {
        return resDialogNegativeButtonText;
    }

    @StringRes
    public int resDialogCommentPrompt() {
        return resDialogCommentPrompt;
    }

    @StringRes
    public int resDialogEmailPrompt() {
        return resDialogEmailPrompt;
    }

    @DrawableRes
    public int resDialogIcon() {
        return resDialogIcon;
    }

    @StringRes
    public int resDialogOkToast() {
        return resDialogOkToast;
    }

    @StringRes
    public int resDialogText() {
        return resDialogText;
    }

    @StringRes
    public int resDialogTitle() {
        return resDialogTitle;
    }

    @StyleRes
    public int resDialogTheme() {
        return resDialogTheme;
    }

    @DrawableRes
    public int resNotifIcon() {
        return resNotifIcon;
    }

    @StringRes
    public int resNotifText() {
        return resNotifText;
    }

    @StringRes
    public int resNotifTickerText() {
        return resNotifTickerText;
    }

    @StringRes
    public int resNotifTitle() {
        return resNotifTitle;
    }

    @StringRes
    public int resToastText() {
        return resToastText;
    }

    public int sharedPreferencesMode() {
        return sharedPreferencesMode;
    }

    @NonNull
    public String sharedPreferencesName() {
        return sharedPreferencesName;
    }

    public int socketTimeout() {
        return socketTimeout;
    }

    public boolean logcatFilterByPid() {
        return logcatFilterByPid;
    }

    public boolean sendReportsInDevMode() {
        return sendReportsInDevMode;
    }

    @NonNull
    public String[] excludeMatchingSharedPreferencesKeys() {
        return copyArray(excludeMatchingSharedPreferencesKeys);
    }

    @NonNull
    public String[] excludeMatchingSettingsKeys() {
        return copyArray(excludeMatchingSettingsKeys);
    }

    /**
     * Will return null if no value has been configured.
     * It is up to clients to construct the recommended default value oof context.getClass().getPackage().getName() + BuildConfig.class
     */
    @Nullable
    public Class buildConfigClass() {
        return buildConfigClass;
    }

    @NonNull
    public String applicationLogFile() {
        return applicationLogFile;
    }

    public int applicationLogFileLines() {
        return applicationLogFileLines;
    }

    @NonNull
    public Class<? extends BaseCrashReportDialog> reportDialogClass() {
        return reportDialogClass;
    }

    @NonNull
    public Class<? extends ReportPrimer> reportPrimerClass() {
        return reportPrimerClass;
    }

    @NonNull
    public Method httpMethod() {
        return httpMethod;
    }

    @NonNull
    public Type reportType() {
        return reportType;
    }

    @NonNull
    public Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses() {
        return copyArray(reportSenderFactoryClasses);
    }

    @NonNull
    public Class<? extends KeyStoreFactory> keyStoreFactoryClass() {
        return keyStoreFactoryClass;
    }

    @RawRes
    public int resCertificate() {
        return resCertificate;
    }

    public String certificatePath() {
        return certificatePath;
    }

    public String certificateType() {
        return certificateType;
    }

    /**
     * Checks that mandatory configuration items have been provided.
     *
     * @throws ACRAConfigurationException if required values are missing.
     */
    public void checkCrashResources() throws ACRAConfigurationException {
        switch (mode()) {
            case TOAST:
                if (resToastText() == ACRAConstants.DEFAULT_RES_VALUE) {
                    throw new ACRAConfigurationException(
                            "TOAST mode: you have to define the resToastText parameter in your application @ReportsCrashes() annotation.");
                }
                break;
            case NOTIFICATION:
                if (resNotifTickerText() == ACRAConstants.DEFAULT_RES_VALUE || resNotifTitle() == ACRAConstants.DEFAULT_RES_VALUE || resNotifText() == ACRAConstants.DEFAULT_RES_VALUE) {
                    throw new ACRAConfigurationException(
                            "NOTIFICATION mode: you have to define at least the resNotifTickerText, resNotifTitle, resNotifText parameters in your application @ReportsCrashes() annotation.");
                }
                if (CrashReportDialog.class.equals(reportDialogClass()) && resDialogText() == ACRAConstants.DEFAULT_RES_VALUE) {
                    throw new ACRAConfigurationException(
                            "NOTIFICATION mode: using the (default) CrashReportDialog requires you have to define the resDialogText parameter in your application @ReportsCrashes() annotation.");
                }
                break;
            case DIALOG:
                if (CrashReportDialog.class.equals(reportDialogClass()) && resDialogText() == ACRAConstants.DEFAULT_RES_VALUE) {
                    throw new ACRAConfigurationException(
                            "DIALOG mode: using the (default) CrashReportDialog requires you to define the resDialogText parameter in your application @ReportsCrashes() annotation.");
                }
                break;
            default:
                break;
        }
    }

    @NonNull
    private static <T> T[] copyArray(@NonNull T[] source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Arrays.copyOf(source, source.length);
        }
        //noinspection unchecked
        T[] result = (T[]) Array.newInstance(source.getClass().getComponentType(), source.length);
        System.arraycopy(source, 0, result, 0, source.length);
        return result;
    }
}
