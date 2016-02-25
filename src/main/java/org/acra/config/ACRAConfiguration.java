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
import android.support.annotation.StringRes;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.builder.ReportPrimer;
import org.acra.dialog.BaseCrashReportDialog;
import org.acra.dialog.CrashReportDialog;
import org.acra.sender.HttpSender;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.acra.sender.ReportSenderFactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

/**
 * Represents the configuration that ACRA will use when handling crash reports.
 * <p/>
 * ACRAConfiguration should be considered immutable. To that end, as of 4.8.1
 * all of the set methods have been deprecated.
 * <p/>
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

    @StringRes
    private Integer resDialogPositiveButtonText;
    @StringRes
    private Integer resDialogNegativeButtonText;
    @StringRes
    private Integer resDialogCommentPrompt;
    @StringRes
    private Integer resDialogEmailPrompt;
    @DrawableRes
    private Integer resDialogIcon;
    @StringRes
    private Integer resDialogOkToast;
    @StringRes
    private Integer resDialogText;
    @StringRes
    private Integer resDialogTitle;
    @DrawableRes
    private Integer resNotifIcon;
    @StringRes
    private Integer resNotifText;
    @StringRes
    private Integer resNotifTickerText;
    @StringRes
    private Integer resNotifTitle;
    @StringRes
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
    private KeyStore keyStore;
    private Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses;

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
    }

    /**
     * Empty constructor which sets no defaults.
     */
    @SuppressWarnings("unused")
    public ACRAConfiguration() {
        //TODO this will always throw a NPE! Modify/remove!
        this(null);
    }


    /**
     * Set custom HTTP headers to be sent by the provided {@link HttpSender}.
     * This should be used also by third party senders.
     *
     * @param headers A map associating HTTP header names to their values.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
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
    @NonNull
    public Map<String, String> getHttpHeaders() {
        return Collections.unmodifiableMap(httpHeaders);
    }

    /**
     * @return List of ReportField that ACRA will provide to the server.
     */
    @NonNull
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
     * @param additionalDropboxTags the additionalDropboxTags to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public ACRAConfiguration setConnectionTimeout(@NonNull Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * @param customReportContent the customReportContent to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setCustomReportContent(@NonNull ReportField[] customReportContent) {
        this.customReportContent = copyArray(customReportContent);
        return this;
    }

    /**
     * @param deleteUnapprovedReportsOnApplicationStart the deleteUnapprovedReportsOnApplicationStart to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setDeleteUnapprovedReportsOnApplicationStart(@NonNull Boolean deleteUnapprovedReportsOnApplicationStart) {
        this.deleteUnapprovedReportsOnApplicationStart = deleteUnapprovedReportsOnApplicationStart;
        return this;
    }

    /**
     * @param deleteOldUnsentReportsOnApplicationStart When to delete old (unsent) reports on startup.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setDeleteOldUnsentReportsOnApplicationStart(@NonNull Boolean deleteOldUnsentReportsOnApplicationStart) {
        this.deleteOldUnsentReportsOnApplicationStart = deleteOldUnsentReportsOnApplicationStart;
        return this;
    }

    /**
     * @param dropboxCollectionMinutes the dropboxCollectionMinutes to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setDropboxCollectionMinutes(@NonNull Integer dropboxCollectionMinutes) {
        this.dropboxCollectionMinutes = dropboxCollectionMinutes;
        return this;
    }

    /**
     * @param forceCloseDialogAfterToast the forceCloseDialogAfterToast to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setForceCloseDialogAfterToast(@NonNull Boolean forceCloseDialogAfterToast) {
        this.forceCloseDialogAfterToast = forceCloseDialogAfterToast;
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public ACRAConfiguration setIncludeDropboxSystemTags(@NonNull Boolean includeDropboxSystemTags) {
        this.includeDropBoxSystemTags = includeDropboxSystemTags;
        return this;
    }

    /**
     * @param logcatArguments the logcatArguments to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public ACRAConfiguration setMode(@NonNull ReportingInteractionMode mode) throws ACRAConfigurationException {
        this.reportingInteractionMode = mode;
        checkCrashResources();
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setResDialogPositiveButtonText(@StringRes int resId) {
        resDialogPositiveButtonText = resId;
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setResDialogNegativeButtonText(@StringRes int resId) {
        resDialogNegativeButtonText = resId;
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public ACRAConfiguration setSharedPreferenceMode(@NonNull Integer sharedPreferenceMode) {
        this.sharedPreferencesMode = sharedPreferenceMode;
        return this;
    }

    /**
     * @param sharedPreferenceName the sharedPreferenceName to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setSharedPreferenceName(@NonNull String sharedPreferenceName) {
        this.sharedPreferencesName = sharedPreferenceName;
        return this;
    }

    /**
     * @param socketTimeout the socketTimeout to set
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setSocketTimeout(@NonNull Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * @param filterByPid true if you want to collect only logcat lines related to your
     *                    application process.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setLogcatFilterByPid(@NonNull Boolean filterByPid) {
        logcatFilterByPid = filterByPid;
        return this;
    }

    /**
     * @param sendReportsInDevMode false if you want to disable sending reports in development
     *                             mode. Reports will be sent only on signed applications.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setSendReportsInDevMode(@NonNull Boolean sendReportsInDevMode) {
        this.sendReportsInDevMode = sendReportsInDevMode;
        return this;
    }

    /**
     * @param sendReportsAtShutdown false if you want to disable sending reports at the time the
     *                              exception is caught. Reports will be sent when the application
     *                              is restarted.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setSendReportsAtShutdown(@NonNull Boolean sendReportsAtShutdown) {
        this.sendReportsAtShutdown = sendReportsAtShutdown;
        return this;
    }

    /**
     * @param excludeMatchingSharedPreferencesKeys an array of Strings containing regexp defining
     *                                             SharedPreferences keys that should be excluded from the data
     *                                             collection.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setExcludeMatchingSharedPreferencesKeys(@NonNull String[] excludeMatchingSharedPreferencesKeys) {
        this.excludeMatchingSharedPreferencesKeys = copyArray(excludeMatchingSharedPreferencesKeys);
        return this;
    }

    /**
     * @param excludeMatchingSettingsKeys an array of Strings containing regexp defining
     *                                    Settings.System, Settings.Secure and Settings.Global keys that
     *                                    should be excluded from the data collection.
     * @return The updated ACRA configuration
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
    public ACRAConfiguration setExcludeMatchingSettingsKeys(@NonNull String[] excludeMatchingSettingsKeys) {
        this.excludeMatchingSettingsKeys = copyArray(excludeMatchingSettingsKeys);
        return this;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @NonNull
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public ACRAConfiguration setReportType(@NonNull Type type) {
        reportType = type;
        return this;
    }

    /**
     * @param keyStore Set this to the keystore that contains the trusted certificates
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @SuppressWarnings("unused")
    public void setKeyStore(@Nullable KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * @deprecated since 4.8.1 - configure using {@link ConfigurationBuilder} instead. ACRAConfiguration will become immutable in the near future.
     */
    @SuppressWarnings("unused")
    public void setReportSenderFactoryClasses(@NonNull Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses) {
        this.reportSenderFactoryClasses = copyArray(reportSenderFactoryClasses);
    }

    @SuppressWarnings("unused")
    @NonNull
    public String[] additionalDropBoxTags() {
        return copyArray(additionalDropBoxTags);
    }

    @SuppressWarnings("unused")
    @NonNull
    public String[] additionalSharedPreferences() {
        return copyArray(additionalSharedPreferences);
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

    @NonNull
    @SuppressWarnings("unused")
    public ReportField[] customReportContent() {
        return copyArray(customReportContent);
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
    @Nullable
    public String formUri() {
        return formUri;
    }

    @SuppressWarnings("unused")
    @Nullable
    public String formUriBasicAuthLogin() {
        return formUriBasicAuthLogin;
    }

    @SuppressWarnings("unused")
    @Nullable
    public String formUriBasicAuthPassword() {
        return formUriBasicAuthPassword;
    }

    @SuppressWarnings("unused")
    public boolean includeDropBoxSystemTags() {
        return includeDropBoxSystemTags;
    }

    @SuppressWarnings("unused")
    @NonNull
    public String[] logcatArguments() {
        return copyArray(logcatArguments);
    }

    @SuppressWarnings("unused")
    @Nullable
    public String mailTo() {
        return mailTo;
    }

    @SuppressWarnings("unused")
    @NonNull
    public ReportingInteractionMode mode() {
        return reportingInteractionMode;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resDialogPositiveButtonText() {
        return resDialogPositiveButtonText;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resDialogNegativeButtonText() {
        return resDialogNegativeButtonText;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resDialogCommentPrompt() {
        return resDialogCommentPrompt;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resDialogEmailPrompt() {
        return resDialogEmailPrompt;
    }

    @SuppressWarnings("unused")
    @DrawableRes
    public int resDialogIcon() {
        return resDialogIcon;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resDialogOkToast() {
        return resDialogOkToast;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resDialogText() {
        return resDialogText;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resDialogTitle() {
        return resDialogTitle;
    }

    @SuppressWarnings("unused")
    @DrawableRes
    public int resNotifIcon() {
        return resNotifIcon;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resNotifText() {
        return resNotifText;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resNotifTickerText() {
        return resNotifTickerText;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resNotifTitle() {
        return resNotifTitle;
    }

    @SuppressWarnings("unused")
    @StringRes
    public int resToastText() {
        return resToastText;
    }

    @SuppressWarnings("unused")
    public int sharedPreferencesMode() {
        return sharedPreferencesMode;
    }

    @SuppressWarnings("unused")
    @NonNull
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

    @NonNull
    @SuppressWarnings("unused")
    public String[] excludeMatchingSharedPreferencesKeys() {
        return copyArray(excludeMatchingSharedPreferencesKeys);
    }

    @NonNull
    @SuppressWarnings("unused")
    public String[] excludeMatchingSettingsKeys() {
        return copyArray(excludeMatchingSettingsKeys);
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
    @NonNull
    public String applicationLogFile() {
        return applicationLogFile;
    }

    @SuppressWarnings("unused")
    public int applicationLogFileLines() {
        return applicationLogFileLines;
    }

    @SuppressWarnings("unused")
    @NonNull
    public Class<? extends BaseCrashReportDialog> reportDialogClass() {
        return reportDialogClass;
    }

    @SuppressWarnings("unused")
    @NonNull
    public Class<? extends ReportPrimer> reportPrimerClass() {
        return reportPrimerClass;
    }

    @SuppressWarnings("unused")
    @NonNull
    public Method httpMethod() {
        return httpMethod;
    }

    @SuppressWarnings("unused")
    @NonNull
    public Type reportType() {
        return reportType;
    }

    @NonNull
    @SuppressWarnings("unused")
    public Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses() {
        return copyArray(reportSenderFactoryClasses);
    }

    @SuppressWarnings("unused")
    @Nullable
    public KeyStore keyStore() {
        return keyStore;
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
