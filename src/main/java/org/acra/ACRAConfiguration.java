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
package org.acra;

import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import java.lang.annotation.Annotation;

import static org.acra.ACRAConstants.DEFAULT_APPLICATION_LOGFILE;
import static org.acra.ACRAConstants.DEFAULT_APPLICATION_LOGFILE_LINES;
import static org.acra.ACRAConstants.DEFAULT_CONNECTION_TIMEOUT;
import static org.acra.ACRAConstants.DEFAULT_DELETE_OLD_UNSENT_REPORTS_ON_APPLICATION_START;
import static org.acra.ACRAConstants.DEFAULT_DELETE_UNAPPROVED_REPORTS_ON_APPLICATION_START;
import static org.acra.ACRAConstants.DEFAULT_DIALOG_ICON;
import static org.acra.ACRAConstants.DEFAULT_DISABLE_SSL_CERT_VALIDATION;
import static org.acra.ACRAConstants.DEFAULT_DROPBOX_COLLECTION_MINUTES;
import static org.acra.ACRAConstants.DEFAULT_FORCE_CLOSE_DIALOG_AFTER_TOAST;
import static org.acra.ACRAConstants.DEFAULT_GOOGLE_FORM_URL_FORMAT;
import static org.acra.ACRAConstants.DEFAULT_INCLUDE_DROPBOX_SYSTEM_TAGS;
import static org.acra.ACRAConstants.DEFAULT_LOGCAT_FILTER_BY_PID;
import static org.acra.ACRAConstants.DEFAULT_LOGCAT_LINES;
import static org.acra.ACRAConstants.DEFAULT_MAX_NUMBER_OF_REQUEST_RETRIES;
import static org.acra.ACRAConstants.DEFAULT_NOTIFICATION_ICON;
import static org.acra.ACRAConstants.DEFAULT_RES_VALUE;
import static org.acra.ACRAConstants.DEFAULT_SEND_REPORTS_IN_DEV_MODE;
import static org.acra.ACRAConstants.DEFAULT_SHARED_PREFERENCES_MODE;
import static org.acra.ACRAConstants.DEFAULT_SOCKET_TIMEOUT;
import static org.acra.ACRAConstants.DEFAULT_STRING_VALUE;
import static org.acra.ACRAConstants.NULL_VALUE;

/**
 * This class is to be used if you need to apply dynamic settings. This is
 * needed for example when using ACRA in an Android Library Project since ADT
 * v14 where resource ids are not final anymore and can't be passed as
 * annotation parameters values.
 * 
 */
public class ACRAConfiguration implements ReportsCrashes {

    private String[] mAdditionalDropboxTags = null;

    private String[] mAdditionalSharedPreferences = null;
    private Integer mConnectionTimeout = null;
    private ReportField[] mCustomReportContent = null;
    private Boolean mDeleteUnapprovedReportsOnApplicationStart = null;
    private Boolean mDeleteOldUnsentReportsOnApplicationStart = null;
    private Integer mDropboxCollectionMinutes = null;
    private Boolean mForceCloseDialogAfterToast = null;
    private String mFormKey = null;
    private String mFormUri = null;
    private String mFormUriBasicAuthLogin = null;
    private String mFormUriBasicAuthPassword = null;
    private Boolean mIncludeDropboxSystemTags = null;

    private String[] mLogcatArguments = null;
    private String mMailTo = null;
    private Integer mMaxNumberOfRequestRetries = null;
    private ReportingInteractionMode mMode = null;
    private ReportsCrashes mReportsCrashes = null;

    private Integer mResDialogCommentPrompt = null;
    private Integer mResDialogEmailPrompt = null;
    private Integer mResDialogIcon = null;
    private Integer mResDialogOkToast = null;
    private Integer mResDialogText = null;
    private Integer mResDialogTitle = null;
    private Integer mResNotifIcon = null;
    private Integer mResNotifText = null;
    private Integer mResNotifTickerText = null;
    private Integer mResNotifTitle = null;
    private Integer mResToastText = null;
    private Integer mSharedPreferenceMode = null;
    private String mSharedPreferenceName = null;
    private Integer mSocketTimeout = null;
    private Boolean mLogcatFilterByPid = null;
    private Boolean mSendReportsInDevMode = null;

    private String[] mExcludeMatchingSharedPreferencesKeys = null;
    private String[] mExcludeMatchingSettingsKeys = null;
    private String mApplicationLogFile = null;
    private Integer mApplicationLogFileLines = null;

    private String mGoogleFormUrlFormat = null;

    private Boolean mDisableSSLCertValidation = null;
    private Method mHttpMethod = null;
    private Type mReportType = null;

    /**
     * @param additionalDropboxTags
     *            the additionalDropboxTags to set
     */
    public void setAdditionalDropboxTags(String[] additionalDropboxTags) {
        this.mAdditionalDropboxTags = additionalDropboxTags;
    }

    /**
     * @param additionalSharedPreferences
     *            the additionalSharedPreferences to set
     */
    public void setAdditionalSharedPreferences(String[] additionalSharedPreferences) {
        this.mAdditionalSharedPreferences = additionalSharedPreferences;
    }

    /**
     * @param connectionTimeout
     *            the connectionTimeout to set
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.mConnectionTimeout = connectionTimeout;
    }

    /**
     * @param customReportContent
     *            the customReportContent to set
     */
    public void setCustomReportContent(ReportField[] customReportContent) {
        this.mCustomReportContent = customReportContent;
    }

    /**
     * @param deleteUnapprovedReportsOnApplicationStart
     *            the deleteUnapprovedReportsOnApplicationStart to set
     */
    public void setDeleteUnapprovedReportsOnApplicationStart(Boolean deleteUnapprovedReportsOnApplicationStart) {
        this.mDeleteUnapprovedReportsOnApplicationStart = deleteUnapprovedReportsOnApplicationStart;
    }

    /**
     * @param deleteOldUnsetReportsOnApplicationStart
     */
    public void setDeleteOldUnsentReportsOnApplicationStart(Boolean deleteOldUnsetReportsOnApplicationStart) {
        this.mDeleteOldUnsentReportsOnApplicationStart = deleteOldUnsetReportsOnApplicationStart;
    }

    /**
     * @param dropboxCollectionMinutes
     *            the dropboxCollectionMinutes to set
     */
    public void setDropboxCollectionMinutes(Integer dropboxCollectionMinutes) {
        this.mDropboxCollectionMinutes = dropboxCollectionMinutes;
    }

    /**
     * @param forceCloseDialogAfterToast
     *            the forceCloseDialogAfterToast to set
     */
    public void setForceCloseDialogAfterToast(Boolean forceCloseDialogAfterToast) {
        this.mForceCloseDialogAfterToast = forceCloseDialogAfterToast;
    }

    /**
     * Modify the formKey of the Google Docs form receiving reports. You need to
     * call {@link ErrorReporter#setDefaultReportSenders()} after modifying this
     * value if you were not using a formKey before (a mailTo or formUri
     * instead).
     * 
     * @param formKey
     *            the formKey to set
     */
    public void setFormKey(String formKey) {
        this.mFormKey = formKey;
    }

    /**
     * Modify the formUri of your backend server receiving reports. You need to
     * call {@link ErrorReporter#setDefaultReportSenders()} after modifying this
     * value if you were not using a formUri before (a mailTo or formKey
     * instead).
     * 
     * @param formUri
     *            the formUri to set
     */
    public void setFormUri(String formUri) {
        this.mFormUri = formUri;
    }

    /**
     * @param formUriBasicAuthLogin
     *            the formUriBasicAuthLogin to set
     */
    public void setFormUriBasicAuthLogin(String formUriBasicAuthLogin) {
        this.mFormUriBasicAuthLogin = formUriBasicAuthLogin;
    }

    /**
     * @param formUriBasicAuthPassword
     *            the formUriBasicAuthPassword to set
     */
    public void setFormUriBasicAuthPassword(String formUriBasicAuthPassword) {
        this.mFormUriBasicAuthPassword = formUriBasicAuthPassword;
    }

    /**
     * @param includeDropboxSystemTags
     *            the includeDropboxSystemTags to set
     */
    public void setIncludeDropboxSystemTags(Boolean includeDropboxSystemTags) {
        this.mIncludeDropboxSystemTags = includeDropboxSystemTags;
    }

    /**
     * @param logcatArguments
     *            the logcatArguments to set
     */
    public void setLogcatArguments(String[] logcatArguments) {
        this.mLogcatArguments = logcatArguments;
    }

    /**
     * Modify the mailTo of the mail account receiving reports. You need to call
     * {@link ErrorReporter#setDefaultReportSenders()} after modifying this
     * value if you were not using a formKey before (a formKey or formUri
     * instead).
     * 
     * @param mailTo
     *            the mailTo to set
     */
    public void setMailTo(String mailTo) {
        this.mMailTo = mailTo;
    }

    /**
     * @param maxNumberOfRequestRetries
     *            the maxNumberOfRequestRetries to set
     */
    public void setMaxNumberOfRequestRetries(Integer maxNumberOfRequestRetries) {
        this.mMaxNumberOfRequestRetries = maxNumberOfRequestRetries;
    }

    /**
     * Change the current {@link ReportingInteractionMode}. You must set
     * required configuration items first.
     * 
     * @param mode
     *            the new mode to set.
     * @throws ACRAConfigurationException
     *             if a configuration item is missing for this mode.
     */
    public void setMode(ReportingInteractionMode mode) throws ACRAConfigurationException {
        this.mMode = mode;
        ACRA.checkCrashResources();
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogCommentPrompt()} comes from an Android
     * Library Project.
     * 
     * @param resId
     *            The resource id, see
     *            {@link ReportsCrashes#resDialogCommentPrompt()}
     */
    public void setResDialogCommentPrompt(int resId) {
        mResDialogCommentPrompt = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogEmailPrompt()} comes from an Android
     * Library Project.
     * 
     * @param resId
     *            The resource id, see
     *            {@link ReportsCrashes#resDialogEmailPrompt()}
     */
    public void setResDialogEmailPrompt(int resId) {
        mResDialogEmailPrompt = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogIcon()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogIcon()}
     */
    public void setResDialogIcon(int resId) {
        mResDialogIcon = resId;
    }

    /**
     * Use this method BEFORE if the id you wanted to give to
     * {@link ReportsCrashes#resDialogOkToast()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogOkToast()}
     */
    public void setResDialogOkToast(int resId) {
        mResDialogOkToast = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogText()}
     */
    public void setResDialogText(int resId) {
        mResDialogText = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogTitle()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogTitle()}
     */
    public void setResDialogTitle(int resId) {
        mResDialogTitle = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifIcon()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifIcon()}
     */
    public void setResNotifIcon(int resId) {
        mResNotifIcon = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifText()}
     */
    public void setResNotifText(int resId) {
        mResNotifText = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTickerText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see
     *            {@link ReportsCrashes#resNotifTickerText()}
     */
    public void setResNotifTickerText(int resId) {
        mResNotifTickerText = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTitle()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifTitle()}
     */
    public void setResNotifTitle(int resId) {
        mResNotifTitle = resId;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resToastText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resToastText()}
     */
    public void setResToastText(int resId) {
        mResToastText = resId;
    }

    /**
     * @param sharedPreferenceMode
     *            the sharedPreferenceMode to set
     */
    public void setSharedPreferenceMode(Integer sharedPreferenceMode) {
        this.mSharedPreferenceMode = sharedPreferenceMode;
    }

    /**
     * @param sharedPreferenceName
     *            the sharedPreferenceName to set
     */
    public void setSharedPreferenceName(String sharedPreferenceName) {
        this.mSharedPreferenceName = sharedPreferenceName;
    }

    /**
     * @param socketTimeout
     *            the socketTimeout to set
     */
    public void setSocketTimeout(Integer socketTimeout) {
        this.mSocketTimeout = socketTimeout;
    }

    /**
     * 
     * @param filterByPid
     *            true if you want to collect only logcat lines related to your
     *            application process.
     */
    public void setLogcatFilterByPid(Boolean filterByPid) {
        mLogcatFilterByPid = filterByPid;
    }

    /**
     * 
     * @param sendReportsInDevMode
     *            false if you want to disable sending reports in development
     *            mode. Reports will be sent only on signed applications.
     */
    public void setSendReportsInDevMode(Boolean sendReportsInDevMode) {
        mSendReportsInDevMode = sendReportsInDevMode;
    }

    /**
     * 
     * @param excludeMatchingSharedPreferencesKeys
     *            an array of Strings containing regexp defining
     *            SharedPreferences keys that should be excluded from the data
     *            collection.
     */
    public void setExcludeMatchingSharedPreferencesKeys(String[] excludeMatchingSharedPreferencesKeys) {
        mExcludeMatchingSharedPreferencesKeys = excludeMatchingSharedPreferencesKeys;
    }

    /**
     * 
     * @param excludeMatchingSettingsKeys
     *            an array of Strings containing regexp defining
     *            Settings.System, Settings.Secure and Settings.Global keys that
     *            should be excluded from the data collection.
     */
    public void setExcludeMatchingSettingsKeys(String[] excludeMatchingSettingsKeys) {
        mExcludeMatchingSettingsKeys = excludeMatchingSettingsKeys;
    }

    /**
     * 
     * @param applicationLogFile
     *            The path and file name of your application log file, to be
     *            used with {@link ReportField#APPLICATION_LOG}.
     */
    public void setApplicationLogFile(String applicationLogFile) {
        mApplicationLogFile = applicationLogFile;
    }

    /**
     * 
     * @param applicationLogFileLines
     *            The number of lines of your application log to be collected,
     *            to be used with {@link ReportField#APPLICATION_LOG} and
     *            {@link ReportsCrashes#applicationLogFile()}.
     */
    public void setApplicationLogFileLines(int applicationLogFileLines) {
        mApplicationLogFileLines = applicationLogFileLines;
    }

    /**
     * 
     * @param disableSSLCertValidation
     *            Set this to true if you need to send reports to a server over
     *            SSL using a self-signed certificate.
     */
    public void setDisableSSLCertValidation(boolean disableSSLCertValidation) {
        mDisableSSLCertValidation = disableSSLCertValidation;
    }

    /**
     * 
     * @param httpMethod
     *            The method to be used to send data to the server.
     */
    public void setHttpMethod(Method httpMethod) {
        mHttpMethod = httpMethod;
    }

    /**
     * 
     * @param type
     *            The type of content encoding to be used to send data to the
     *            server.
     */
    public void setReportType(Type type) {
        mReportType = type;
    }

    /**
     * 
     * @param defaults
     */
    public ACRAConfiguration(ReportsCrashes defaults) {
        mReportsCrashes = defaults;
    }

    @Override
    public String[] additionalDropBoxTags() {
        if (mAdditionalDropboxTags != null) {
            return mAdditionalDropboxTags;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.additionalDropBoxTags();
        }

        String[] defaultValue = {};
        return defaultValue;
    }

    @Override
    public String[] additionalSharedPreferences() {
        if (mAdditionalSharedPreferences != null) {
            return mAdditionalSharedPreferences;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.additionalSharedPreferences();
        }

        String[] defaultValue = {};
        return defaultValue;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return mReportsCrashes.annotationType();
    }

    @Override
    public int connectionTimeout() {
        if (mConnectionTimeout != null) {
            return mConnectionTimeout;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.connectionTimeout();
        }

        return DEFAULT_CONNECTION_TIMEOUT;
    }

    @Override
    public ReportField[] customReportContent() {
        if (mCustomReportContent != null) {
            return mCustomReportContent;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.customReportContent();
        }

        ReportField[] defaultValue = {};
        return defaultValue;
    }

    @Override
    public boolean deleteUnapprovedReportsOnApplicationStart() {
        if (mDeleteUnapprovedReportsOnApplicationStart != null) {
            return mDeleteUnapprovedReportsOnApplicationStart;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.deleteUnapprovedReportsOnApplicationStart();
        }

        return DEFAULT_DELETE_UNAPPROVED_REPORTS_ON_APPLICATION_START;
    }

    @Override
    public boolean deleteOldUnsentReportsOnApplicationStart() {
        if (mDeleteOldUnsentReportsOnApplicationStart != null) {
            return mDeleteOldUnsentReportsOnApplicationStart;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.deleteOldUnsentReportsOnApplicationStart();
        }

        return DEFAULT_DELETE_OLD_UNSENT_REPORTS_ON_APPLICATION_START;
    }

    @Override
    public int dropboxCollectionMinutes() {
        if (mDropboxCollectionMinutes != null) {
            return mDropboxCollectionMinutes;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.dropboxCollectionMinutes();
        }

        return DEFAULT_DROPBOX_COLLECTION_MINUTES;
    }

    @Override
    public boolean forceCloseDialogAfterToast() {
        if (mForceCloseDialogAfterToast != null) {
            return mForceCloseDialogAfterToast;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.forceCloseDialogAfterToast();
        }

        return DEFAULT_FORCE_CLOSE_DIALOG_AFTER_TOAST;
    }

    @Override
    public String formKey() {
        if (mFormKey != null) {
            return mFormKey;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.formKey();
        }

        return DEFAULT_STRING_VALUE;
    }

    @Override
    public String formUri() {
        if (mFormUri != null) {
            return mFormUri;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.formUri();
        }

        return DEFAULT_STRING_VALUE;
    }

    @Override
    public String formUriBasicAuthLogin() {
        if (mFormUriBasicAuthLogin != null) {
            return mFormUriBasicAuthLogin;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.formUriBasicAuthLogin();
        }

        return NULL_VALUE;
    }

    @Override
    public String formUriBasicAuthPassword() {
        if (mFormUriBasicAuthPassword != null) {
            return mFormUriBasicAuthPassword;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.formUriBasicAuthPassword();
        }

        return NULL_VALUE;
    }

    @Override
    public boolean includeDropBoxSystemTags() {
        if (mIncludeDropboxSystemTags != null) {
            return mIncludeDropboxSystemTags;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.includeDropBoxSystemTags();
        }

        return DEFAULT_INCLUDE_DROPBOX_SYSTEM_TAGS;
    }

    @Override
    public String[] logcatArguments() {
        if (mLogcatArguments != null) {
            return mLogcatArguments;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.logcatArguments();
        }

        String[] defaultValues = { "-t", Integer.toString(DEFAULT_LOGCAT_LINES), "-v", "time" };
        return defaultValues;
    }

    @Override
    public String mailTo() {
        if (mMailTo != null) {
            return mMailTo;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.mailTo();
        }

        return DEFAULT_STRING_VALUE;
    }

    @Override
    public int maxNumberOfRequestRetries() {
        if (mMaxNumberOfRequestRetries != null) {
            return mMaxNumberOfRequestRetries;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.maxNumberOfRequestRetries();
        }

        return DEFAULT_MAX_NUMBER_OF_REQUEST_RETRIES;
    }

    @Override
    public ReportingInteractionMode mode() {
        if (mMode != null) {
            return mMode;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.mode();
        }

        return ReportingInteractionMode.SILENT;
    }

    @Override
    public int resDialogCommentPrompt() {
        if (mResDialogCommentPrompt != null) {
            return mResDialogCommentPrompt;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resDialogCommentPrompt();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int resDialogEmailPrompt() {
        if (mResDialogEmailPrompt != null) {
            return mResDialogEmailPrompt;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resDialogEmailPrompt();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int resDialogIcon() {
        if (mResDialogIcon != null) {
            return mResDialogIcon;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resDialogIcon();
        }

        return DEFAULT_DIALOG_ICON;
    }

    @Override
    public int resDialogOkToast() {
        if (mResDialogOkToast != null) {
            return mResDialogOkToast;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resDialogOkToast();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int resDialogText() {
        if (mResDialogText != null) {
            return mResDialogText;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resDialogText();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int resDialogTitle() {
        if (mResDialogTitle != null) {
            return mResDialogTitle;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resDialogTitle();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int resNotifIcon() {
        if (mResNotifIcon != null) {
            return mResNotifIcon;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resNotifIcon();
        }

        return DEFAULT_NOTIFICATION_ICON;
    }

    @Override
    public int resNotifText() {
        if (mResNotifText != null) {
            return mResNotifText;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resNotifText();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int resNotifTickerText() {
        if (mResNotifTickerText != null) {
            return mResNotifTickerText;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resNotifTickerText();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int resNotifTitle() {
        if (mResNotifTitle != null) {
            return mResNotifTitle;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resNotifTitle();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int resToastText() {
        if (mResToastText != null) {
            return mResToastText;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.resToastText();
        }

        return DEFAULT_RES_VALUE;
    }

    @Override
    public int sharedPreferencesMode() {
        if (mSharedPreferenceMode != null) {
            return mSharedPreferenceMode;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.sharedPreferencesMode();
        }

        return DEFAULT_SHARED_PREFERENCES_MODE;
    }

    @Override
    public String sharedPreferencesName() {
        if (mSharedPreferenceName != null) {
            return mSharedPreferenceName;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.sharedPreferencesName();
        }

        return DEFAULT_STRING_VALUE;
    }

    @Override
    public int socketTimeout() {
        if (mSocketTimeout != null) {
            return mSocketTimeout;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.socketTimeout();
        }

        return DEFAULT_SOCKET_TIMEOUT;
    }

    @Override
    public boolean logcatFilterByPid() {
        if (mLogcatFilterByPid != null) {
            return mLogcatFilterByPid;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.logcatFilterByPid();
        }

        return DEFAULT_LOGCAT_FILTER_BY_PID;
    }

    @Override
    public boolean sendReportsInDevMode() {
        if (mSendReportsInDevMode != null) {
            return mSendReportsInDevMode;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.sendReportsInDevMode();
        }

        return DEFAULT_SEND_REPORTS_IN_DEV_MODE;
    }

    @Override
    public String[] excludeMatchingSharedPreferencesKeys() {
        if (mExcludeMatchingSharedPreferencesKeys != null) {
            return mExcludeMatchingSharedPreferencesKeys;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.excludeMatchingSharedPreferencesKeys();
        }

        String[] defaultValue = {};

        return defaultValue;
    }

    @Override
    public String[] excludeMatchingSettingsKeys() {
        if (mExcludeMatchingSettingsKeys != null) {
            return mExcludeMatchingSettingsKeys;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.excludeMatchingSettingsKeys();
        }

        String[] defaultValue = {};

        return defaultValue;
    }

    @Override
    public String applicationLogFile() {
        if (mApplicationLogFile != null) {
            return mApplicationLogFile;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.applicationLogFile();
        }

        return DEFAULT_APPLICATION_LOGFILE;
    }

    @Override
    public int applicationLogFileLines() {
        if (mApplicationLogFileLines != null) {
            return mApplicationLogFileLines;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.applicationLogFileLines();
        }

        return DEFAULT_APPLICATION_LOGFILE_LINES;
    }

    @Override
    public String googleFormUrlFormat() {
        if (mGoogleFormUrlFormat != null) {
            return mGoogleFormUrlFormat;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.googleFormUrlFormat();
        }

        return DEFAULT_GOOGLE_FORM_URL_FORMAT;
    }

    @Override
    public boolean disableSSLCertValidation() {
        if (mDisableSSLCertValidation != null) {
            return mDisableSSLCertValidation;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.disableSSLCertValidation();
        }

        return DEFAULT_DISABLE_SSL_CERT_VALIDATION;
    }

    @Override
    public Method httpMethod() {
        if (mHttpMethod != null) {
            return mHttpMethod;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.httpMethod();
        }

        return Method.POST;
    }

    @Override
    public Type reportType() {
        if (mReportType != null) {
            return mReportType;
        }

        if (mReportsCrashes != null) {
            return mReportsCrashes.reportType();
        }

        return Type.FORM;
    }

    public static boolean isNull(String aString) {
        return aString == null || ACRAConstants.NULL_VALUE.equals(aString);
    }

}
