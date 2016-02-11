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

import android.app.Application;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.builder.NoOpReportPrimer;
import org.acra.builder.ReportPrimer;
import org.acra.dialog.BaseCrashReportDialog;
import org.acra.dialog.CrashReportDialog;
import org.acra.sender.DefaultReportSenderFactory;
import org.acra.sender.HttpSender;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.acra.sender.ReportSenderFactory;

import java.lang.annotation.Annotation;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import static org.acra.ACRAConstants.*;

/**
 * Builder responsible for programmatic construction of an {@link ACRAConfiguration}.
 *
 * {@link ACRAConfiguration} should be considered immutable and in the future will be.
 *
 * @since 4.8.1
 */
@SuppressWarnings("unused")
public final class ConfigurationBuilder {

    final Class<? extends Annotation> annotationType;

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
    private Class buildConfigClass;
    private String applicationLogFile;
    private Integer applicationLogFileLines;

    private Method httpMethod;
    private Type reportType;
    private final Map<String, String> httpHeaders = new HashMap<>();
    private KeyStore keyStore;
    private Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses;

    /**
     * Constructs a ConfigurationBuilder that is prepopulated with any
     * '@ReportCrashes' annotation declared on the Application class.
     *
     * @param app   Current Application, from which any annotated config will be gleaned.
     */
    public ConfigurationBuilder(Application app) {

        // Populate with annotated config
        final ReportsCrashes annotationConfig = app.getClass().getAnnotation(ReportsCrashes.class);

        if (annotationConfig != null) {
            annotationType = annotationConfig.annotationType();

            additionalDropBoxTags = annotationConfig.additionalDropBoxTags();
            additionalSharedPreferences = annotationConfig.additionalSharedPreferences();
            connectionTimeout = annotationConfig.connectionTimeout();
            customReportContent = annotationConfig.customReportContent();
            deleteUnapprovedReportsOnApplicationStart = annotationConfig.deleteUnapprovedReportsOnApplicationStart();
            deleteOldUnsentReportsOnApplicationStart = annotationConfig.deleteOldUnsentReportsOnApplicationStart();
            dropboxCollectionMinutes = annotationConfig.dropboxCollectionMinutes();
            forceCloseDialogAfterToast = annotationConfig.forceCloseDialogAfterToast();
            formUri = annotationConfig.formUri();
            formUriBasicAuthLogin = annotationConfig.formUriBasicAuthLogin();
            formUriBasicAuthPassword = annotationConfig.formUriBasicAuthPassword();
            includeDropBoxSystemTags = annotationConfig.includeDropBoxSystemTags();
            logcatArguments = annotationConfig.logcatArguments();
            mailTo = annotationConfig.mailTo();
            reportingInteractionMode = annotationConfig.mode();
            resDialogIcon = annotationConfig.resDialogIcon();
            resDialogPositiveButtonText = annotationConfig.resDialogPositiveButtonText();
            resDialogNegativeButtonText = annotationConfig.resDialogNegativeButtonText();
            resDialogCommentPrompt = annotationConfig.resDialogCommentPrompt();
            resDialogEmailPrompt = annotationConfig.resDialogEmailPrompt();
            resDialogOkToast = annotationConfig.resDialogOkToast();
            resDialogText = annotationConfig.resDialogText();
            resDialogTitle = annotationConfig.resDialogTitle();
            resNotifIcon = annotationConfig.resNotifIcon();
            resNotifText = annotationConfig.resNotifText();
            resNotifTickerText = annotationConfig.resNotifTickerText();
            resNotifTitle = annotationConfig.resNotifTitle();
            resToastText = annotationConfig.resToastText();
            sharedPreferencesMode = annotationConfig.sharedPreferencesMode();
            sharedPreferencesName = annotationConfig.sharedPreferencesName();
            socketTimeout = annotationConfig.socketTimeout();
            logcatFilterByPid = annotationConfig.logcatFilterByPid();
            sendReportsInDevMode = annotationConfig.sendReportsInDevMode();
            sendReportsAtShutdown = annotationConfig.sendReportsAtShutdown();
            excludeMatchingSharedPreferencesKeys = annotationConfig.excludeMatchingSharedPreferencesKeys();
            excludeMatchingSettingsKeys = annotationConfig.excludeMatchingSettingsKeys();
            buildConfigClass = annotationConfig.buildConfigClass();
            applicationLogFile = annotationConfig.applicationLogFile();
            applicationLogFileLines = annotationConfig.applicationLogFileLines();
            reportDialogClass = annotationConfig.reportDialogClass();
            reportPrimerClass = annotationConfig.reportPrimerClass();
            httpMethod = annotationConfig.httpMethod();
            reportType = annotationConfig.reportType();
            reportSenderFactoryClasses = annotationConfig.reportSenderFactoryClasses();
        } else {
            annotationType = null;
        }
    }

    /**
     * @return new ACRAConfiguration containing all the properties configured on this builder.
     */
    public ACRAConfiguration build() {
        return new ACRAConfiguration(this);
    }

    /**
     * Set custom HTTP headers to be sent by the provided {@link HttpSender}.
     * This should be used also by third party senders.
     * 
     * @param headers
     *            A map associating HTTP header names to their values.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setHttpHeaders(Map<String, String> headers) {
        this.httpHeaders.clear();
        this.httpHeaders.putAll(headers);
        return this;
    }

    /**
     * @param additionalDropboxTags
     *            the additionalDropboxTags to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setAdditionalDropboxTags(String[] additionalDropboxTags) {
        this.additionalDropBoxTags = additionalDropboxTags;
        return this;
    }

    /**
     * @param additionalSharedPreferences
     *            the additionalSharedPreferences to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setAdditionalSharedPreferences(String[] additionalSharedPreferences) {
        this.additionalSharedPreferences = additionalSharedPreferences;
        return this;
    }

    /**
     * @param connectionTimeout
     *            the connectionTimeout to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * @param customReportContent
     *            the customReportContent to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setCustomReportContent(ReportField[] customReportContent) {
        this.customReportContent = customReportContent;
        return this;
    }

    /**
     * @param deleteUnapprovedReportsOnApplicationStart the deleteUnapprovedReportsOnApplicationStart to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setDeleteUnapprovedReportsOnApplicationStart(Boolean deleteUnapprovedReportsOnApplicationStart) {
        this.deleteUnapprovedReportsOnApplicationStart = deleteUnapprovedReportsOnApplicationStart;
        return this;
    }

    /**
     * @param deleteOldUnsentReportsOnApplicationStart    When to delete old (unsent) reports on startup.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setDeleteOldUnsentReportsOnApplicationStart(Boolean deleteOldUnsentReportsOnApplicationStart) {
        this.deleteOldUnsentReportsOnApplicationStart = deleteOldUnsentReportsOnApplicationStart;
        return this;
    }

    /**
     * @param dropboxCollectionMinutes
     *            the dropboxCollectionMinutes to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setDropboxCollectionMinutes(Integer dropboxCollectionMinutes) {
        this.dropboxCollectionMinutes = dropboxCollectionMinutes;
        return this;
    }

    /**
     * @param forceCloseDialogAfterToast
     *            the forceCloseDialogAfterToast to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setForceCloseDialogAfterToast(Boolean forceCloseDialogAfterToast) {
        this.forceCloseDialogAfterToast = forceCloseDialogAfterToast;
        return this;
    }

    /**
     * Modify the formUri of your backend server receiving reports.
     * 
     * @param formUri   formUri to set.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setFormUri(String formUri) {
        this.formUri = formUri;
        return this;
    }

    /**
     * @param formUriBasicAuthLogin
     *            the formUriBasicAuthLogin to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setFormUriBasicAuthLogin(String formUriBasicAuthLogin) {
        this.formUriBasicAuthLogin = formUriBasicAuthLogin;
        return this;
    }

    /**
     * @param formUriBasicAuthPassword
     *            the formUriBasicAuthPassword to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setFormUriBasicAuthPassword(String formUriBasicAuthPassword) {
        this.formUriBasicAuthPassword = formUriBasicAuthPassword;
        return this;
    }

    /**
     * @param includeDropboxSystemTags
     *            the includeDropboxSystemTags to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setIncludeDropboxSystemTags(Boolean includeDropboxSystemTags) {
        this.includeDropBoxSystemTags = includeDropboxSystemTags;
        return this;
    }

    /**
     * @param logcatArguments
     *            the logcatArguments to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setLogcatArguments(String[] logcatArguments) {
        this.logcatArguments = logcatArguments;
        return this;
    }

    /**
     * Modify the mailTo of the mail account receiving reports.
     * 
     * @param mailTo    mailTo to set.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setMailTo(String mailTo) {
        this.mailTo = mailTo;
        return this;
    }

    /**
     * Change the current {@link ReportingInteractionMode}.
     * 
     * @param mode  ReportingInteractionMode to set.
     * @return The updated ACRA configuration
     * @throws ACRAConfigurationException if a configuration item is missing for this reportingInteractionMode.
     *
     * @deprecated since 4.8.2 use {@link #setReportingInteractionMode(ReportingInteractionMode)} instead.
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setMode(ReportingInteractionMode mode) throws ACRAConfigurationException {
        this.reportingInteractionMode = mode;
        return this;
    }

    /**
     * Change the current {@link ReportingInteractionMode}.
     *
     * @param mode  ReportingInteractionMode to set.
     * @return The updated ACRA configuration
     * @throws ACRAConfigurationException if a configuration item is missing for this reportingInteractionMode.
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setReportingInteractionMode(ReportingInteractionMode mode) throws ACRAConfigurationException {
        this.reportingInteractionMode = mode;
        return this;
    }

    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResDialogPositiveButtonText(int resId) {
        resDialogPositiveButtonText = resId;
        return this;
    }

    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResDialogNegativeButtonText(int resId) {
        resDialogNegativeButtonText = resId;
        return this;
    }

    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setReportDialogClass(Class<? extends BaseCrashReportDialog> reportDialogClass) {
        this.reportDialogClass = reportDialogClass;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogCommentPrompt()} comes from an Android
     * Library Project.
     * 
     * @param resId The resource id, see {@link ReportsCrashes#resDialogCommentPrompt()}
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResDialogCommentPrompt(int resId) {
        resDialogCommentPrompt = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogEmailPrompt()} comes from an Android Library Project.
     * 
     * @param resId
     *            The resource id, see
     *            {@link ReportsCrashes#resDialogEmailPrompt()}
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResDialogEmailPrompt(int resId) {
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
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResDialogIcon(int resId) {
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
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResDialogOkToast(int resId) {
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
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResDialogText(int resId) {
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
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResDialogTitle(int resId) {
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
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResNotifIcon(int resId) {
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
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResNotifText(int resId) {
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
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResNotifTickerText(int resId) {
        resNotifTickerText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTitle()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifTitle()}
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResNotifTitle(int resId) {
        resNotifTitle = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resToastText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resToastText()}
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setResToastText(int resId) {
        resToastText = resId;
        return this;
    }

    /**
     * @param sharedPreferenceMode
     *            the sharedPreferenceMode to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setSharedPreferenceMode(Integer sharedPreferenceMode) {
        this.sharedPreferencesMode = sharedPreferenceMode;
        return this;
    }

    /**
     * @param sharedPreferenceName
     *            the sharedPreferenceName to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setSharedPreferenceName(String sharedPreferenceName) {
        this.sharedPreferencesName = sharedPreferenceName;
        return this;
    }

    /**
     * @param socketTimeout
     *            the socketTimeout to set
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * 
     * @param filterByPid
     *            true if you want to collect only logcat lines related to your
     *            application process.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setLogcatFilterByPid(Boolean filterByPid) {
        logcatFilterByPid = filterByPid;
        return this;
    }

    /**
     * 
     * @param sendReportsInDevMode
     *            false if you want to disable sending reports in development
     *            mode. Reports will be sent only on signed applications.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setSendReportsInDevMode(Boolean sendReportsInDevMode) {
        this.sendReportsInDevMode = sendReportsInDevMode;
        return this;
    }

    /**
     * 
     * @param sendReportsAtShutdown
     *            false if you want to disable sending reports at the time the
     *            exception is caught. Reports will be sent when the application
     *            is restarted.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setSendReportsAtShutdown(Boolean sendReportsAtShutdown) {
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
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setExcludeMatchingSharedPreferencesKeys(String[] excludeMatchingSharedPreferencesKeys) {
        this.excludeMatchingSharedPreferencesKeys = excludeMatchingSharedPreferencesKeys;
        return this;
    }

    /**
     * 
     * @param excludeMatchingSettingsKeys
     *            an array of Strings containing regexp defining
     *            Settings.System, Settings.Secure and Settings.Global keys that
     *            should be excluded from the data collection.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setExcludeMatchingSettingsKeys(String[] excludeMatchingSettingsKeys) {
        this.excludeMatchingSettingsKeys = excludeMatchingSettingsKeys;
        return this;
    }

    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setBuildConfigClass(Class buildConfigClass) {
        this.buildConfigClass = buildConfigClass;
        return this;
    }
    /**
     * 
     * @param applicationLogFile
     *            The path and file name of your application log file, to be
     *            used with {@link ReportField#APPLICATION_LOG}.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setApplicationLogFile(String applicationLogFile) {
        this.applicationLogFile = applicationLogFile;
        return this;
    }

    /**
     * 
     * @param applicationLogFileLines
     *            The number of lines of your application log to be collected,
     *            to be used with {@link ReportField#APPLICATION_LOG} and
     *            {@link ReportsCrashes#applicationLogFile()}.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setApplicationLogFileLines(int applicationLogFileLines) {
        this.applicationLogFileLines = applicationLogFileLines;
        return this;
    }

    /**
     * 
     * @param httpMethod
     *            The method to be used to send data to the server.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setHttpMethod(Method httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * 
     * @param type
     *            The type of content encoding to be used to send data to the
     *            server.
     * @return The updated ACRA configuration
     */
    @SuppressWarnings( "unused" )
    public ConfigurationBuilder setReportType(Type type) {
        reportType = type;
        return this;
    }

    /**
     * 
     * @param keyStore
     *            Set this to the keystore that contains the trusted certificates
     */
    @SuppressWarnings("unused")
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }


    @SuppressWarnings("unused")
    public void setReportSenderFactoryClasses(Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses) {
        this.reportSenderFactoryClasses = reportSenderFactoryClasses;
    }

    @SuppressWarnings("unused")
    public void setReportPrimerClass(Class<? extends ReportPrimer> reportPrimerClass) {
        this.reportPrimerClass = reportPrimerClass;
    }


    // Getters - used to provide values and !DEFAULTS! to ACRConfiguration during construction

    @SuppressWarnings("unused")
    String[] additionalDropBoxTags() {
        if (additionalDropBoxTags != null) {
            return additionalDropBoxTags;
        }
        return new String[0];
    }

    @SuppressWarnings("unused")
    String[] additionalSharedPreferences() {
        if (additionalSharedPreferences != null) {
            return additionalSharedPreferences;
        }
        return new String[0];
    }

    /**
     * @deprecated since 4.8.1 no replacement.
     */
    @SuppressWarnings("unused")
    Class<? extends Annotation> annotationType() {
        return annotationType; // Why would this ever be needed?
    }

    @SuppressWarnings("unused")
    int connectionTimeout() {
        if (connectionTimeout != null) {
            return connectionTimeout;
        }
        return DEFAULT_CONNECTION_TIMEOUT;
    }

    @SuppressWarnings("unused")
    ReportField[] customReportContent() {
        if (customReportContent != null) {
            return customReportContent;
        }
        return new ReportField[0];
    }

    @SuppressWarnings("unused")
    boolean deleteUnapprovedReportsOnApplicationStart() {
        if (deleteUnapprovedReportsOnApplicationStart != null) {
            return deleteUnapprovedReportsOnApplicationStart;
        }
        return DEFAULT_DELETE_UNAPPROVED_REPORTS_ON_APPLICATION_START;
    }

    @SuppressWarnings("unused")
    boolean deleteOldUnsentReportsOnApplicationStart() {
        if (deleteOldUnsentReportsOnApplicationStart != null) {
            return deleteOldUnsentReportsOnApplicationStart;
        }
        return DEFAULT_DELETE_OLD_UNSENT_REPORTS_ON_APPLICATION_START;
    }

    @SuppressWarnings("unused")
    int dropboxCollectionMinutes() {
        if (dropboxCollectionMinutes != null) {
            return dropboxCollectionMinutes;
        }
        return DEFAULT_DROPBOX_COLLECTION_MINUTES;
    }

    @SuppressWarnings("unused")
    boolean forceCloseDialogAfterToast() {
        if (forceCloseDialogAfterToast != null) {
            return forceCloseDialogAfterToast;
        }
        return DEFAULT_FORCE_CLOSE_DIALOG_AFTER_TOAST;
    }

    @SuppressWarnings("unused")
    String formUri() {
        if (formUri != null) {
            return formUri;
        }
        return DEFAULT_STRING_VALUE;
    }

    @SuppressWarnings("unused")
    String formUriBasicAuthLogin() {
        if (formUriBasicAuthLogin != null) {
            return formUriBasicAuthLogin;
        }
        return NULL_VALUE;
    }

    @SuppressWarnings("unused")
    String formUriBasicAuthPassword() {
        if (formUriBasicAuthPassword != null) {
            return formUriBasicAuthPassword;
        }
        return NULL_VALUE;
    }

    @SuppressWarnings("unused")
    boolean includeDropBoxSystemTags() {
        if (includeDropBoxSystemTags != null) {
            return includeDropBoxSystemTags;
        }
        return DEFAULT_INCLUDE_DROPBOX_SYSTEM_TAGS;
    }

    @SuppressWarnings("unused")
    String[] logcatArguments() {
        if (logcatArguments != null) {
            return logcatArguments;
        }
        return new String[] { "-t", Integer.toString(DEFAULT_LOGCAT_LINES), "-v", "time" };
    }

    @SuppressWarnings("unused")
    String mailTo() {
        if (mailTo != null) {
            return mailTo;
        }
        return DEFAULT_STRING_VALUE;
    }

    @SuppressWarnings("unused")
    ReportingInteractionMode reportingInteractionMode() {
        if (reportingInteractionMode != null) {
            return reportingInteractionMode;
        }
        return ReportingInteractionMode.SILENT;
    }

    @SuppressWarnings("unused")
    public int resDialogPositiveButtonText() {
        if (resDialogPositiveButtonText != null) {
            return resDialogPositiveButtonText;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resDialogNegativeButtonText() {
        if (resDialogNegativeButtonText != null) {
            return resDialogNegativeButtonText;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resDialogCommentPrompt() {
        if (resDialogCommentPrompt != null) {
            return resDialogCommentPrompt;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resDialogEmailPrompt() {
        if (resDialogEmailPrompt != null) {
            return resDialogEmailPrompt;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resDialogIcon() {
        if (resDialogIcon != null) {
            return resDialogIcon;
        }
        return DEFAULT_DIALOG_ICON;
    }

    @SuppressWarnings("unused")
    int resDialogOkToast() {
        if (resDialogOkToast != null) {
            return resDialogOkToast;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resDialogText() {
        if (resDialogText != null) {
            return resDialogText;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resDialogTitle() {
        if (resDialogTitle != null) {
            return resDialogTitle;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resNotifIcon() {
        if (resNotifIcon != null) {
            return resNotifIcon;
        }
        return DEFAULT_NOTIFICATION_ICON;
    }

    @SuppressWarnings("unused")
    int resNotifText() {
        if (resNotifText != null) {
            return resNotifText;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resNotifTickerText() {
        if (resNotifTickerText != null) {
            return resNotifTickerText;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resNotifTitle() {
        if (resNotifTitle != null) {
            return resNotifTitle;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int resToastText() {
        if (resToastText != null) {
            return resToastText;
        }
        return DEFAULT_RES_VALUE;
    }

    @SuppressWarnings("unused")
    int sharedPreferencesMode() {
        if (sharedPreferencesMode != null) {
            return sharedPreferencesMode;
        }
        return DEFAULT_SHARED_PREFERENCES_MODE;
    }

    @SuppressWarnings("unused")
    String sharedPreferencesName() {
        if (sharedPreferencesName != null) {
            return sharedPreferencesName;
        }

        return DEFAULT_STRING_VALUE;
    }

    @SuppressWarnings("unused")
    int socketTimeout() {
        if (socketTimeout != null) {
            return socketTimeout;
        }
        return DEFAULT_SOCKET_TIMEOUT;
    }

    @SuppressWarnings("unused")
    boolean logcatFilterByPid() {
        if (logcatFilterByPid != null) {
            return logcatFilterByPid;
        }
        return DEFAULT_LOGCAT_FILTER_BY_PID;
    }

    @SuppressWarnings("unused")
    boolean sendReportsInDevMode() {
        if (sendReportsInDevMode != null) {
            return sendReportsInDevMode;
        }
        return DEFAULT_SEND_REPORTS_IN_DEV_MODE;
    }

    @SuppressWarnings("unused")
    boolean sendReportsAtShutdown() {
        if (sendReportsAtShutdown != null) {
            return sendReportsAtShutdown;
        }
        return DEFAULT_SEND_REPORTS_AT_SHUTDOWN;
    }

    @SuppressWarnings("unused")
    String[] excludeMatchingSharedPreferencesKeys() {
        if (excludeMatchingSharedPreferencesKeys != null) {
            return excludeMatchingSharedPreferencesKeys;
        }
        return new String[0];
    }

    @SuppressWarnings("unused")
    String[] excludeMatchingSettingsKeys() {
        if (excludeMatchingSettingsKeys != null) {
            return excludeMatchingSettingsKeys;
        }
        return new String[0];
    }

    /**
     * Will return null if no value has been configured.
     * It is up to clients to construct the recommended default value oof context.getClass().getPackage().getName() + BuildConfig.class
     */
    @SuppressWarnings("unused")
    Class buildConfigClass() {
        if (buildConfigClass != null) {
            return buildConfigClass;
        }
        return null;
    }

    @SuppressWarnings("unused")
    String applicationLogFile() {
        if (applicationLogFile != null) {
            return applicationLogFile;
        }
        return DEFAULT_APPLICATION_LOGFILE;
    }

    @SuppressWarnings("unused")
    int applicationLogFileLines() {
        if (applicationLogFileLines != null) {
            return applicationLogFileLines;
        }
        return DEFAULT_APPLICATION_LOGFILE_LINES;
    }

    @SuppressWarnings("unused")
    Class<? extends BaseCrashReportDialog> reportDialogClass() {
        if (reportDialogClass != null) {
            return reportDialogClass;
        }
        return CrashReportDialog.class;
    }

    @SuppressWarnings("unused")
    Class<? extends ReportPrimer> reportPrimerClass() {
        if (reportPrimerClass != null) {
            return reportPrimerClass;
        }
        return NoOpReportPrimer.class;
    }

    @SuppressWarnings("unused")
    Method httpMethod() {
        if (httpMethod != null) {
            return httpMethod;
        }
        return Method.POST;
    }

    @SuppressWarnings("unused")
    Type reportType() {
        if (reportType != null) {
            return reportType;
        }
        return Type.FORM;
    }

    @SuppressWarnings("unused")
    Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses() {
        if (reportSenderFactoryClasses != null) {
            return reportSenderFactoryClasses;
        }
        //noinspection unchecked
        return new Class[] {DefaultReportSenderFactory.class};
    }

    @SuppressWarnings("unused")
    KeyStore keyStore() {
        return keyStore;
    }

    Map<String, String> httpHeaders() {
        return httpHeaders;
    }
}
