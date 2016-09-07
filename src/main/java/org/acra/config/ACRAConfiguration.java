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

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;

import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.builder.ReportPrimer;
import org.acra.collections.ImmutableList;
import org.acra.dialog.BaseCrashReportDialog;
import org.acra.file.Directory;
import org.acra.security.KeyStoreFactory;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.acra.sender.ReportSenderFactory;
import org.acra.collections.ImmutableMap;
import org.acra.collections.ImmutableSet;

import java.io.Serializable;

/**
 * Represents the configuration that ACRA will use when handling crash reports.
 *
 * Use {@link ConfigurationBuilder} to programmatically construct an ACRAConfiguration.
 */
public final class ACRAConfiguration implements Serializable {

    private final ImmutableSet<String> additionalDropBoxTags;
    private final ImmutableSet<String> additionalSharedPreferences;
    private final int connectionTimeout;
    private final ImmutableSet<ReportField> reportContent;
    private final boolean deleteUnapprovedReportsOnApplicationStart;
    private final boolean deleteOldUnsentReportsOnApplicationStart;
    private final int dropboxCollectionMinutes;
    private final boolean alsoReportToAndroidFramework;
    private final String formUri;
    private final String formUriBasicAuthLogin;
    private final String formUriBasicAuthPassword;
    private final boolean includeDropBoxSystemTags;

    private final ImmutableList<String> logcatArguments;
    private final String mailTo;
    private final ReportingInteractionMode reportingInteractionMode;
    private final Class<? extends BaseCrashReportDialog> reportDialogClass;
    private final Class<? extends ReportPrimer> reportPrimerClass;

    @StringRes
    private final int resDialogPositiveButtonText;
    @StringRes
    private final int resDialogNegativeButtonText;
    @StringRes
    private final int resDialogCommentPrompt;
    @StringRes
    private final int resDialogEmailPrompt;
    @DrawableRes
    private final int resDialogIcon;
    @StringRes
    private final int resDialogOkToast;
    @StringRes
    private final int resDialogText;
    @StringRes
    private final int resDialogTitle;
    @StyleRes
    private final int resDialogTheme;
    @DrawableRes
    private final int resNotifIcon;
    @StringRes
    private final int resNotifText;
    @StringRes
    private final int resNotifTickerText;
    @StringRes
    private final int resNotifTitle;
    @StringRes
    private final int resToastText;
    private final int sharedPreferencesMode;
    private final String sharedPreferencesName;
    private final int socketTimeout;
    private final boolean logcatFilterByPid;
    private final boolean nonBlockingReadForLogcat;
    private final boolean sendReportsInDevMode;

    private final ImmutableSet<String> excludeMatchingSharedPreferencesKeys;
    private final ImmutableSet<String> excludeMatchingSettingsKeys;
    private final Class buildConfigClass;
    private final String applicationLogFile;
    private final int applicationLogFileLines;
    private final Directory applicationLogFileDir;

    private final Method httpMethod;
    private final Type reportType;
    private final ImmutableMap<String, String> httpHeaders;
    private final Class<? extends KeyStoreFactory> keyStoreFactoryClass;
    private final ImmutableSet<Class<? extends ReportSenderFactory>> reportSenderFactoryClasses;
    @RawRes
    private final int resCertificate;
    private final String certificatePath;
    private final String certificateType;
    private final Class<? extends RetryPolicy> retryPolicyClass;

    /**
     * @param builder ConfigurationBuilder with which to initialise this {@link ACRAConfiguration}.
     */
    ACRAConfiguration(@NonNull ConfigurationBuilder builder) {
        additionalDropBoxTags = new ImmutableSet<String>(builder.additionalDropBoxTags());
        additionalSharedPreferences = new ImmutableSet<String>(builder.additionalSharedPreferences());
        connectionTimeout = builder.connectionTimeout();
        reportContent = new ImmutableSet<ReportField>(builder.reportContent());
        deleteUnapprovedReportsOnApplicationStart = builder.deleteUnapprovedReportsOnApplicationStart();
        deleteOldUnsentReportsOnApplicationStart = builder.deleteOldUnsentReportsOnApplicationStart();
        dropboxCollectionMinutes = builder.dropboxCollectionMinutes();
        alsoReportToAndroidFramework = builder.alsoReportToAndroidFramework();
        formUri = builder.formUri();
        formUriBasicAuthLogin = builder.formUriBasicAuthLogin();
        formUriBasicAuthPassword = builder.formUriBasicAuthPassword();
        includeDropBoxSystemTags = builder.includeDropBoxSystemTags();
        logcatArguments = new ImmutableList<String>(builder.logcatArguments());
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
        nonBlockingReadForLogcat = builder.nonBlockingReadForLogcat();
        sendReportsInDevMode = builder.sendReportsInDevMode();
        excludeMatchingSharedPreferencesKeys = new ImmutableSet<String>(builder.excludeMatchingSharedPreferencesKeys());
        excludeMatchingSettingsKeys = new ImmutableSet<String>(builder.excludeMatchingSettingsKeys());
        buildConfigClass = builder.buildConfigClass();
        applicationLogFile = builder.applicationLogFile();
        applicationLogFileLines = builder.applicationLogFileLines();
        applicationLogFileDir = builder.applicationLogFileDir();
        reportDialogClass = builder.reportDialogClass();
        reportPrimerClass = builder.reportPrimerClass();
        httpMethod = builder.httpMethod();
        httpHeaders = new ImmutableMap<String, String>(builder.httpHeaders());
        reportType = builder.reportType();
        reportSenderFactoryClasses = new ImmutableSet<Class<? extends ReportSenderFactory>>(builder.reportSenderFactoryClasses());
        keyStoreFactoryClass = builder.keyStoreFactoryClass();
        resCertificate = builder.resCertificate();
        certificatePath = builder.certificatePath();
        certificateType = builder.certificateType();
        retryPolicyClass = builder.retryPolicyClass();
    }

    /**
     * Retrieve HTTP headers defined by the application developer. These should
     * be added to requests sent by any third-party sender (over HTTP of
     * course).
     *
     * @return A map associating http header names to their values.
     */
    @NonNull
    public ImmutableMap<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * @return List of ReportField that ACRA will provide to the server.
     */
    @NonNull
    public ImmutableSet<ReportField> getReportFields() {
        return reportContent;
    }

    @NonNull
    public ImmutableSet<String> additionalDropBoxTags() {
        return additionalDropBoxTags;
    }

    @NonNull
    public ImmutableSet<String> additionalSharedPreferences() {
        return additionalSharedPreferences;
    }

    public int connectionTimeout() {
        return connectionTimeout;
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
    public ImmutableList<String> logcatArguments() {
        return logcatArguments;
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

    public boolean nonBlockingReadForLogcat() {
        return nonBlockingReadForLogcat;
    }

    public boolean sendReportsInDevMode() {
        return sendReportsInDevMode;
    }

    @NonNull
    public ImmutableSet<String> excludeMatchingSharedPreferencesKeys() {
        return excludeMatchingSharedPreferencesKeys;
    }

    @NonNull
    public ImmutableSet<String> excludeMatchingSettingsKeys() {
        return excludeMatchingSettingsKeys;
    }

    /**
     * Will return null if no value has been configured.
     * It is up to clients to construct the recommended default value of context.getClass().getPackage().getName() + BuildConfig.class
     *
     * @return Class generated at compile time containing the build config for this application.
     */
    @NonNull
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
    public Directory applicationLogFileDir() {
        return applicationLogFileDir;
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
    public ImmutableSet<Class<? extends ReportSenderFactory>> reportSenderFactoryClasses() {
        return reportSenderFactoryClasses;
    }

    @NonNull
    public Class<? extends KeyStoreFactory> keyStoreFactoryClass() {
        return keyStoreFactoryClass;
    }

    @RawRes
    public int resCertificate() {
        return resCertificate;
    }

    @NonNull
    public String certificatePath() {
        return certificatePath;
    }

    @NonNull
    public String certificateType() {
        return certificateType;
    }

    @NonNull
    public Class<? extends RetryPolicy> retryPolicyClass() {
        return retryPolicyClass;
    }
}
