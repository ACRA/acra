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

import java.lang.annotation.Annotation;
import static org.acra.ACRAConstants.*;

import org.acra.annotation.ReportsCrashes;

/**
 * This class is to be used if you need to apply dynamic settings. This is
 * needed for example when using ACRA in an Android Library Project since ADT
 * v14 where resource ids are not final anymore and can't be passed as
 * annotation parameters values.
 * 
 */
public class ACRAConfiguration implements ReportsCrashes {

    private static String[] mAdditionalDropboxTags = null;

    private static String[] mAdditionalSharedPreferences = null;
    private static Integer mConnectionTimeout = null;
    private static ReportField[] mCustomReportContent = null;
    private static Boolean mDeleteUnapprovedReportsOnApplicationStart = null;
    private static Integer mDropboxCollectionMinutes = null;
    private static Boolean mForceCloseDialogAfterToast = null;
    private static String mFormKey = null;
    private static String mFormUri = null;
    private static String mFormUriBasicAuthLogin = null;
    private static String mFormUriBasicAuthPassword = null;
    private static Boolean mIncludeDropboxSystemTags = null;

    private static String[] mLogcatArguments = null;
    private static String mMailTo = null;
    private static Integer mMaxNumberOfRequestRetries = null;
    private static ReportingInteractionMode mMode = null;
    private static ReportsCrashes mReportsCrashes = null;


    private static Integer mResDialogCommentPrompt = null;
    private static Integer mResDialogEmailPrompt = null;
    private static Integer mResDialogIcon = null;
    private static Integer mResDialogOkToast = null;
    private static Integer mResDialogText = null;
    private static Integer mResDialogTitle = null;
    private static Integer mResNotifIcon = null;
    private static Integer mResNotifText = null;
    private static Integer mResNotifTickerText = null;
    private static Integer mResNotifTitle = null;
    private static Integer mResToastText = null;
    private static Integer mSharedPreferenceMode = null;
    private static String mSharedPreferenceName = null;
    private static Integer mSocketTimeout = null;

    /**
     * @param additionalDropboxTags
     *            the additionalDropboxTags to set
     */
    public static void setAdditionalDropboxTags(String[] additionalDropboxTags) {
        ACRAConfiguration.mAdditionalDropboxTags = additionalDropboxTags;
    }

    /**
     * @param additionalSharedPreferences
     *            the additionalSharedPreferences to set
     */
    public static void setAdditionalSharedPreferences(String[] additionalSharedPreferences) {
        ACRAConfiguration.mAdditionalSharedPreferences = additionalSharedPreferences;
    }

    /**
     * @param connectionTimeout
     *            the connectionTimeout to set
     */
    public static void setConnectionTimeout(Integer connectionTimeout) {
        ACRAConfiguration.mConnectionTimeout = connectionTimeout;
    }

    /**
     * @param customReportContent
     *            the customReportContent to set
     */
    public static void setCustomReportContent(ReportField[] customReportContent) {
        ACRAConfiguration.mCustomReportContent = customReportContent;
    }

    /**
     * @param deleteUnapprovedReportsOnApplicationStart
     *            the deleteUnapprovedReportsOnApplicationStart to set
     */
    public static void setDeleteUnapprovedReportsOnApplicationStart(Boolean deleteUnapprovedReportsOnApplicationStart) {
        ACRAConfiguration.mDeleteUnapprovedReportsOnApplicationStart = deleteUnapprovedReportsOnApplicationStart;
    }

    /**
     * @param dropboxCollectionMinutes
     *            the dropboxCollectionMinutes to set
     */
    public static void setDropboxCollectionMinutes(Integer dropboxCollectionMinutes) {
        ACRAConfiguration.mDropboxCollectionMinutes = dropboxCollectionMinutes;
    }

    /**
     * @param forceCloseDialogAfterToast
     *            the forceCloseDialogAfterToast to set
     */
    public static void setForceCloseDialogAfterToast(Boolean forceCloseDialogAfterToast) {
        ACRAConfiguration.mForceCloseDialogAfterToast = forceCloseDialogAfterToast;
    }

    /**
     * @param formKey
     *            the formKey to set
     */
    public static void setFormKey(String formKey) {
        ACRAConfiguration.mFormKey = formKey;
    }

    /**
     * @param formUri
     *            the formUri to set
     */
    public static void setFormUri(String formUri) {
        ACRAConfiguration.mFormUri = formUri;
    }

    /**
     * @param formUriBasicAuthLogin
     *            the formUriBasicAuthLogin to set
     */
    public static void setFormUriBasicAuthLogin(String formUriBasicAuthLogin) {
        ACRAConfiguration.mFormUriBasicAuthLogin = formUriBasicAuthLogin;
    }

    /**
     * @param formUriBasicAuthPassword
     *            the formUriBasicAuthPassword to set
     */
    public static void setFormUriBasicAuthPassword(String formUriBasicAuthPassword) {
        ACRAConfiguration.mFormUriBasicAuthPassword = formUriBasicAuthPassword;
    }

    /**
     * @param includeDropboxSystemTags
     *            the includeDropboxSystemTags to set
     */
    public static void setIncludeDropboxSystemTags(Boolean includeDropboxSystemTags) {
        ACRAConfiguration.mIncludeDropboxSystemTags = includeDropboxSystemTags;
    }

    /**
     * @param logcatArguments
     *            the logcatArguments to set
     */
    public static void setLogcatArguments(String[] logcatArguments) {
        ACRAConfiguration.mLogcatArguments = logcatArguments;
    }

    /**
     * @param mailTo
     *            the mailTo to set
     */
    public static void setMailTo(String mailTo) {
        ACRAConfiguration.mMailTo = mailTo;
    }

    /**
     * @param maxNumberOfRequestRetries
     *            the maxNumberOfRequestRetries to set
     */
    public static void setMaxNumberOfRequestRetries(Integer maxNumberOfRequestRetries) {
        ACRAConfiguration.mMaxNumberOfRequestRetries = maxNumberOfRequestRetries;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public static void setMode(ReportingInteractionMode mode) {
        ACRAConfiguration.mMode = mode;
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
    public static void setResDialogCommentPrompt(int resId) {
        mResDialogCommentPrompt = resId;
    }

    /**
     * @param resDialogCommentPrompt
     *            the resDialogCommentPrompt to set
     */
    public static void setResDialogCommentPrompt(Integer resDialogCommentPrompt) {
        ACRAConfiguration.mResDialogCommentPrompt = resDialogCommentPrompt;
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
    public static void setResDialogEmailPrompt(int resId) {
        mResDialogEmailPrompt = resId;
    }

    /**
     * @param resDialogEmailPrompt
     *            the resDialogEmailPrompt to set
     */
    public static void setResDialogEmailPrompt(Integer resDialogEmailPrompt) {
        ACRAConfiguration.mResDialogEmailPrompt = resDialogEmailPrompt;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogIcon()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogIcon()}
     */
    public static void setResDialogIcon(int resId) {
        mResDialogIcon = resId;
    }

    /**
     * @param resDialogIcon
     *            the resDialogIcon to set
     */
    public static void setResDialogIcon(Integer resDialogIcon) {
        ACRAConfiguration.mResDialogIcon = resDialogIcon;
    }

    /**
     * Use this method BEFORE if the id you wanted to give to
     * {@link ReportsCrashes#resDialogOkToast()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogOkToast()}
     */
    public static void setResDialogOkToast(int resId) {
        mResDialogOkToast = resId;
    }

    /**
     * @param resDialogOkToast
     *            the resDialogOkToast to set
     */
    public static void setResDialogOkToast(Integer resDialogOkToast) {
        ACRAConfiguration.mResDialogOkToast = resDialogOkToast;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogText()}
     */
    public static void setResDialogText(int resId) {
        mResDialogText = resId;
    }

    /**
     * @param resDialogText
     *            the resDialogText to set
     */
    public static void setResDialogText(Integer resDialogText) {
        ACRAConfiguration.mResDialogText = resDialogText;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogTitle()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resDialogTitle()}
     */
    public static void setResDialogTitle(int resId) {
        mResDialogTitle = resId;
    }

    /**
     * @param resDialogTitle
     *            the resDialogTitle to set
     */
    public static void setResDialogTitle(Integer resDialogTitle) {
        ACRAConfiguration.mResDialogTitle = resDialogTitle;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifIcon()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifIcon()}
     */
    public static void setResNotifIcon(int resId) {
        mResNotifIcon = resId;
    }

    /**
     * @param resNotifIcon
     *            the resNotifIcon to set
     */
    public static void setResNotifIcon(Integer resNotifIcon) {
        ACRAConfiguration.mResNotifIcon = resNotifIcon;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifText()}
     */
    public static void setResNotifText(int resId) {
        mResNotifText = resId;
    }

    /**
     * @param resNotifText
     *            the resNotifText to set
     */
    public static void setResNotifText(Integer resNotifText) {
        ACRAConfiguration.mResNotifText = resNotifText;
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
    public static void setResNotifTickerText(int resId) {
        mResNotifTickerText = resId;
    }

    /**
     * @param resNotifTickerText
     *            the resNotifTickerText to set
     */
    public static void setResNotifTickerText(Integer resNotifTickerText) {
        ACRAConfiguration.mResNotifTickerText = resNotifTickerText;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTitle()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resNotifTitle()}
     */
    public static void setResNotifTitle(int resId) {
        mResNotifTitle = resId;
    }

    /**
     * @param resNotifTitle
     *            the resNotifTitle to set
     */
    public static void setResNotifTitle(Integer resNotifTitle) {
        ACRAConfiguration.mResNotifTitle = resNotifTitle;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resToastText()} comes from an Android Library
     * Project.
     * 
     * @param resId
     *            The resource id, see {@link ReportsCrashes#resToastText()}
     */
    public static void setResToastText(int resId) {
        mResToastText = resId;
    }

    /**
     * @param resToastText
     *            the resToastText to set
     */
    public static void setResToastText(Integer resToastText) {
        ACRAConfiguration.mResToastText = resToastText;
    }

    /**
     * @param sharedPreferenceMode
     *            the sharedPreferenceMode to set
     */
    public static void setSharedPreferenceMode(Integer sharedPreferenceMode) {
        ACRAConfiguration.mSharedPreferenceMode = sharedPreferenceMode;
    }

    /**
     * @param sharedPreferenceName
     *            the sharedPreferenceName to set
     */
    public static void setSharedPreferenceName(String sharedPreferenceName) {
        ACRAConfiguration.mSharedPreferenceName = sharedPreferenceName;
    }

    /**
     * @param socketTimeout
     *            the socketTimeout to set
     */
    public static void setSocketTimeout(Integer socketTimeout) {
        ACRAConfiguration.mSocketTimeout = socketTimeout;
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

        String[] defaultValues = { "-t", DEFAULT_LOGCAT_LINES, "-v", "time" };
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
}
