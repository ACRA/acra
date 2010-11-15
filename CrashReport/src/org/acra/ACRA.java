/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
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

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Use this class to initialize the crash reporting feature using
 * {@link #init(Application)} as soon as possible in your {@link Application}
 * subclass {@link Application#onCreate()} method. Configuration items must have
 * been set by using {@link ReportsCrashes} above the declaration of your
 * {@link Application} subclass.
 * 
 * @author Kevin Gaudin
 * 
 */
public class ACRA {
    protected static final String LOG_TAG = ACRA.class.getSimpleName();

    /**
     * Bundle key for the icon in the status bar notification.
     */
    static final String RES_NOTIF_ICON = "RES_NOTIF_ICON";
    /**
     * Bundle key for the ticker text in the status bar notification.
     */
    static final String RES_NOTIF_TICKER_TEXT = "RES_NOTIF_TICKER_TEXT";
    /**
     * Bundle key for the title in the status bar notification.
     */
    static final String RES_NOTIF_TITLE = "RES_NOTIF_TITLE";
    /**
     * Bundle key for the text in the status bar notification.
     */
    static final String RES_NOTIF_TEXT = "RES_NOTIF_TEXT";
    /**
     * Bundle key for the icon in the crash dialog.
     */
    static final String RES_DIALOG_ICON = "RES_DIALOG_ICON";
    /**
     * Bundle key for the title in the crash dialog.
     */
    static final String RES_DIALOG_TITLE = "RES_DIALOG_TITLE";
    /**
     * Bundle key for the text in the crash dialog.
     */
    static final String RES_DIALOG_TEXT = "RES_DIALOG_TEXT";
    /**
     * Bundle key for the user comment input label in the crash dialog. If not
     * provided, disables the input field.
     */
    static final String RES_DIALOG_COMMENT_PROMPT = "RES_DIALOG_COMMENT_PROMPT";
    /**
     * Bundle key for the Toast text triggered when the user accepts to send a
     * report in the crash dialog.
     */
    static final String RES_DIALOG_OK_TOAST = "RES_DIALOG_OK_TOAST";
    /**
     * Bundle key for the Toast text triggered when the application crashes if
     * the notification+dialog mode is not used.
     */
    static final String RES_TOAST_TEXT = "RES_TOAST_TEXT";

    /**
     * This is the identifier (value = 666) use for the status bar notification
     * issued when crashes occur.
     */
    static final int NOTIF_CRASH_ID = 666;

    /**
     * The key of the application default SharedPreference where you can put a
     * 'true' Boolean value to disable ACRA.
     */
    public static final String PREF_DISABLE_ACRA = "acra.disable";

    /**
     * Alternatively, you can use this key if you prefer your users to have the
     * checkbox ticked to enable crash reports. If both acra.disable and
     * acra.enable are set, the value of acra.disable takes over the other.
     */
    public static final String PREF_ENABLE_ACRA = "acra.enable";

    private static Application mApplication;
    private static ReportsCrashes mReportsCrashes;
    private static Bundle mCrashResources;

    /**
     * 
     * @param app
     */
    public static void init(Application app) {
        mApplication = app;
        mReportsCrashes = mApplication.getClass().getAnnotation(ReportsCrashes.class);
        if (mReportsCrashes != null) {

            SharedPreferences prefs = getACRASharedPreferences();
            prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (PREF_DISABLE_ACRA.equals(key) || PREF_ENABLE_ACRA.equals(key)) {
                        Boolean disableAcra = false;
                        try {
                            disableAcra = sharedPreferences.getBoolean(PREF_DISABLE_ACRA,
                                    !sharedPreferences.getBoolean(PREF_ENABLE_ACRA, true));
                        } catch (Exception e) {
                            // In case of a ClassCastException
                        }
                        if (disableAcra) {
                            ErrorReporter.getInstance().disable();
                        } else {
                            try {
                                initAcra();
                            } catch (ACRAConfigurationException e) {
                                Log.w(LOG_TAG, "Error : ", e);
                            }
                        }
                    }

                }
            });

            // If the application default shared preferences contains true for
            // the
            // key "acra.disable", do not activate ACRA. Also checks the
            // alternative
            // opposite setting "acra.enable" if "acra.disable" is not found.
            boolean disableAcra = false;
            try {
                disableAcra = prefs.getBoolean(PREF_DISABLE_ACRA, !prefs.getBoolean(PREF_ENABLE_ACRA, true));
            } catch (Exception e) {
                // In case of a ClassCastException
            }

            if (disableAcra) {
                Log.d(LOG_TAG, "ACRA is disabled for " + mApplication.getPackageName() + ".");
                return;
            } else {
                try {
                    initAcra();
                } catch (ACRAConfigurationException e) {
                    Log.w(LOG_TAG, "Error : ", e);
                }
            }

        }
    }

    /**
     * Activate ACRA.
     * 
     * @throws ACRAConfigurationException
     */
    private static void initAcra() throws ACRAConfigurationException {
        initCrashResources();
        Log.d(LOG_TAG, "ACRA is enabled for " + mApplication.getPackageName() + ", intializing...");
        // Initialize ErrorReporter with all required data
        ErrorReporter errorReporter = ErrorReporter.getInstance();
        errorReporter.setFormUri(getFormUri());
        errorReporter.setReportingInteractionMode(mReportsCrashes.mode());

        errorReporter.setCrashResources(getCrashResources());

        // Activate the ErrorReporter
        errorReporter.init(mApplication.getApplicationContext());

        // Check for pending reports

        errorReporter.checkReportsOnApplicationStart();
    }

    static void initCrashResources() throws ACRAConfigurationException {
        mCrashResources = new Bundle();
        switch (mReportsCrashes.mode()) {
        case TOAST:
            if (mReportsCrashes.resToastText() == 0) {
                throw new ACRAConfigurationException(
                        "TOAST mode: you have to define the resToastText parameter in your application @ReportsCrashes() annotation.");
            }
            mCrashResources.putInt(RES_TOAST_TEXT, mReportsCrashes.resToastText());
            break;
        case NOTIFICATION:
            if (mReportsCrashes.resNotifTickerText() != 0 && mReportsCrashes.resNotifTitle() != 0
                    && mReportsCrashes.resNotifText() != 0 && mReportsCrashes.resDialogText() != 0) {
                mCrashResources.putInt(RES_NOTIF_TICKER_TEXT, mReportsCrashes.resNotifTickerText());
                mCrashResources.putInt(RES_NOTIF_TITLE, mReportsCrashes.resNotifTitle());
                mCrashResources.putInt(RES_NOTIF_TEXT, mReportsCrashes.resNotifText());
                mCrashResources.putInt(RES_DIALOG_TEXT, mReportsCrashes.resDialogText());
                mCrashResources.putInt(RES_NOTIF_ICON, mReportsCrashes.resNotifIcon());
                mCrashResources.putInt(RES_DIALOG_ICON, mReportsCrashes.resDialogIcon());
                mCrashResources.putInt(RES_DIALOG_TITLE, mReportsCrashes.resDialogTitle());
                mCrashResources.putInt(RES_DIALOG_COMMENT_PROMPT, mReportsCrashes.resDialogCommentPrompt());
                mCrashResources.putInt(RES_DIALOG_OK_TOAST, mReportsCrashes.resDialogOkToast());
            } else {
                throw new ACRAConfigurationException(
                        "NOTIFICATION mode: you have to define at least the resNotifTickerText, resNotifTitle, resNotifText, resDialogText parameters in your application @ReportsCrashes() annotation.");
            }
            break;
        }
    }

    static Bundle getCrashResources() {
        return mCrashResources;
    }

    private static Uri getFormUri() {

        return mReportsCrashes.formUri().equals("") ? Uri.parse("http://spreadsheets.google.com/formResponse?formkey="
                + mReportsCrashes.formKey() + "&amp;ifq") : Uri.parse(mReportsCrashes.formUri());
    }

    /**
     * Override this method if you need to store "acra.disable" or "acra.enable"
     * in a different SharedPrefence than the application's default.
     * 
     * @return The Shared Preferences where ACRA will check the value of the
     *         setting which disables/enables it's action.
     */
    public static SharedPreferences getACRASharedPreferences() {
        if (!"".equals(mReportsCrashes.sharedPreferencesName())) {
            return mApplication.getSharedPreferences(mReportsCrashes.sharedPreferencesName(),
                    mReportsCrashes.sharedPreferencesMode());
        } else {
            return PreferenceManager.getDefaultSharedPreferences(mApplication);
        }
    }

}
