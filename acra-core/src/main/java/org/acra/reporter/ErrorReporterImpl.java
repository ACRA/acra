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
package org.acra.reporter;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.builder.LastActivityManager;
import org.acra.builder.ReportBuilder;
import org.acra.builder.ReportExecutor;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportDataFactory;
import org.acra.prefs.SharedPreferencesFactory;
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
 * directory, which may then be sent.
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class ErrorReporterImpl implements Thread.UncaughtExceptionHandler, SharedPreferences.OnSharedPreferenceChangeListener, ErrorReporter {

    private final boolean supportedAndroidVersion;
    private final Application context;
    private final ReportExecutor reportExecutor;
    private final Map<String, String> customData = new HashMap<>();


    /**
     * @param context                 Context for the application in which ACRA is running.
     * @param config                  AcraConfig to use when reporting and sending errors.
     * @param enabled                 Whether this ErrorReporter should capture Exceptions and forward their reports.
     * @param supportedAndroidVersion the minimal supported version
     */
    public ErrorReporterImpl(@NonNull Application context, @NonNull CoreConfiguration config,
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
     * {@inheritDoc}
     */
    @Override
    public String putCustomData(@NonNull String key, @Nullable String value) {
        return customData.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public String removeCustomData(@NonNull String key) {
        return customData.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCustomData() {
        customData.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
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
     * {@inheritDoc}
     */
    @Override
    public void handleSilentException(@Nullable Throwable e) {
        new ReportBuilder()
                .exception(e)
                .customData(customData)
                .sendSilently()
                .build(reportExecutor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (supportedAndroidVersion) {
            ACRA.log.i(LOG_TAG, "ACRA is " + (enabled ? "enabled" : "disabled") + " for " + context.getPackageName());
            reportExecutor.setEnabled(enabled);
        } else {
            ACRA.log.w(LOG_TAG, "ACRA 4.7.0+ requires Froyo or greater. ACRA is disabled and will NOT catch crashes or send messages.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public void handleException(@Nullable Throwable e) {
        handleException(e, false);
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, @Nullable String key) {
        if (ACRA.PREF_DISABLE_ACRA.equals(key) || ACRA.PREF_ENABLE_ACRA.equals(key)) {
            setEnabled(SharedPreferencesFactory.shouldEnableACRA(sharedPreferences));
        }
    }
}