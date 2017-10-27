/*
 *  Copyright 2010 Emmanuel Astier &amp; Kevin Gaudin
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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.builder.LastActivityManager;
import org.acra.builder.ReportBuilder;
import org.acra.builder.ReportExecutor;
import org.acra.data.CrashReportDataFactory;
import org.acra.config.CoreConfiguration;
import org.acra.util.InstanceCreator;
import org.acra.util.ProcessFinisher;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

/**
 * <p>
 * The ErrorReporter is a Singleton object in charge of collecting crash context
 * data and sending crash reports. It registers itself as the Application's
 * Thread default {@link UncaughtExceptionHandler}.
 * </p>
 * <p>
 * When a crash occurs, it collects data of the crash context (device, system,
 * stack trace...) and writes a report file in the application private
 * directory. This report file is then sent:
 * </p>
 * <ul>
 * <li>immediately if org.acra.annotation.AcraCore#reportingInteractionMode() is set to
 * ReportingInteractionMode#SILENT or
 * ReportingInteractionMode#TOAST,</li>
 * <li>on application start if in the previous case the transmission could not
 * technically be made,</li>
 * <li>when the user accepts to send it if org.acra.annotation.AcraCore#reportingInteractionMode() is set
 * to ReportingInteractionMode#NOTIFICATION.</li>
 * </ul>
 * <p>
 * If an error occurs while sending a report, it is kept for later attempts.
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class ErrorReporter implements Thread.UncaughtExceptionHandler, SharedPreferences.OnSharedPreferenceChangeListener {

    private final boolean supportedAndroidVersion;

    private final Application context;

    @NonNull
    private final ReportExecutor reportExecutor;

    private final Map<String, String> customData = new HashMap<>();


    /**
     * Can only be constructed from within this class.
     *  @param context                     Context for the application in which ACRA is running.
     * @param config                      AcraConfig to use when reporting and sending errors.
     * @param enabled                     Whether this ErrorReporter should capture Exceptions and forward their reports.
     */
    ErrorReporter(@NonNull Application context, @NonNull CoreConfiguration config,
                  boolean enabled, boolean supportedAndroidVersion) {

        this.context = context;
        this.supportedAndroidVersion = supportedAndroidVersion;

        final CrashReportDataFactory crashReportDataFactory = new CrashReportDataFactory(context, config);
        crashReportDataFactory.collectStartUp();

        final Thread.UncaughtExceptionHandler defaultExceptionHandler;
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        final LastActivityManager lastActivityManager = new LastActivityManager(this.context);
        final InstanceCreator instanceCreator = new InstanceCreator();
        final ProcessFinisher processFinisher = new ProcessFinisher(context, config, lastActivityManager);

        reportExecutor = new ReportExecutor(context, config, crashReportDataFactory, defaultExceptionHandler, processFinisher);
        reportExecutor.setEnabled(enabled);
    }

    /**
     * <p>
     * Use this method to provide the ErrorReporter with data of your running
     * application. You should call this at several key places in your code the
     * same way as you would output important debug data in a log file. Only the
     * latest value is kept for each key (no history of the values is sent in
     * the report).
     * </p>
     *
     * @param key   A key for your custom data.
     * @param value The value associated to your key.
     * @return The previous value for this key if there was one, or null.
     * @see #removeCustomData(String)
     * @see #getCustomData(String)
     */
    public String putCustomData(@NonNull String key, String value) {
        return customData.put(key, value);
    }

    /**
     * Removes a key/value pair from your reports custom data field.
     *
     * @param key The key of the data to be removed.
     * @return The value for this key before removal.
     * @see #putCustomData(String, String)
     * @see #getCustomData(String)
     */
    public String removeCustomData(@NonNull String key) {
        return customData.remove(key);
    }

    /**
     * Removes all key/value pairs from your reports custom data field.
     */
    public void clearCustomData() {
        customData.clear();
    }

    /**
     * Gets the current value for a key in your reports custom data field.
     *
     * @param key The key of the data to be retrieved.
     * @return The value for this key.
     * @see #putCustomData(String, String)
     * @see #removeCustomData(String)
     */
    public String getCustomData(@NonNull String key) {
        return customData.get(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(@Nullable Thread t, @NonNull Throwable e) {

        // If we're not enabled then just pass the Exception on to the defaultExceptionHandler.
        if (!reportExecutor.isEnabled()) {
            reportExecutor.handReportToDefaultExceptionHandler(t, e);
            return;
        }

        try {
            ACRA.log.e(LOG_TAG, "ACRA caught a " + e.getClass().getSimpleName() + " for " + context.getPackageName(), e);
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Building report");

            // Generate and send crash report
            new ReportBuilder()
                    .uncaughtExceptionThread(t)
                    .exception(e)
                    .customData(customData)
                    .endApplication()
                    .build(reportExecutor);

        } catch (Throwable fatality) {
            // ACRA failed. Prevent any recursive call to ACRA.uncaughtException(), let the native reporter do its job.
            ACRA.log.e(LOG_TAG, "ACRA failed to capture the error - handing off to native error reporter", fatality);
            reportExecutor.handReportToDefaultExceptionHandler(t, e);
        }
    }

    /**
     * Mark this report as silent as send it.
     *
     * @param e The {@link Throwable} to be reported. If null the report will
     *          contain a new Exception("Report requested by developer").
     */
    public void handleSilentException(@Nullable Throwable e) {
        new ReportBuilder()
                .exception(e)
                .customData(customData)
                .sendSilently()
                .build(reportExecutor);
    }

    /**
     * Enable or disable this ErrorReporter. By default it is enabled.
     *
     * @param enabled Whether this ErrorReporter should capture Exceptions and
     *                forward them as crash reports.
     */
    public void setEnabled(boolean enabled) {
        if (supportedAndroidVersion) {
            ACRA.log.i(LOG_TAG, "ACRA is " + (enabled ? "enabled" : "disabled") + " for " + context.getPackageName());
            reportExecutor.setEnabled(enabled);
        } else {
            ACRA.log.w(LOG_TAG, "ACRA 4.7.0+ requires Froyo or greater. ACRA is disabled and will NOT catch crashes or send messages.");
        }
    }

    /**
     * Send a report for a {@link Throwable} with the reporting interaction mode
     * configured by the developer.
     *
     * @param e              The {@link Throwable} to be reported. If null the report will
     *                       contain a new Exception("Report requested by developer").
     * @param endApplication Set this to true if you want the application to be ended after
     *                       sending the report.
     */
    public void handleException(@Nullable Throwable e, boolean endApplication) {
        final ReportBuilder builder = new ReportBuilder();
        builder.exception(e)
                .customData(customData);
        if (endApplication) {
            builder.endApplication();
        }
        builder.build(reportExecutor);
    }

    /**
     * Send a report for a {@link Throwable} with the reporting interaction mode
     * configured by the developer, the application is then killed and restarted
     * by the system.
     *
     * @param e The {@link Throwable} to be reported. If null the report will
     *          contain a new Exception("Report requested by developer").
     */
    public void handleException(@Nullable Throwable e) {
        handleException(e, false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (ACRA.PREF_DISABLE_ACRA.equals(key) || ACRA.PREF_ENABLE_ACRA.equals(key)) {
            setEnabled(!ACRA.shouldDisableACRA(sharedPreferences));
        }
    }
}