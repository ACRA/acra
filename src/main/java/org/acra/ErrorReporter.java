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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.annotation.ReportsCrashes;
import org.acra.builder.LastActivityManager;
import org.acra.builder.NoOpReportPrimer;
import org.acra.builder.ReportBuilder;
import org.acra.builder.ReportExecutor;
import org.acra.builder.ReportPrimer;
import org.acra.collector.ConfigurationCollector;
import org.acra.collector.CrashReportDataFactory;
import org.acra.config.ACRAConfiguration;
import org.acra.model.Element;
import org.acra.util.ApplicationStartupProcessor;
import org.acra.util.ProcessFinisher;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
 * <li>immediately if {@link ReportsCrashes#mode} is set to
 * {@link ReportingInteractionMode#SILENT} or
 * {@link ReportingInteractionMode#TOAST},</li>
 * <li>on application start if in the previous case the transmission could not
 * technically be made,</li>
 * <li>when the user accepts to send it if {@link ReportsCrashes#mode()} is set
 * to {@link ReportingInteractionMode#NOTIFICATION}.</li>
 * </ul>
 * <p>
 * If an error occurs while sending a report, it is kept for later attempts.
 * </p>
 */
public class ErrorReporter implements Thread.UncaughtExceptionHandler {

    private final boolean supportedAndroidVersion;

    private final Application context;
    @NonNull
    private final ACRAConfiguration config;

    @NonNull
    private final CrashReportDataFactory crashReportDataFactory;
    @NonNull
    private final ReportExecutor reportExecutor;

    @NonNull
    private volatile ExceptionHandlerInitializer exceptionHandlerInitializer = new ExceptionHandlerInitializer() {
        @Override
        public void initializeExceptionHandler(ErrorReporter reporter) {
        }
    };


    /**
     * Can only be constructed from within this class.
     *
     * @param context   Context for the application in which ACRA is running.
     * @param config    AcraConfig to use when reporting and sending errors.
     * @param prefs     SharedPreferences used by ACRA.
     * @param enabled   Whether this ErrorReporter should capture Exceptions and forward their reports.
     * @param listenForUncaughtExceptions   Whether to listen for uncaught Exceptions.
     */
    ErrorReporter(@NonNull Application context, @NonNull ACRAConfiguration config, @NonNull SharedPreferences prefs,
                  boolean enabled, boolean supportedAndroidVersion, boolean listenForUncaughtExceptions) {

        this.context = context;
        this.config = config;
        this.supportedAndroidVersion = supportedAndroidVersion;

        // Store the initial Configuration state.
        // This is expensive to gather, so only do so if we plan to report it.
        final Element initialConfiguration;
        if (config.getReportFields().contains(ReportField.INITIAL_CONFIGURATION)) {
            initialConfiguration = ConfigurationCollector.collectConfiguration(this.context);
        } else {
            initialConfiguration = ACRAConstants.NOT_AVAILABLE;
        }

        // Sets the application start date.
        // This will be included in the reports, will be helpful compared to user_crash date.
        final Calendar appStartDate = new GregorianCalendar();

        crashReportDataFactory = new CrashReportDataFactory(this.context, config, prefs, appStartDate, initialConfiguration);

        final Thread.UncaughtExceptionHandler defaultExceptionHandler;
        if (listenForUncaughtExceptions) {
            defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        } else {
            defaultExceptionHandler = null;
        }

        final LastActivityManager lastActivityManager = new LastActivityManager(this.context);
        final ReportPrimer reportPrimer = getReportPrimer(config);
        final ProcessFinisher processFinisher = new ProcessFinisher(context, config, lastActivityManager);

        reportExecutor = new ReportExecutor(context, config, crashReportDataFactory, defaultExceptionHandler, reportPrimer, processFinisher);
        reportExecutor.setEnabled(enabled);
    }

    /**
     * Deprecated. Use {@link #putCustomData(String, String)}.
     *
     * @param key   A key for your custom data.
     * @param value The value associated to your key.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public void addCustomData(@NonNull String key, String value) {
        putCustomData(key, value);
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
    @SuppressWarnings("unused")
    public String putCustomData(@NonNull String key, String value) {
        return crashReportDataFactory.putCustomData(key, value);
    }

    /**
     * <p>
     * Use this method to perform additional initialization before the
     * ErrorReporter handles a throwable. This can be used, for example, to put
     * custom data using {@link #putCustomData(String, String)}, which is not
     * available immediately after startup. It can be, for example, last 20
     * requests or something else. The call is thread safe.
     * </p>
     * <p>
     * {@link ExceptionHandlerInitializer#initializeExceptionHandler(ErrorReporter)}
     * will be executed on the main thread in case of uncaught exception and on
     * the caller thread of {@link #handleSilentException(Throwable)} or
     * {@link #handleException(Throwable)}.
     * </p>
     * <p>
     * Example. Add to the {@link Application#onCreate()}:
     * </p>
     *
     * <pre>
     * ACRA.getErrorReporter().setExceptionHandlerInitializer(new ExceptionHandlerInitializer() {
     *     <code>@Override</code> public void initializeExceptionHandler(ErrorReporter reporter) {
     *         reporter.putCustomData("CUSTOM_ACCUMULATED_DATA_TAG", someAccumulatedData.toString);
     *     }
     * });
     * </pre>
     *
     * @param initializer   The initializer. Can be <code>null</code>.
     * @deprecated since 4.8.0 use {@link ReportPrimer} mechanism instead.
     */
    public void setExceptionHandlerInitializer(@Nullable ExceptionHandlerInitializer initializer) {
        exceptionHandlerInitializer = (initializer != null)
                ? initializer
                : new ExceptionHandlerInitializer() {
                    @Override
                    public void initializeExceptionHandler(ErrorReporter reporter) {
                    }
                };
    }

    /**
     * Removes a key/value pair from your reports custom data field.
     *
     * @param key   The key of the data to be removed.
     * @return The value for this key before removal.
     * @see #putCustomData(String, String)
     * @see #getCustomData(String)
     */
    @SuppressWarnings("unused")
    public String removeCustomData(@NonNull String key) {
        return crashReportDataFactory.removeCustomData(key);
    }

    /**
     * Removes all key/value pairs from your reports custom data field.
     */
    @SuppressWarnings("unused")
    public void clearCustomData() {
        crashReportDataFactory.clearCustomData();
    }

    /**
     * Gets the current value for a key in your reports custom data field.
     *
     * @param key
     *            The key of the data to be retrieved.
     * @return The value for this key.
     * @see #putCustomData(String, String)
     * @see #removeCustomData(String)
     */
    @SuppressWarnings("unused")
    public String getCustomData(@NonNull String key) {
        return crashReportDataFactory.getCustomData(key);
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

            performDeprecatedReportPriming();

            // Generate and send crash report
            new ReportBuilder()
                .uncaughtExceptionThread(t)
                .exception(e)
                .endApplication()
                .build(reportExecutor);

        } catch (Throwable fatality) {
            // ACRA failed. Prevent any recursive call to ACRA.uncaughtException(), let the native reporter do its job.
            ACRA.log.e(LOG_TAG, "ACRA failed to capture the error - handing off to native error reporter" , fatality);
            reportExecutor.handReportToDefaultExceptionHandler(t, e);
        }
    }

    /**
     * Mark this report as silent as send it.
     *
     * @param e The {@link Throwable} to be reported. If null the report will
     *          contain a new Exception("Report requested by developer").
     */
    @SuppressWarnings("unused")
    public void handleSilentException(@Nullable Throwable e) {
        performDeprecatedReportPriming();
        new ReportBuilder()
                .exception(e)
                .sendSilently()
                .build(reportExecutor);
    }

    /**
     * Enable or disable this ErrorReporter. By default it is enabled.
     *
     * @param enabled
     *            Whether this ErrorReporter should capture Exceptions and
     *            forward them as crash reports.
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
     * This method looks for pending reports and does the action required depending on the interaction mode set.
     *
     * There is no need to call this method as ACRA will by default check for errors on report start.
     *
     * Whether ACRA checks for reports on app start is controlled by {@link ACRA#init(Application, ACRAConfiguration, boolean)},
     * but the default is that it will.
     *
     * @deprecated since 4.8.0 No replacement.
     */
    @SuppressWarnings( " unused" )
    public void checkReportsOnApplicationStart() {
        final ApplicationStartupProcessor startupProcessor = new ApplicationStartupProcessor(context,  config);
        if (config.deleteOldUnsentReportsOnApplicationStart()) {
            startupProcessor.deleteUnsentReportsFromOldAppVersion();
        }
        if (config.deleteUnapprovedReportsOnApplicationStart()) {
            startupProcessor.deleteAllUnapprovedReportsBarOne();
        }
        if (reportExecutor.isEnabled()) {
            startupProcessor.sendApprovedReports();
        }
    }

    /**
     * Send a report for a {@link Throwable} with the reporting interaction mode
     * configured by the developer.
     *
     * @param e
     *            The {@link Throwable} to be reported. If null the report will
     *            contain a new Exception("Report requested by developer").
     * @param endApplication
     *            Set this to true if you want the application to be ended after
     *            sending the report.
     */
    @SuppressWarnings("unused")
    public void handleException(@Nullable Throwable e, boolean endApplication) {
        performDeprecatedReportPriming();
        final ReportBuilder builder = new ReportBuilder();
        builder.exception(e);
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
     * @param e
     *            The {@link Throwable} to be reported. If null the report will
     *            contain a new Exception("Report requested by developer").
     */
    @SuppressWarnings("unused")
    public void handleException(@Nullable Throwable e) {
        handleException(e, false);
    }

    /**
     * This method is only here to support the deprecated {@link ExceptionHandlerInitializer} mechanism
     * for adding additional data to a crash report.
     */
    private void performDeprecatedReportPriming() {
        try {
            exceptionHandlerInitializer.initializeExceptionHandler(this);
        } catch (Exception exceptionInRunnable) {
            ACRA.log.w(LOG_TAG, "Failed to initialize " + exceptionHandlerInitializer + " from #handleException");
        }
    }

    @NonNull
    private static ReportPrimer getReportPrimer(@NonNull ACRAConfiguration config) {
        try {
            return config.reportPrimerClass().newInstance();
        } catch (InstantiationException e) {
            ACRA.log.w(LOG_TAG, "Could not construct ReportPrimer from " + config.reportPrimerClass() + " - not priming", e);
        } catch (IllegalAccessException e) {
            ACRA.log.w(LOG_TAG, "Could not construct ReportPrimer from " + config.reportPrimerClass() + " - not priming", e);
        }

        return new NoOpReportPrimer();
    }
}