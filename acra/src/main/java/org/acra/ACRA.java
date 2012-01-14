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

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.BUILD;
import static org.acra.ReportField.CRASH_CONFIGURATION;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.DEVICE_FEATURES;
import static org.acra.ReportField.DEVICE_ID;
import static org.acra.ReportField.DISPLAY;
import static org.acra.ReportField.DROPBOX;
import static org.acra.ReportField.DUMPSYS_MEMINFO;
import static org.acra.ReportField.ENVIRONMENT;
import static org.acra.ReportField.EVENTSLOG;
import static org.acra.ReportField.FILE_PATH;
import static org.acra.ReportField.INITIAL_CONFIGURATION;
import static org.acra.ReportField.INSTALLATION_ID;
import static org.acra.ReportField.IS_SILENT;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.RADIOLOG;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.SETTINGS_SECURE;
import static org.acra.ReportField.SETTINGS_SYSTEM;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_CRASH_DATE;
import static org.acra.ReportField.USER_EMAIL;

import org.acra.annotation.ReportsCrashes;
import org.acra.sender.EmailIntentSender;
import org.acra.sender.GoogleFormSender;
import org.acra.sender.HttpPostSender;
import org.acra.util.PackageManagerWrapper;

import android.Manifest.permission;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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

    public static final boolean DEV_LOGGING = false; // Should be false for
                                                     // release.
    public static final String LOG_TAG = ACRA.class.getSimpleName();

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

    // Accessible via ACRA#getErrorReporter().
    private static ErrorReporter errorReporterSingleton;

    // NB don't convert to a local field because then it could be garbage
    // collected and then we would have no PreferenceListener.
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
     * @throws IllegalStateException
     *             if it is called more than once.
     */
    public static void init(Application app) {

        if (mApplication != null) {
            throw new IllegalStateException("ACRA#init called more than once");
        }

        mApplication = app;
        mReportsCrashes = mApplication.getClass().getAnnotation(ReportsCrashes.class);
        if (mReportsCrashes == null) {
            Log.e(LOG_TAG,
                    "ACRA#init called but no ReportsCrashes annotation on Application " + mApplication.getPackageName());
            return;
        }

        final SharedPreferences prefs = getACRASharedPreferences();
        Log.d(ACRA.LOG_TAG, "Set OnSharedPreferenceChangeListener.");

        try {
            checkCrashResources();

            Log.d(LOG_TAG, "ACRA is enabled for " + mApplication.getPackageName() + ", intializing...");

            // Initialize ErrorReporter with all required data
            final boolean enableAcra = !shouldDisableACRA(prefs);
            final ErrorReporter errorReporter = new ErrorReporter(mApplication.getApplicationContext(), prefs,
                    enableAcra);

            // Append ReportSenders.
            addReportSenders(errorReporter);

            errorReporterSingleton = errorReporter;

        } catch (ACRAConfigurationException e) {
            Log.w(LOG_TAG, "Error : ", e);
        }

        // We HAVE to keep a reference otherwise the listener could be garbage
        // collected:
        // http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently/3104265#3104265
        mPrefListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (PREF_DISABLE_ACRA.equals(key) || PREF_ENABLE_ACRA.equals(key)) {
                    final boolean enableAcra = !shouldDisableACRA(sharedPreferences);
                    getErrorReporter().setEnabled(enableAcra);
                }
            }
        };

        // This listener has to be set after initAcra is called to avoid a
        // NPE in ErrorReporter.disable() because
        // the context could be null at this moment.
        prefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    /**
     * @return the current instance of ErrorReporter.
     * @throws IllegalStateException
     *             if {@link ACRA#init(android.app.Application)} has not yet
     *             been called.
     */
    public static ErrorReporter getErrorReporter() {
        if (errorReporterSingleton == null) {
            throw new IllegalStateException("Cannot access ErrorReporter before ACRA#init");
        }
        return errorReporterSingleton;
    }

    /**
     * Adds any relevant ReportSenders to the ErrorReporter.
     * 
     * @param errorReporter
     *            ErrorReporter to which to add appropriate ReportSenders.
     */
    private static void addReportSenders(ErrorReporter errorReporter) {
        ReportsCrashes conf = getConfig();

        // Try to send by mail.
        if (!"".equals(conf.mailTo())) {
            Log.w(LOG_TAG, mApplication.getPackageName() + " reports will be sent by email (if accepted by user).");
            errorReporter.addReportSender(new EmailIntentSender(mApplication));
            return;
        }

        final PackageManagerWrapper pm = new PackageManagerWrapper(mApplication);
        if (!pm.hasPermission(permission.INTERNET)) {
            // NB If the PackageManager has died then this will erroneously log
            // the error that the App doesn't have Internet (even though it
            // does).
            // I think that is a small price to pay to ensure that ACRA doesn't
            // crash if the PackageManager has died.
            Log.e(LOG_TAG,
                    mApplication.getPackageName()
                            + " should be granted permission "
                            + permission.INTERNET
                            + " if you want your crash reports to be sent. If you don't want to add this permission to your application you can also enable sending reports by email. If this is your will then provide your email address in @ReportsCrashes(mailTo=\"your.account@domain.com\"");
            return;
        }

        // If formUri is set, instantiate a sender for a generic HTTP POST form
        if (conf.formUri() != null && !"".equals(conf.formUri())) {
            errorReporter.addReportSender(new HttpPostSender(conf.formUri(), null));
            return;
        }

        // The default behavior is to use the formKey for a Google Docs Form.
        if (conf.formKey() != null && !"".equals(conf.formKey().trim())) {
            errorReporter.addReportSender(new GoogleFormSender(conf.formKey()));
        }
    }

    /**
     * Check if the application default shared preferences contains true for the
     * key "acra.disable", do not activate ACRA. Also checks the alternative
     * opposite setting "acra.enable" if "acra.disable" is not found.
     * 
     * @param prefs
     *            SharedPreferences to check to see whether ACRA should be
     *            disabled.
     * @return true if prefs indicate that ACRA should be disabled.
     */
    private static boolean shouldDisableACRA(SharedPreferences prefs) {
        boolean disableAcra = false;
        try {
            final boolean enableAcra = prefs.getBoolean(PREF_ENABLE_ACRA, true);
            disableAcra = prefs.getBoolean(PREF_DISABLE_ACRA, !enableAcra);
        } catch (Exception e) {
            // In case of a ClassCastException
        }
        return disableAcra;
    }

    private static void checkCrashResources() throws ACRAConfigurationException {
        ReportsCrashes conf = getConfig();
        switch (conf.mode()) {
        case TOAST:
            if (conf.resToastText() == 0) {
                throw new ACRAConfigurationException(
                        "TOAST mode: you have to define the resToastText parameter in your application @ReportsCrashes() annotation.");
            }
            break;
        case NOTIFICATION:
            if (conf.resNotifTickerText() == 0 || conf.resNotifTitle() == 0 || conf.resNotifText() == 0
                    || conf.resDialogText() == 0) {
                throw new ACRAConfigurationException(
                        "NOTIFICATION mode: you have to define at least the resNotifTickerText, resNotifTitle, resNotifText, resDialogText parameters in your application @ReportsCrashes() annotation.");
            }
            break;
        case DIALOG:
            if (conf.resDialogText() == 0) {
                throw new ACRAConfigurationException(
                        "DIALOG mode: you have to define at least the resDialogText parameters in your application @ReportsCrashes() annotation.");
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
        // TODO is there any reason to keep this method public? If we can hide
        // it, we should. Do clients ever need to access it?
        ReportsCrashes conf = getConfig();
        if (!"".equals(conf.sharedPreferencesName())) {
            Log.d(ACRA.LOG_TAG, "Retrieve SharedPreferences " + conf.sharedPreferencesName());
            return mApplication.getSharedPreferences(conf.sharedPreferencesName(), conf.sharedPreferencesMode());
        } else {
            Log.d(ACRA.LOG_TAG, "Retrieve application default SharedPreferences.");
            return PreferenceManager.getDefaultSharedPreferences(mApplication);
        }
    }

    /**
     * Provides the configuration annotation instance.
     * 
     * @return ACRA {@link ReportsCrashes} configuration instance.
     */
    public static ReportsCrashes getConfig() {
        if (configProxy == null) {
            configProxy = new ACRAConfiguration(mReportsCrashes);
        }
        return configProxy;
    }

    /**
     * Default list of {@link ReportField}s to be sent in email reports. You can
     * set your own list with
     * {@link org.acra.annotation.ReportsCrashes#customReportContent()}.
     * 
     * @see org.acra.annotation.ReportsCrashes#mailTo()
     */
    public final static ReportField[] DEFAULT_MAIL_REPORT_FIELDS = { ReportField.USER_COMMENT,
            ReportField.ANDROID_VERSION, ReportField.APP_VERSION_NAME, ReportField.BRAND, ReportField.PHONE_MODEL,
            ReportField.CUSTOM_DATA, ReportField.STACK_TRACE };

    /**
     * Default list of {@link ReportField}s to be sent in reports. You can set
     * your own list with
     * {@link org.acra.annotation.ReportsCrashes#customReportContent()}.
     */
    public static final ReportField[] DEFAULT_REPORT_FIELDS = { REPORT_ID, APP_VERSION_CODE, APP_VERSION_NAME,
            PACKAGE_NAME, FILE_PATH, PHONE_MODEL, BRAND, PRODUCT, ANDROID_VERSION, BUILD, TOTAL_MEM_SIZE,
            AVAILABLE_MEM_SIZE, CUSTOM_DATA, IS_SILENT, STACK_TRACE, INITIAL_CONFIGURATION, CRASH_CONFIGURATION,
            DISPLAY, USER_COMMENT, USER_EMAIL, USER_APP_START_DATE, USER_CRASH_DATE, DUMPSYS_MEMINFO, LOGCAT,
            DEVICE_ID, INSTALLATION_ID, DEVICE_FEATURES, ENVIRONMENT, SHARED_PREFERENCES, SETTINGS_SYSTEM,
            SETTINGS_SECURE };

    private static ReportsCrashes configProxy;

    /**
     * Returns true if the application is in debuggable.
     * @return true if the application is in debuggable.
     */
    static boolean isDebuggable() {
        PackageManager pm = mApplication.getPackageManager();
        try {
            return ((pm.getApplicationInfo(mApplication.getPackageName(), 0).flags & ApplicationInfo.FLAG_DEBUGGABLE) > 0);
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
