/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acra;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.config.ACRAConfigurationException;
import org.acra.config.CoreConfiguration;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.legacy.LegacyFileHandler;
import org.acra.log.ACRALog;
import org.acra.log.AndroidLogDelegate;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.reporter.ErrorReporterImpl;
import org.acra.util.ApplicationStartupProcessor;
import org.acra.util.StreamReader;
import org.acra.util.StubCreator;

import java.io.IOException;

/**
 * Use this class to initialize the crash reporting feature using
 * {@link #init(Application)} as soon as possible in your {@link Application}
 * subclass {@link Application#onCreate()} method. Configuration items must have
 * been set by using {@link org.acra.annotation.AcraCore} above the declaration of your
 * {@link Application} subclass.
 *
 * @author Kevin Gaudin
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Keep
public final class ACRA {
    private ACRA() {
    }

    public static /*non-final*/ boolean DEV_LOGGING = false; // Should be false for release.

    public static final String LOG_TAG = ACRA.class.getSimpleName();

    @NonNull
    public static ACRALog log = new AndroidLogDelegate();

    private static final String ACRA_PRIVATE_PROCESS_NAME = ":acra";

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

    @NonNull
    private static ErrorReporter errorReporterSingleton = StubCreator.createErrorReporterStub();

    /**
     * <p>
     * Initialize ACRA for a given Application.
     * <p>
     * The call to this method should be placed as soon as possible in the {@link Application#attachBaseContext(Context)} method.
     * <p>
     * Uses the configuration as configured with the @ReportCrashes annotation.
     * Sends any unsent reports.
     * </p>
     *
     * @param app Your Application class.
     * @throws IllegalStateException if it is called more than once.
     */
    public static void init(@NonNull Application app) {
        init(app, new CoreConfigurationBuilder(app));
    }

    /**
     * <p>
     * Initialize ACRA for a given Application.
     * <p>
     * The call to this method should be placed as soon as possible in the {@link Application#attachBaseContext(Context)} method.
     * <p>
     * Uses the configuration as configured with the @ReportCrashes annotation.
     * Sends any unsent reports.
     * </p>
     *
     * @param app     Your Application class.
     * @param builder ConfigurationBuilder to manually set up ACRA configuration.
     */
    public static void init(@NonNull Application app, @NonNull CoreConfigurationBuilder builder) {
        init(app, builder, true);
    }

    /**
     * <p>
     * Initialize ACRA for a given Application.
     * <p>
     * The call to this method should be placed as soon as possible in the {@link Application#attachBaseContext(Context)}  method.
     * </p>
     *
     * @param app                            Your Application class.
     * @param builder                        ConfigurationBuilder to manually set up ACRA configuration.
     * @param checkReportsOnApplicationStart Whether to invoke ErrorReporter.checkReportsOnApplicationStart().
     */
    public static void init(@NonNull Application app, @NonNull CoreConfigurationBuilder builder, boolean checkReportsOnApplicationStart) {
        try {
            init(app, builder.build(), checkReportsOnApplicationStart);
        } catch (ACRAConfigurationException e) {
            log.w(LOG_TAG, "Configuration Error - ACRA not started : " + e.getMessage());
        }
    }

    /**
     * <p>
     * Initialize ACRA for a given Application.
     * <p>
     * The call to this method should be placed as soon as possible in the {@link Application#attachBaseContext(Context)} method.
     * <p>
     * Sends any unsent reports.
     * </p>
     *
     * @param app    Your Application class.
     * @param config CoreConfiguration to manually set up ACRA configuration.
     * @throws IllegalStateException if it is called more than once.
     */
    public static void init(@NonNull Application app, @NonNull CoreConfiguration config) {
        init(app, config, true);
    }

    /**
     * <p>
     * Initialize ACRA for a given Application. The call to this method should
     * be placed as soon as possible in the {@link Application#attachBaseContext(Context)}
     * method.
     * </p>
     *
     * @param app                            Your Application class.
     * @param config                         CoreConfiguration to manually set up ACRA configuration.
     * @param checkReportsOnApplicationStart Whether to invoke ErrorReporter.checkReportsOnApplicationStart().
     * @throws IllegalStateException if it is called more than once.
     */
    public static void init(@NonNull Application app, @NonNull CoreConfiguration config, boolean checkReportsOnApplicationStart) {

        final boolean senderServiceProcess = isACRASenderServiceProcess();
        if (senderServiceProcess) {
            if (ACRA.DEV_LOGGING)
                log.d(LOG_TAG, "Not initialising ACRA to listen for uncaught Exceptions as this is the SendWorker process and we only send reports, we don't capture them to avoid infinite loops");
        }

        final boolean supportedAndroidVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        if (!supportedAndroidVersion) {
            // NB We keep initialising so that everything is configured. But ACRA is never enabled below.
            log.w(LOG_TAG, "ACRA 5.1.0+ requires ICS or greater. ACRA is disabled and will NOT catch crashes or send messages.");
        }

        if (isInitialised()) {
            log.w(LOG_TAG, "ACRA#init called more than once. Won't do anything more.");
            return;
        }

        //noinspection ConstantConditions
        if (config == null) {
            log.e(LOG_TAG, "ACRA#init called but no CoreConfiguration provided");
            return;
        }

        final SharedPreferences prefs = new SharedPreferencesFactory(app, config).create();

        new LegacyFileHandler(app, prefs).updateToCurrentVersionIfNecessary();
        if (!senderServiceProcess) {
            // Initialize ErrorReporter with all required data
            final boolean enableAcra = supportedAndroidVersion && SharedPreferencesFactory.shouldEnableACRA(prefs);
            // Indicate that ACRA is or is not listening for crashes.
            log.i(LOG_TAG, "ACRA is " + (enableAcra ? "enabled" : "disabled") + " for " + app.getPackageName() + ", initializing...");
            ErrorReporterImpl reporter = new ErrorReporterImpl(app, config, enableAcra, supportedAndroidVersion);
            errorReporterSingleton = reporter;

            // Check for approved reports and send them (if enabled).
            // NB don't check if senderServiceProcess as it will gather these reports itself.
            if (checkReportsOnApplicationStart) {
                new ApplicationStartupProcessor(app, config).checkReports(enableAcra);
            }

            // register after initAcra is called to avoid a
            // NPE in ErrorReporter.disable() because
            // the context could be null at this moment.
            prefs.registerOnSharedPreferenceChangeListener(reporter);
        }
    }

    /**
     * @return true is ACRA has been initialised.
     */
    @SuppressWarnings("unused")
    public static boolean isInitialised() {
        return errorReporterSingleton instanceof ErrorReporterImpl;
    }

    /**
     * @return true if the current process is the process running the SenderService.
     * NB this assumes that your SenderService is configured to used the default ':acra' process.
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
            return new StreamReader("/proc/self/cmdline").read().trim();
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
        return errorReporterSingleton;
    }


    public static void setLog(@NonNull ACRALog log) {
        //noinspection ConstantConditions (do not rely on annotation alone)
        if (log == null) {
            throw new NullPointerException("ACRALog cannot be null");
        }
        ACRA.log = log;
    }
}
