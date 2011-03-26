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
import org.acra.sender.EmailIntentSender;
import org.acra.sender.GoogleFormSender;
import org.acra.sender.HttpPostSender;

import android.Manifest.permission;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
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
    public static final String LOG_TAG = ACRA.class.getSimpleName();

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

    /**
     * The key of the SharedPreference allowing the user to disable sending
     * content of logcat/dropbox. System logs collection is also dependent of
     * the READ_LOGS permission.
     */
    public static final String PREF_ENABLE_SYSTEM_LOGS = "acra.syslog.enable";

    /**
     * The key of the SharedPreference allowing the user to disable sending his
     * device id. Device ID collection is also dependent of the READ_PHONE_STATE
     * permission.
     */
    public static final String PREF_ENABLE_DEVICE_ID = "acra.deviceid.enable";

    /**
     * The key of the SharedPreference allowing the user to always include his
     * email address.
     */
    public static final String PREF_USER_EMAIL_ADDRESS = "acra.user.email";

    /**
     * The key of the SharedPreference allowing the user to automatically accept
     * sending reports.
     */
    public static final String PREF_ALWAYS_ACCEPT = "acra.alwaysaccept";

    private static Application mApplication;
    private static ReportsCrashes mReportsCrashes;
    private static OnSharedPreferenceChangeListener mPrefListener;

    /**
     * <p>
     * Initialize ACRA for a given Application. The call to this method should
     * be placed as soon as possible in the {@link Application#onCreate()}
     * method.
     * </p>
     * 
     * @param app
     *            Your Application class.
     */
    public static void init(Application app) {
        mApplication = app;
        mReportsCrashes = mApplication.getClass().getAnnotation(ReportsCrashes.class);
        if (mReportsCrashes != null) {

            SharedPreferences prefs = getACRASharedPreferences();
            Log.d(ACRA.LOG_TAG, "Set OnSharedPreferenceChangeListener.");
            // We HAVE to keep a reference otherwise the listener could be
            // garbage collected:
            // http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently/3104265#3104265
            mPrefListener = new OnSharedPreferenceChangeListener() {

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
            };

            // If the application default shared preferences contains true for
            // the key "acra.disable", do not activate ACRA. Also checks the
            // alternative opposite setting "acra.enable" if "acra.disable" is
            // not found.
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

            // This listener has to be set after initAcra is called to avoid a
            // NPE in ErrorReporter.disable() because
            // the context could be null at this moment.
            prefs.registerOnSharedPreferenceChangeListener(mPrefListener);
        }
    }

    /**
     * Activate ACRA.
     * 
     * @throws ACRAConfigurationException
     */
    private static void initAcra() throws ACRAConfigurationException {
        checkCrashResources();
        Log.d(LOG_TAG, "ACRA is enabled for " + mApplication.getPackageName() + ", intializing...");

        // Initialize ErrorReporter with all required data
        ErrorReporter errorReporter = ErrorReporter.getInstance();
        errorReporter.setReportingInteractionMode(mReportsCrashes.mode());

        if (!"".equals(mReportsCrashes.mailTo())) {
            Log.w(LOG_TAG, mApplication.getPackageName() + " reports will be sent by email (if accepted by user).");
            errorReporter.addReportSender(new EmailIntentSender(mApplication));
        } else {
            // Check for Internet permission, if not granted fallback to email
            // report
            PackageManager pm = mApplication.getPackageManager();
            if (pm != null) {
                if (pm.checkPermission(permission.INTERNET, mApplication.getPackageName()) == PackageManager.PERMISSION_GRANTED) {

                    // If formUri is set, instantiate a sender for a generic
                    // HTTP POST form
                    if (mReportsCrashes.formUri() != null && !"".equals(mReportsCrashes.formUri())) {
                        errorReporter.addReportSender(new HttpPostSender(mReportsCrashes.formUri(), null));
                    } else {
                        // The default behavior is to us the formKey for a
                        // Google Docs Form.
                        if (mReportsCrashes.formKey() != null && !"".equals(mReportsCrashes.formKey().trim())) {
                            errorReporter.addReportSender(new GoogleFormSender(mReportsCrashes.formKey()));
                        }
                    }
                } else {
                    Log.e(LOG_TAG,
                            mApplication.getPackageName()
                                    + " should be granted permission "
                                    + permission.INTERNET
                                    + " if you want your crash reports to be sent. If you don't want to add this permission to your application you can also enable sending reports by email. If this is your will then provide your email address in @ReportsCrashes(mailTo=\"your.account@domain.com\"");
                }
            }
        }

        // Activate the ErrorReporter
        errorReporter.init(mApplication.getApplicationContext());

        // Check for pending reports
        errorReporter.checkReportsOnApplicationStart();
    }

    static void checkCrashResources() throws ACRAConfigurationException {
        switch (mReportsCrashes.mode()) {
        case TOAST:
            if (mReportsCrashes.resToastText() == 0) {
                throw new ACRAConfigurationException(
                        "TOAST mode: you have to define the resToastText parameter in your application @ReportsCrashes() annotation.");
            }
            break;
        case NOTIFICATION:
            if (mReportsCrashes.resNotifTickerText() == 0 || mReportsCrashes.resNotifTitle() == 0
                    || mReportsCrashes.resNotifText() == 0 || mReportsCrashes.resDialogText() == 0) {
                throw new ACRAConfigurationException(
                        "NOTIFICATION mode: you have to define at least the resNotifTickerText, resNotifTitle, resNotifText, resDialogText parameters in your application @ReportsCrashes() annotation.");
            }
            break;
        }
    }

    /**
     * Retrieves the {@link SharedPreferences} instance where user adjustable
     * settings for ACRA are stored. Default are the Application default
     * SharedPreferences, but you can provide another SharedPreferences name
     * with {@link ReportsCrashes#sharedPreferencesName()}.
     * 
     * @return The Shared Preferences where ACRA will retrieve its user
     *         adjustable setting.
     */
    public static SharedPreferences getACRASharedPreferences() {
        if (!"".equals(mReportsCrashes.sharedPreferencesName())) {
            Log.d(ACRA.LOG_TAG, "Retrieve SharedPreferences " + mReportsCrashes.sharedPreferencesName());
            return mApplication.getSharedPreferences(mReportsCrashes.sharedPreferencesName(),
                    mReportsCrashes.sharedPreferencesMode());
        } else {
            Log.d(ACRA.LOG_TAG, "Retrieve application default SharedPreferences.");
            return PreferenceManager.getDefaultSharedPreferences(mApplication);
        }
    }

    /**
     * Provides the configuration annotation instance.
     * @return ACRA {@link ReportsCrashes} configuration instance.
     */
    public static ReportsCrashes getConfig() {
        return mReportsCrashes;
    }

}
