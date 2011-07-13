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

import static org.acra.ReportField.*;

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
import android.text.format.Time;
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

    public static final boolean DEV_LOGGING = false; // Should be false for release.
    public static final String LOG_TAG = ACRA.class.getSimpleName();

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

    // NB don't convert to a local field because then it could be garbage collected and then we would have no PreferenceListener.
    private static OnSharedPreferenceChangeListener mPrefListener;
    private static Time mAppStartDate;

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
        mAppStartDate = new Time();
        mAppStartDate.setToNow();
        mApplication = app;
        mReportsCrashes = mApplication.getClass().getAnnotation(ReportsCrashes.class);
        if (mReportsCrashes != null) {

            final SharedPreferences prefs = getACRASharedPreferences();
            Log.d(ACRA.LOG_TAG, "Set OnSharedPreferenceChangeListener.");
            // We HAVE to keep a reference otherwise the listener could be
            // garbage collected:
            // http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently/3104265#3104265
            mPrefListener = new OnSharedPreferenceChangeListener() {

                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (PREF_DISABLE_ACRA.equals(key) || PREF_ENABLE_ACRA.equals(key)) {
                        boolean disableAcra = false;
                        try {
                            final boolean enableAcra = sharedPreferences.getBoolean(PREF_ENABLE_ACRA, true);
                            disableAcra = sharedPreferences.getBoolean(PREF_DISABLE_ACRA, !enableAcra);
                        } catch (Exception e) {
                            // In case of a ClassCastException
                        }

                        if (disableAcra) {
                            ErrorReporter.getInstance().disable();
                        } else {
                            try {
                                initAcra(); // TODO if PREF_DISABLE_ACRA or PREF_ENABLE_ACRA is changed the we might call #initAcra again which could be disastrous.
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
                final boolean enableAcra = prefs.getBoolean(PREF_ENABLE_ACRA, true);
                disableAcra = prefs.getBoolean(PREF_DISABLE_ACRA, !enableAcra);
            } catch (Exception e) {
                // In case of a ClassCastException
            }

            if (disableAcra) {
                Log.d(LOG_TAG, "ACRA is disabled for " + mApplication.getPackageName() + ".");
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
     * @throws ACRAConfigurationException if ACRA is not properly configured.
     */
    private static void initAcra() throws ACRAConfigurationException {
        checkCrashResources();
        Log.d(LOG_TAG, "ACRA is enabled for " + mApplication.getPackageName() + ", intializing...");

        // Initialize ErrorReporter with all required data
        final ErrorReporter errorReporter = ErrorReporter.getInstance();
        errorReporter.setReportingInteractionMode(mReportsCrashes.mode());
        errorReporter.setAppStartDate(mAppStartDate);

        if (!"".equals(mReportsCrashes.mailTo())) {
            Log.w(LOG_TAG, mApplication.getPackageName() + " reports will be sent by email (if accepted by user).");
            errorReporter.addReportSender(new EmailIntentSender(mApplication));
        } else {
            // Check for Internet permission, if not granted fallback to email report
            final PackageManager pm = mApplication.getPackageManager();
            if (pm != null) {
                if (pm.checkPermission(permission.INTERNET, mApplication.getPackageName()) == PackageManager.PERMISSION_GRANTED) {

                    // If formUri is set, instantiate a sender for a generic HTTP POST form
                    if (mReportsCrashes.formUri() != null && !"".equals(mReportsCrashes.formUri())) {
                        errorReporter.addReportSender(new HttpPostSender(mReportsCrashes.formUri(), null));
                    } else {
                        // The default behavior is to us the formKey for a Google Docs Form.
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

    /**
     * Default list of {@link ReportField}s to be sent in email reports {@see #mailTo()}. You can set your own list with
     * {@link org.acra.annotation.ReportsCrashes#customReportContent()}.
     */
    public final static ReportField[] DEFAULT_MAIL_REPORT_FIELDS = { ReportField.USER_COMMENT, ReportField.ANDROID_VERSION,
            ReportField.APP_VERSION_NAME, ReportField.BRAND, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA,
            ReportField.STACK_TRACE };

    /**
     * Default list of {@link ReportField}s to be sent in reports. You can set your own list with
     * {@link org.acra.annotation.ReportsCrashes#customReportContent()}.
     */
    public static final ReportField[] DEFAULT_REPORT_FIELDS = { REPORT_ID, APP_VERSION_CODE, APP_VERSION_NAME, PACKAGE_NAME,
    FILE_PATH, PHONE_MODEL, BRAND, PRODUCT, ANDROID_VERSION, BUILD, TOTAL_MEM_SIZE, AVAILABLE_MEM_SIZE,
    CUSTOM_DATA, IS_SILENT, STACK_TRACE, INITIAL_CONFIGURATION, CRASH_CONFIGURATION, DISPLAY, USER_COMMENT,
    USER_EMAIL, USER_APP_START_DATE, USER_CRASH_DATE, DUMPSYS_MEMINFO, DROPBOX, LOGCAT, EVENTSLOG, RADIOLOG,
 DEVICE_ID, INSTALLATION_ID, DEVICE_FEATURES, ENVIRONMENT, SHARED_PREFERENCES,
            SETTINGS_SYSTEM, SETTINGS_SECURE };

    /**
     * A special String value to allow the usage of a pseudo-null default value in annotation parameters.
     */
    public static final String NULL_VALUE = "ACRA-NULL-STRING";

}
