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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
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
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

/**
 * Represents the configuration that ACRA will use when handling crash reports.
 *
 * ACRAConfiguration should be considered immutable. To that end, as of 4.8.1
 * all of the set methods have been deprecated.
 *
 * Use {@link ConfigurationBuilder} to programmatically construct an ACRAConfiguration.
 */
public final class ACRAConfiguration implements Serializable {

    @Nullable
    private final Class<? extends Annotation> annotationType;

    // TODO Make all of these attributes final in ACRA 4.9 or 5.0
    private String[] additionalDropBoxTags;
    private String[] additionalSharedPreferences;
    private Integer connectionTimeout;
    private ReportField[] customReportContent;
    private Boolean deleteUnapprovedReportsOnApplicationStart;
    private Boolean deleteOldUnsentReportsOnApplicationStart;
    private Integer dropboxCollectionMinutes;
    private Boolean forceCloseDialogAfterToast;
    private String formUri;
    private String formUriBasicAuthLogin;
    private String formUriBasicAuthPassword;
    private Boolean includeDropBoxSystemTags;

    private String[] logcatArguments;
    private String mailTo;
    private ReportingInteractionMode reportingInteractionMode;
    private Class<? extends BaseCrashReportDialog> reportDialogClass;
    private Class<? extends ReportPrimer> reportPrimerClass;

    private Integer resDialogPositiveButtonText;
    private Integer resDialogNegativeButtonText;
    private Integer resDialogCommentPrompt;
    private Integer resDialogEmailPrompt;
    private Integer resDialogIcon;
    private Integer resDialogOkToast;
    private Integer resDialogText;
    private Integer resDialogTitle;
    private Integer resNotifIcon;
    private Integer resNotifText;
    private Integer resNotifTickerText;
    private Integer resNotifTitle;
    private Integer resToastText;
    private Integer sharedPreferencesMode;
    private String sharedPreferencesName;
    private Integer socketTimeout;
    private Boolean logcatFilterByPid;
    private Boolean sendReportsInDevMode;
    private Boolean sendReportsAtShutdown;

    private String[] excludeMatchingSharedPreferencesKeys;
    private String[] excludeMatchingSettingsKeys;
    @Nullable
    private Class buildConfigClass;
    private String applicationLogFile;
    private Integer applicationLogFileLines;

    private Method httpMethod;
    private Type reportType;
    private final Map<String, String> httpHeaders = new HashMap<String, String>();
    private KeyStoreFactory keyStoreFactory;
    private Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses;

    /**
     * @param builder  ConfigurationBuilder with which to initialise this {@link ACRAConfiguration}.
     */
    ACRAConfiguration(@Nullable ConfigurationBuilder builder) {
        if (builder == null) {
            throw new NullPointerException("A ConfigurationBuilder must be supplied to ACRAConfiguration");
        }

        annotationType = builder.annotationType;

        additionalDropBoxTags = copyArray(builder.additionalDropBoxTags());
        additionalSharedPreferences = copyArray(builder.additionalSharedPreferences());
        connectionTimeout = builder.connectionTimeout();
        customReportContent = copyArray(builder.customReportContent());
        deleteUnapprovedReportsOnApplicationStart = builder.deleteUnapprovedReportsOnApplicationStart();
        deleteOldUnsentReportsOnApplicationStart = builder.deleteOldUnsentReportsOnApplicationStart();
        dropboxCollectionMinutes = builder.dropboxCollectionMinutes();
        forceCloseDialogAfterToast = builder.forceCloseDialogAfterToast();
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
        sendReportsAtShutdown = builder.sendReportsAtShutdown();
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
        keyStoreFactory = builder.keyStoreFactory();
    }

    /**
     * Empty constructor which sets no defaults.
     */
    @SuppressWarnings( "unused" )
    public ACRAConfiguration(){
        this(null);
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
    @SuppressWarnings("unused")
    public Map<String, String> getHttpHeaders() {
        return Collections.unmodifiableMap(httpHeaders);
    }

    /**
     * @return List of ReportField that ACRA will provide to the server.
     */
    @SuppressWarnings("unused")
    public List<ReportField> getReportFields() {
        final ReportField[] customReportFields = customReportContent();

        final ReportField[] fieldsList;
        if (customReportFields.length != 0) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using custom Report Fields");
            fieldsList = customReportFields;
        } else if (mailTo() == null || "".equals(mailTo())) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using default Report Fields");
            fieldsList = ACRAConstants.DEFAULT_REPORT_FIELDS;
        } else {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using default Mail Report Fields");
            fieldsList = ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS;
        }
        return Arrays.asList(fieldsList);
    }

    /**
     * @param additionalDropboxTags
     *            the additionalDropboxTags to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setAdditionalDropboxTags(String[] additionalDropboxTags) {
        this.additionalDropBoxTags = additionalDropboxTags;
        return this;
    }

    /**
     * @param additionalSharedPreferences
     *            the additionalSharedPreferences to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setAdditionalSharedPreferences(String[] additionalSharedPreferences) {
        this.additionalSharedPreferences = additionalSharedPreferences;
        return this;
    }

    /**
     * @param connectionTimeout
     *            the connectionTimeout to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * @param customReportContent
     *            the customReportContent to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setCustomReportContent(ReportField[] customReportContent) {
        this.customReportContent = customReportContent;
        return this;
    }

    /**
     * @param deleteUnapprovedReportsOnApplicationStart
     *            the deleteUnapprovedReportsOnApplicationStart to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setDeleteUnapprovedReportsOnApplicationStart(Boolean deleteUnapprovedReportsOnApplicationStart) {
        this.deleteUnapprovedReportsOnApplicationStart = deleteUnapprovedReportsOnApplicationStart;
        return this;
    }

    /**
     * @param deleteOldUnsentReportsOnApplicationStart    When to delete old (unsent) reports on startup.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setDeleteOldUnsentReportsOnApplicationStart(Boolean deleteOldUnsentReportsOnApplicationStart) {
        this.deleteOldUnsentReportsOnApplicationStart = deleteOldUnsentReportsOnApplicationStart;
        return this;
    }

    /**
     * @param dropboxCollectionMinutes
     *            the dropboxCollectionMinutes to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setDropboxCollectionMinutes(Integer dropboxCollectionMinutes) {
        this.dropboxCollectionMinutes = dropboxCollectionMinutes;
        return this;
    }

    /**
     * @param forceCloseDialogAfterToast
     *            the forceCloseDialogAfterToast to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setForceCloseDialogAfterToast(Boolean forceCloseDialogAfterToast) {
        this.forceCloseDialogAfterToast = forceCloseDialogAfterToast;
        return this;
    }

    /**
     * Modify the formUri of your backend server receiving reports.
     * 
     * @param formUri   formUri to set.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setFormUri(String formUri) {
        this.formUri = formUri;
        return this;
    }

    /**
     * @param formUriBasicAuthLogin
     *            the formUriBasicAuthLogin to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setFormUriBasicAuthLogin(String formUriBasicAuthLogin) {
        this.formUriBasicAuthLogin = formUriBasicAuthLogin;
        return this;
    }

    /**
     * @param formUriBasicAuthPassword
     *            the formUriBasicAuthPassword to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setFormUriBasicAuthPassword(String formUriBasicAuthPassword) {
        this.formUriBasicAuthPassword = formUriBasicAuthPassword;
        return this;
    }

    /**
     * @param includeDropboxSystemTags
     *            the includeDropboxSystemTags to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setIncludeDropboxSystemTags(Boolean includeDropboxSystemTags) {
        this.includeDropBoxSystemTags = includeDropboxSystemTags;
        return this;
    }

    /**
     * @param logcatArguments
     *            the logcatArguments to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setLogcatArguments(String[] logcatArguments) {
        this.logcatArguments = logcatArguments;
        return this;
    }

    /**
     * Modify the mailTo of the mail account receiving reports.
     * 
     * @param mailTo    mailTo to set.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setMailTo(String mailTo) {
        this.mailTo = mailTo;
        return this;
    }

    /**
     * Change the current {@link ReportingInteractionMode}. You must set
     * required configuration items first.
     * 
     * @param mode
     *            the new mode to set.
     * @return The updated ACRA configuration
     * @throws ACRAConfigurationException
     *             if a configuration item is missing for this mode.
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setMode(ReportingInteractionMode mode) throws ACRAConfigurationException {
        this.reportingInteractionMode = mode;
        checkCrashResources();
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResDialogPositiveButtonText(int resId) {
        resDialogPositiveButtonText = resId;
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResDialogNegativeButtonText(int resId) {
        resDialogNegativeButtonText = resId;
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setReportDialogClass(Class<? extends BaseCrashReportDialog> reportDialogClass) {
        this.reportDialogClass = reportDialogClass;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogCommentPrompt()} comes from an Android
     * Library Project.
     * 
     * @param resId
     *            The resource id, see
     *            {@link ReportsCrashes#resDialogCommentPrompt()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResDialogCommentPrompt(int resId) {
        resDialogCommentPrompt = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogEmailPrompt()} comes from an Android
     * Library Project.
     * 
     * @param resId
     *            The resource id, see
     *            {@link ReportsCrashes#resDialogEmailPrompt()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResDialogEmailPrompt(int resId) {
        resDialogEmailPrompt = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogIcon()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogIcon()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResDialogIcon(int resId) {
        resDialogIcon = resId;
        return this;
    }

    /**
     * Use this method BEFORE if the id you wanted to give to
     * {@link ReportsCrashes#resDialogOkToast()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogOkToast()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResDialogOkToast(int resId) {
        resDialogOkToast = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogText()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResDialogText(int resId) {
        resDialogText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogTitle()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogTitle()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResDialogTitle(int resId) {
        resDialogTitle = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifIcon()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifIcon()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResNotifIcon(int resId) {
        resNotifIcon = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifText()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResNotifText(int resId) {
        resNotifText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTickerText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see
     *            {@link ReportsCrashes#resNotifTickerText()}
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResNotifTickerText(int resId) {
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
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResNotifTitle(int resId) {
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
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setResToastText(int resId) {
        resToastText = resId;
        return this;
    }

    /**
     * @param sharedPreferenceMode the sharedPreferenceMode to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setSharedPreferenceMode(Integer sharedPreferenceMode) {
        this.sharedPreferencesMode = sharedPreferenceMode;
        return this;
    }

    /**
     * @param sharedPreferenceName the sharedPreferenceName to set
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setSharedPreferenceName(String sharedPreferenceName) {
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
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setSocketTimeout(Integer socketTimeout) {
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
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setLogcatFilterByPid(Boolean filterByPid) {
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
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setSendReportsInDevMode(Boolean sendReportsInDevMode) {
        this.sendReportsInDevMode = sendReportsInDevMode;
        return this;
    }

    /**
     * @param sendReportsAtShutdown
     *            false if you want to disable sending reports at the time the
     *            exception is caught. Reports will be sent when the application
     *            is restarted.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setSendReportsAtShutdown(Boolean sendReportsAtShutdown) {
        this.sendReportsAtShutdown = sendReportsAtShutdown;
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
    public ACRAConfiguration setExcludeMatchingSharedPreferencesKeys(String[] excludeMatchingSharedPreferencesKeys) {
        this.excludeMatchingSharedPreferencesKeys = excludeMatchingSharedPreferencesKeys;
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
    public ACRAConfiguration setExcludeMatchingSettingsKeys(String[] excludeMatchingSettingsKeys) {
        this.excludeMatchingSettingsKeys = excludeMatchingSettingsKeys;
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setBuildConfigClass(@Nullable Class buildConfigClass) {
        this.buildConfigClass = buildConfigClass;
        return this;
    }

    /**
     * @param applicationLogFile
     *            The path and file name of your application log file, to be
     *            used with {@link ReportField#APPLICATION_LOG}.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setApplicationLogFile(String applicationLogFile) {
        this.applicationLogFile = applicationLogFile;
        return this;
    }

    /**
     * @param applicationLogFileLines
     *            The number of lines of your application log to be collected,
     *            to be used with {@link ReportField#APPLICATION_LOG} and
     *            {@link ReportsCrashes#applicationLogFile()}.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setApplicationLogFileLines(int applicationLogFileLines) {
        this.applicationLogFileLines = applicationLogFileLines;
        return this;
    }

    /**
     * @param httpMethod    The method to be used to send data to the server.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setHttpMethod(Method httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * @param type  The type of content encoding to be used to send data to the server.
     * @return The updated ACRA configuration
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration setReportType(Type type) {
        reportType = type;
        return this;
    }

    /**
     * @param keyStore  Set this to the keystore that contains the trusted certificates
     *
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @SuppressWarnings("unused")
    public void setKeyStore(KeyStore keyStore) {
        throw new UnsupportedOperationException("This method is not supported anymore");
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @SuppressWarnings("unused")
    public void setReportSenderFactoryClasses(Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses) {
        this.reportSenderFactoryClasses = reportSenderFactoryClasses;
    }

    @SuppressWarnings("unused")
    public String[] additionalDropBoxTags() {
        return additionalDropBoxTags;
    }

    @SuppressWarnings("unused")
    public String[] additionalSharedPreferences() {
        return additionalSharedPreferences;
    }

    /**
     * @deprecated since 4.8.1 no replacement.
     */
    @Nullable
    @SuppressWarnings("unused")
    public Class<? extends Annotation> annotationType() {
        return annotationType; // Why would this ever be needed?
    }

    @SuppressWarnings("unused")
    public int connectionTimeout() {
        return connectionTimeout;
    }

    @SuppressWarnings("unused")
    public ReportField[] customReportContent() {
        return customReportContent;
    }

    @SuppressWarnings("unused")
    public boolean deleteUnapprovedReportsOnApplicationStart() {
        return deleteUnapprovedReportsOnApplicationStart;
    }

    @SuppressWarnings("unused")
    public boolean deleteOldUnsentReportsOnApplicationStart() {
        return deleteOldUnsentReportsOnApplicationStart;
    }

    @SuppressWarnings("unused")
    public int dropboxCollectionMinutes() {
        return dropboxCollectionMinutes;
    }

    @SuppressWarnings("unused")
    public boolean forceCloseDialogAfterToast() {
        return forceCloseDialogAfterToast;
    }

    @SuppressWarnings("unused")
    public String formUri() {
        return formUri;
    }

    @SuppressWarnings("unused")
    public String formUriBasicAuthLogin() {
        return formUriBasicAuthLogin;
    }

    @SuppressWarnings("unused")
    public String formUriBasicAuthPassword() {
        return formUriBasicAuthPassword;
    }

    @SuppressWarnings("unused")
    public boolean includeDropBoxSystemTags() {
        return includeDropBoxSystemTags;
    }

    @SuppressWarnings("unused")
    public String[] logcatArguments() {
        return logcatArguments;
    }

    @SuppressWarnings("unused")
    public String mailTo() {
        return mailTo;
    }

    @SuppressWarnings("unused")
    public ReportingInteractionMode mode() {
        return reportingInteractionMode;
    }

    @SuppressWarnings("unused")
    public int resDialogPositiveButtonText() {
        return resDialogPositiveButtonText;
    }

    @SuppressWarnings("unused")
    public int resDialogNegativeButtonText() {
        return resDialogNegativeButtonText;
    }

    @SuppressWarnings("unused")
    public int resDialogCommentPrompt() {
        return resDialogCommentPrompt;
    }

    @SuppressWarnings("unused")
    public int resDialogEmailPrompt() {
        return resDialogEmailPrompt;
    }

    @SuppressWarnings("unused")
    public int resDialogIcon() {
        return resDialogIcon;
    }

    @SuppressWarnings("unused")
    public int resDialogOkToast() {
        return resDialogOkToast;
    }

    @SuppressWarnings("unused")
    public int resDialogText() {
        return resDialogText;
    }

    @SuppressWarnings("unused")
    public int resDialogTitle() {
        return resDialogTitle;
    }

    @SuppressWarnings("unused")
    public int resNotifIcon() {
        return resNotifIcon;
    }

    @SuppressWarnings("unused")
    public int resNotifText() {
        return resNotifText;
    }

    @SuppressWarnings("unused")
    public int resNotifTickerText() {
        return resNotifTickerText;
    }

    @SuppressWarnings("unused")
    public int resNotifTitle() {
        return resNotifTitle;
    }

    @SuppressWarnings("unused")
    public int resToastText() {
        return resToastText;
    }

    @SuppressWarnings("unused")
    public int sharedPreferencesMode() {
        return sharedPreferencesMode;
    }

    @SuppressWarnings("unused")
    public String sharedPreferencesName() {
        return sharedPreferencesName;
    }

    @SuppressWarnings("unused")
    public int socketTimeout() {
        return socketTimeout;
    }

    @SuppressWarnings("unused")
    public boolean logcatFilterByPid() {
        return logcatFilterByPid;
    }

    @SuppressWarnings("unused")
    public boolean sendReportsInDevMode() {
        return sendReportsInDevMode;
    }

    @SuppressWarnings("unused")
    public boolean sendReportsAtShutdown() {
        return sendReportsAtShutdown;
    }

    @SuppressWarnings("unused")
    public String[] excludeMatchingSharedPreferencesKeys() {
        return excludeMatchingSharedPreferencesKeys;
    }

    @SuppressWarnings("unused")
    public String[] excludeMatchingSettingsKeys() {
        return excludeMatchingSettingsKeys;
    }

    /**
     * Will return null if no value has been configured.
     * It is up to clients to construct the recommended default value oof context.getClass().getPackage().getName() + BuildConfig.class
     */
    @Nullable
    @SuppressWarnings("unused")
    public Class buildConfigClass() {
        return buildConfigClass;
    }

    @SuppressWarnings("unused")
    public String applicationLogFile() {
        return applicationLogFile;
    }

    @SuppressWarnings("unused")
    public int applicationLogFileLines() {
        return applicationLogFileLines;
    }

    @SuppressWarnings("unused")
    public Class<? extends BaseCrashReportDialog> reportDialogClass() {
        return reportDialogClass;
    }

    @SuppressWarnings("unused")
    public Class<? extends ReportPrimer> reportPrimerClass() {
        return reportPrimerClass;
    }

    @SuppressWarnings("unused")
    public Method httpMethod() {
        return httpMethod;
    }

    @SuppressWarnings("unused")
    public Type reportType() {
        return reportType;
    }

    @SuppressWarnings("unused")
    public Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses() {
        return reportSenderFactoryClasses;
    }

    @SuppressWarnings("unused")
    public KeyStoreFactory keyStoreFactory() {
        return keyStoreFactory;
    }

    /**
     * Checks that mandatory configuration items have been provided.
     *
     * @throws ACRAConfigurationException if required values are missing.
     */
    public void checkCrashResources() throws ACRAConfigurationException {
        switch (mode()) {
            case TOAST:
                if (resToastText() == 0) {
                    throw new ACRAConfigurationException(
                            "TOAST mode: you have to define the resToastText parameter in your application @ReportsCrashes() annotation.");
                }
                break;
            case NOTIFICATION:
                if (resNotifTickerText() == 0 || resNotifTitle() == 0 || resNotifText() == 0) {
                    throw new ACRAConfigurationException(
                            "NOTIFICATION mode: you have to define at least the resNotifTickerText, resNotifTitle, resNotifText parameters in your application @ReportsCrashes() annotation.");
                }
                if (CrashReportDialog.class.equals(reportDialogClass()) && resDialogText() == 0) {
                    throw new ACRAConfigurationException(
                            "NOTIFICATION mode: using the (default) CrashReportDialog requires you have to define the resDialogText parameter in your application @ReportsCrashes() annotation.");
                }
                break;
            case DIALOG:
                if (CrashReportDialog.class.equals(reportDialogClass()) && resDialogText() == 0) {
                    throw new ACRAConfigurationException(
                            "DIALOG mode: using the (default) CrashReportDialog requires you to define the resDialogText parameter in your application @ReportsCrashes() annotation.");
                }
                break;
            default:
                break;
        }
    }

    @NonNull
    private static String[] copyArray(@NonNull String[] source) {
        final String[] target = new String[source.length];
        System.arraycopy(source, 0, target, 0, source.length);
        return target;
    }

    @NonNull
    private static ReportField[] copyArray(@NonNull ReportField[] source) {
        final ReportField[] target = new ReportField[source.length];
        System.arraycopy(source, 0, target, 0, source.length);
        return target;
    }

    @NonNull
    private static Class<? extends ReportSenderFactory>[] copyArray(@NonNull Class<? extends ReportSenderFactory>[] source) {
        final Class<? extends ReportSenderFactory>[] target = new Class[source.length];
        System.arraycopy(source, 0, target, 0, source.length);
        return target;
    }
}
