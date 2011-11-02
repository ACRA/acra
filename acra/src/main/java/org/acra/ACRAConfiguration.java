package org.acra;

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

import java.lang.annotation.Annotation;

import org.acra.annotation.ReportsCrashes;

import android.app.Application;

/**
 * This class is to be used if you need to apply dynamic settings. This is
 * needed for example when using ACRA in an Android Library Project since ADT
 * v14 where resource ids are not final anymore and can't be passed as
 * annotation parameters values.
 * 
 */
public class ACRAConfiguration implements ReportsCrashes {

    private static ReportsCrashes mReportsCrashes = null;

    /**
     * Since ADT v14, when using Android Library Projects, resource Ids can't be
     * passed as annotation parameter values anymore. In this case, devs can use
     * setters to pass their Ids. These setters have to be called before
     * {@link ACRA#init(Application)}. This method is called early in
     * {@link ACRA#init(Application)} to initialize the {@link ReportsCrashes}
     * annotation with values passed in the setters.
     */
    private static Integer RES_DIALOG_COMMENT_PROMPT = null;
    private static Integer RES_DIALOG_EMAIL_PROMPT = null;
    private static Integer RES_DIALOG_ICON = null;
    private static Integer RES_DIALOG_OK_TOAST = null;
    private static Integer RES_DIALOG_TEXT = null;
    private static Integer RES_DIALOG_TITLE = null;
    private static Integer RES_NOTIF_ICON = null;
    private static Integer RES_NOTIF_TEXT = null;
    private static Integer RES_NOTIF_TICKER_TEXT = null;
    private static Integer RES_NOTIF_TITLE = null;
    private static Integer RES_TOAST_TEXT = null;

    public ACRAConfiguration(ReportsCrashes defaults) {
        mReportsCrashes = defaults;
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
        RES_DIALOG_COMMENT_PROMPT = resId;
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
        RES_DIALOG_EMAIL_PROMPT = resId;
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
        RES_DIALOG_ICON = resId;
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
        RES_DIALOG_OK_TOAST = resId;
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
        RES_DIALOG_TEXT = resId;
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
        RES_DIALOG_TITLE = resId;
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
        RES_NOTIF_ICON = resId;
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
        RES_NOTIF_TEXT = resId;
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
        RES_NOTIF_TICKER_TEXT = resId;
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
        RES_NOTIF_TITLE = resId;
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
        RES_TOAST_TEXT = resId;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return mReportsCrashes.annotationType();
    }

    @Override
    public int socketTimeout() {
        return mReportsCrashes.socketTimeout();
    }

    @Override
    public String sharedPreferencesName() {
        return mReportsCrashes.sharedPreferencesName();
    }

    @Override
    public int sharedPreferencesMode() {
        return mReportsCrashes.sharedPreferencesMode();
    }

    @Override
    public int resToastText() {
        if (RES_TOAST_TEXT != null) {
            return RES_TOAST_TEXT;
        } else {
            return mReportsCrashes.resToastText();
        }
    }

    @Override
    public int resNotifTitle() {
        if (RES_NOTIF_TITLE != null) {
            return RES_NOTIF_TITLE;
        } else {
            return mReportsCrashes.resNotifTitle();
        }
    }

    @Override
    public int resNotifTickerText() {
        if (RES_NOTIF_TICKER_TEXT != null) {
            return RES_NOTIF_TICKER_TEXT;
        } else {
            return mReportsCrashes.resNotifTickerText();
        }
    }

    @Override
    public int resNotifText() {
        if (RES_NOTIF_TEXT != null) {
            return RES_NOTIF_TEXT;
        } else {
            return mReportsCrashes.resNotifText();
        }
    }

    @Override
    public int resNotifIcon() {
        if (RES_NOTIF_ICON != null) {
            return RES_NOTIF_ICON;
        } else {
            return mReportsCrashes.resNotifIcon();
        }
    }

    @Override
    public int resDialogTitle() {
        if (RES_DIALOG_TITLE != null) {
            return RES_DIALOG_TITLE;
        } else {
            return mReportsCrashes.resDialogTitle();
        }
    }

    @Override
    public int resDialogText() {
        if (RES_DIALOG_TEXT != null) {
            return RES_DIALOG_TEXT;
        } else {
            return mReportsCrashes.resDialogText();
        }
    }

    @Override
    public int resDialogOkToast() {
        if (RES_DIALOG_OK_TOAST != null) {
            return RES_DIALOG_OK_TOAST;
        } else {
            return mReportsCrashes.resDialogOkToast();
        }
    }

    @Override
    public int resDialogIcon() {
        if (RES_DIALOG_ICON != null) {
            return RES_DIALOG_ICON;
        } else {
            return mReportsCrashes.resDialogIcon();
        }
    }

    @Override
    public int resDialogEmailPrompt() {
        if (RES_DIALOG_EMAIL_PROMPT != null) {
            return RES_DIALOG_EMAIL_PROMPT;
        } else {
            return mReportsCrashes.resDialogEmailPrompt();
        }
    }

    @Override
    public int resDialogCommentPrompt() {
        if (RES_DIALOG_COMMENT_PROMPT != null) {
            return RES_DIALOG_COMMENT_PROMPT;
        } else {
            return mReportsCrashes.resDialogCommentPrompt();
        }
    }

    @Override
    public ReportingInteractionMode mode() {
        return mReportsCrashes.mode();
    }

    @Override
    public int maxNumberOfRequestRetries() {
        return mReportsCrashes.maxNumberOfRequestRetries();
    }

    @Override
    public String mailTo() {
        return mReportsCrashes.mailTo();
    }

    @Override
    public String[] logcatArguments() {
        return mReportsCrashes.logcatArguments();
    }

    @Override
    public boolean includeDropBoxSystemTags() {
        return mReportsCrashes.includeDropBoxSystemTags();
    }

    @Override
    public String formUriBasicAuthPassword() {
        return mReportsCrashes.formUriBasicAuthPassword();
    }

    @Override
    public String formUriBasicAuthLogin() {
        return mReportsCrashes.formUriBasicAuthLogin();
    }

    @Override
    public String formUri() {
        return mReportsCrashes.formUri();
    }

    @Override
    public String formKey() {
        return mReportsCrashes.formKey();
    }

    @Override
    public boolean forceCloseDialogAfterToast() {
        return mReportsCrashes.forceCloseDialogAfterToast();
    }

    @Override
    public int dropboxCollectionMinutes() {
        return mReportsCrashes.dropboxCollectionMinutes();
    }

    @Override
    public boolean deleteUnapprovedReportsOnApplicationStart() {
        return mReportsCrashes.deleteUnapprovedReportsOnApplicationStart();
    }

    @Override
    public ReportField[] customReportContent() {
        return mReportsCrashes.customReportContent();
    }

    @Override
    public int connectionTimeout() {
        return mReportsCrashes.connectionTimeout();
    }

    @Override
    public String[] additionalSharedPreferences() {
        return mReportsCrashes.additionalSharedPreferences();
    }

    @Override
    public String[] additionalDropBoxTags() {
        return mReportsCrashes.additionalDropBoxTags();
    }
}
