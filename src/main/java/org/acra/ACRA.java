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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;
import org.acra.legacy.LegacyFileHandler;
import org.acra.log.ACRALog;
import org.acra.log.AndroidLogDelegate;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.util.ApplicationStartupProcessor;
import org.acra.util.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;

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
@SuppressWarnings({"WeakerAccess","unused"})
public final class ACRA {
    private ACRA(){}

    public static /*non-final*/ boolean DEV_LOGGING = false; // Should be false for release.

    public static final String LOG_TAG = ACRA.class.getSimpleName();

    @NonNull
    public static ACRALog log = new AndroidLogDelegate();

    private static final String ACRA_PRIVATE_PROCESS_NAME= ":acra";

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

    /**
     * The version number of the application the last time ACRA was started.
     * This is used to determine whether unsent reports should be discarded
     * because they are old and out of date.
     */
    public static final String PREF_LAST_VERSION_NR = "acra.lastVersionNr";

    private static Application mApplication;
    @Nullable
    private static ACRAConfiguration configProxy;

    // Accessible via ACRA#getErrorReporter().
    @Nullable
    private static ErrorReporter errorReporterSingleton;

    // NB don't convert to a local field because then it could be garbage
    // collected and then we would have no PreferenceListener.
    private static OnSharedPreferenceChangeListener mPrefListener; // TODO consider moving to ErrorReport so it doesn't need to be a static field.

    /**
     * <p>
     * Initialize ACRA for a given Application.
     *
     * The call to this method should be placed as soon as possible in the {@link Application#attachBaseContext(Context)} method.
     *
     * Uses the configuration as configured with the @ReportCrashes annotation.
     * Sends any unsent reports.
     * </p>
     *
     * @param app   Your Application class.
     * @throws IllegalStateException if it is called more than once.
     */
    public static void init(@NonNull Application app) {
        final ReportsCrashes reportsCrashes = app.getClass().getAnnotation(ReportsCrashes.class);
        if (reportsCrashes == null) {
            log.e(LOG_TAG, "ACRA#init(Application) called but no ReportsCrashes annotation on Application " + app.getPackageName());
            return;
        }
        init(app, new ConfigurationBuilder(app));
    }

    /**
     * <p>
     * Initialize ACRA for a given Application.
     *
     * The call to this method should be placed as soon as possible in the {@link Application#attachBaseContext(Context)} method.
     *
     * Uses the configuration as configured with the @ReportCrashes annotation.
     * Sends any unsent reports.
     * </p>
     *
     * @param app     Your Application class.
     * @param builder ConfigurationBuilder to manually set up ACRA configuration.
     */
    public static void init(@NonNull Application app, @NonNull ConfigurationBuilder builder) {
        init(app, builder, true);
    }

    /**
     * <p>
     * Initialize ACRA for a given Application.
     *
     * The call to this method should be placed as soon as possible in the {@link Application#attachBaseContext(Context)}  method.
     * </p>
     *
     * @param app                            Your Application class.
     * @param builder                        ConfigurationBuilder to manually set up ACRA configuration.
     * @param checkReportsOnApplicationStart Whether to invoke ErrorReporter.checkReportsOnApplicationStart().
     */
    public static void init(@NonNull Application app, @NonNull ConfigurationBuilder builder, boolean checkReportsOnApplicationStart) {
        try {
            init(app, builder.build(), checkReportsOnApplicationStart);
        } catch (ACRAConfigurationException e) {
            log.w(LOG_TAG, "Configuration Error - ACRA not started : " + e.getMessage());
        }
    }

    /**
     * <p>
     * Initialize ACRA for a given Application.
     *
     * The call to this method should be placed as soon as possible in the {@link Application#attachBaseContext(Context)} method.
     *
     * Sends any unsent reports.
     * </p>
     *
     * @param app       Your Application class.
     * @param config    ACRAConfiguration to manually set up ACRA configuration.
     * @throws IllegalStateException if it is called more than once.
     */
    public static void init(@NonNull Application app, @NonNull ACRAConfiguration config) {
        init(app, config, true);
    }

    /**
     * <p>
     * Initialize ACRA for a given Application. The call to this method should
     * be placed as soon as possible in the {@link Application#attachBaseContext(Context)}
     * method.
     * </p>
     *
     * @param app       Your Application class.
     * @param config    ACRAConfiguration to manually set up ACRA configuration.
     * @param checkReportsOnApplicationStart    Whether to invoke ErrorReporter.checkReportsOnApplicationStart().
     * @throws IllegalStateException if it is called more than once.
     */
    public static void init(@NonNull Application app, @NonNull ACRAConfiguration config, boolean checkReportsOnApplicationStart){

        final boolean senderServiceProcess = isACRASenderServiceProcess();
        if (senderServiceProcess) {
            if (ACRA.DEV_LOGGING) log.d(LOG_TAG, "Not initialising ACRA to listen for uncaught Exceptions as this is the SendWorker process and we only send reports, we don't capture them to avoid infinite loops");
        }

        final boolean supportedAndroidVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
        if (!supportedAndroidVersion){
            // NB We keep initialising so that everything is configured. But ACRA is never enabled below.
            log.w(LOG_TAG, "ACRA 4.7.0+ requires Froyo or greater. ACRA is disabled and will NOT catch crashes or send messages.");
        }

        if (mApplication != null) {
            log.w(LOG_TAG, "ACRA#init called more than once. Won't do anything more.");
            return;
        }
        mApplication = app;

        //noinspection ConstantConditions
        if (config == null) {
            log.e(LOG_TAG, "ACRA#init called but no ACRAConfiguration provided");
            return;
        }
        configProxy = config;

        final SharedPreferences prefs = new SharedPreferencesFactory(mApplication, configProxy).create();

        new LegacyFileHandler(app, prefs).updateToCurrentVersionIfNecessary();

        // Initialize ErrorReporter with all required data
        final boolean enableAcra = supportedAndroidVersion && !shouldDisableACRA(prefs);
        if (!senderServiceProcess) {
            // Indicate that ACRA is or is not listening for crashes.
            log.i(LOG_TAG, "ACRA is " + (enableAcra ? "enabled" : "disabled") + " for " + mApplication.getPackageName() + ", initializing...");
        }
        errorReporterSingleton = new ErrorReporter(mApplication, configProxy, prefs, enableAcra, supportedAndroidVersion, !senderServiceProcess);

        // Check for approved reports and send them (if enabled).
        // NB don't check if senderServiceProcess as it will gather these reports itself.
        if (checkReportsOnApplicationStart && !senderServiceProcess) {
            final ApplicationStartupProcessor startupProcessor = new ApplicationStartupProcessor(mApplication,  config);
            if (config.deleteOldUnsentReportsOnApplicationStart()) {
                startupProcessor.deleteUnsentReportsFromOldAppVersion();
            }
            if (config.deleteUnapprovedReportsOnApplicationStart()) {
                startupProcessor.deleteAllUnapprovedReportsBarOne();
            }
            if (enableAcra) {
                startupProcessor.sendApprovedReports();
            }
        }

        // We HAVE to keep a reference otherwise the listener could be garbage
        // collected:
        // http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently/3104265#3104265
        mPrefListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, String key) {
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
     * @return true is ACRA has been initialised.
     */
    @SuppressWarnings("unused")
    public static boolean isInitialised() {
        return configProxy != null;
    }

    /**
     * @return true if the current process is the process running the SenderService.
     *          NB this assumes that your SenderService is configured to used the default ':acra' process.
     */
    public static boolean isACRASenderServiceProcess() {
        final String processName = getCurrentProcessName();
        if (ACRA.DEV_LOGGING) log.d(LOG_TAG, "ACRA processName='" + processName + '\'');
        //processName sometimes (or always?) starts with the package name, so we use endsWith instead of equals
        return processName != null && processName.endsWith(ACRA_PRIVATE_PROCESS_NAME);
    }

    @Nullable
    private static String getCurrentProcessName() {
        try {
            return IOUtils.streamToString(new FileInputStream("/proc/self/cmdline")).trim();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @return the current instance of ErrorReporter.
     * @throws IllegalStateException if {@link ACRA#init(android.app.Application)} has not yet been called.
     */
    @NonNull
    public static ErrorReporter getErrorReporter() {
        if (errorReporterSingleton == null) {
            throw new IllegalStateException("Cannot access ErrorReporter before ACRA#init");
        }
        return errorReporterSingleton;
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
    private static boolean shouldDisableACRA(@NonNull SharedPreferences prefs) {
        boolean disableAcra = false;
        try {
            final boolean enableAcra = prefs.getBoolean(PREF_ENABLE_ACRA, true);
            disableAcra = prefs.getBoolean(PREF_DISABLE_ACRA, !enableAcra);
        } catch (Exception e) {
            // In case of a ClassCastException
        }
        return disableAcra;
    }

    /**
     * @return The Shared Preferences where ACRA will retrieve its user adjustable setting.
     * @deprecated since 4.8.0 use {@link SharedPreferencesFactory} instead.
     */
    @SuppressWarnings( "unused" )
    @NonNull
    public static SharedPreferences getACRASharedPreferences() {
        if (configProxy == null) {
            throw new IllegalStateException("Cannot call ACRA.getACRASharedPreferences() before ACRA.init().");
        }
        return new SharedPreferencesFactory(mApplication, configProxy).create();
    }

    /**
     * Provides the current ACRA configuration.
     *
     * @return Current ACRA {@link ReportsCrashes} configuration instance.
     * @deprecated since 4.8.0 {@link ACRAConfiguration} should be passed into classes instead of retrieved statically.
     */
    @NonNull
    public static ACRAConfiguration getConfig() {
        if (configProxy == null) {
            throw new IllegalStateException("Cannot call ACRA.getConfig() before ACRA.init().");
        }
        return configProxy;
    }

    public static void setLog(@NonNull ACRALog log) {
        //noinspection ConstantConditions (do not rely on annotation alone)
        if (log == null) {
            throw new NullPointerException("ACRALog cannot be null");
        }
        ACRA.log = log;
    }
}
