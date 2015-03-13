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

import android.Manifest.permission;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.Compatibility;
import org.acra.collector.ConfigurationCollector;
import org.acra.collector.CrashReportData;
import org.acra.collector.CrashReportDataFactory;
import org.acra.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksCompat;
import org.acra.jraf.android.util.activitylifecyclecallbackscompat.ApplicationHelper;
import org.acra.sender.EmailIntentSender;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSender;
import org.acra.util.PackageManagerWrapper;
import org.acra.util.ToastSender;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.IS_SILENT;

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

    private boolean enabled = false;

    private final Application mContext;
    private final SharedPreferences prefs;

    /**
     * Contains the active {@link ReportSender}s.
     */
    private final List<ReportSender> mReportSenders = new ArrayList<ReportSender>();

    private final CrashReportDataFactory crashReportDataFactory;

    private final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();

    // A reference to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the default exception handling after sending the
    // report.
    private final Thread.UncaughtExceptionHandler mDfltExceptionHandler;

    private WeakReference<Activity> lastActivityCreated = new WeakReference<Activity>(null);

    /**
     * This is used to wait for the crash toast to end it's display duration
     * before killing the Application.
     */
    private static boolean toastWaitEnded = true;

    private static final ExceptionHandlerInitializer NULL_EXCEPTION_HANDLER_INITIALIZER = new ExceptionHandlerInitializer() {
        @Override
        public void initializeExceptionHandler(ErrorReporter reporter) {
        }
    };

    private volatile ExceptionHandlerInitializer exceptionHandlerInitializer = NULL_EXCEPTION_HANDLER_INITIALIZER;

    /**
     * Used to create a new (non-cached) PendingIntent each time a new crash occurs.
     */
    private static int mNotificationCounter = 0;

    /**
     * Can only be constructed from within this class.
     *
     * @param context
     *            Context for the application in which ACRA is running.
     * @param prefs
     *            SharedPreferences used by ACRA.
     * @param enabled
     *            Whether this ErrorReporter should capture Exceptions and
     *            forward their reports.
     */
    ErrorReporter(Application context, SharedPreferences prefs, boolean enabled) {

        this.mContext = context;
        this.prefs = prefs;
        this.enabled = enabled;

        // Store the initial Configuration state. This is expensive to gather, so only do so
        // if we plan to report it.
        final String initialConfiguration;
        if (CrashReportDataFactory.getReportFields().contains(ReportField.INITIAL_CONFIGURATION)) {
            initialConfiguration = ConfigurationCollector.collectConfiguration(mContext);
        } else {
            initialConfiguration = null;
        }

        // Sets the application start date.
        // This will be included in the reports, will be helpful compared to
        // user_crash date.
        final Time appStartDate = new Time();
        appStartDate.setToNow();

        if (Compatibility.getAPILevel() >= 14) { // ActivityLifecycleCallback
            // only available for API14+
            ApplicationHelper.registerActivityLifecycleCallbacks(context, new ActivityLifecycleCallbacksCompat() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.d(ACRA.LOG_TAG, "onActivityCreated " + activity.getClass());
                    if (!(activity instanceof BaseCrashReportDialog)) {
                        // Ignore CrashReportDialog because we want the last
                        // application Activity that was started so that we can
                        // explicitly kill it off.
                        lastActivityCreated = new WeakReference<Activity>(activity);
                    }
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.d(ACRA.LOG_TAG, "onActivityStarted " + activity.getClass());
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.d(ACRA.LOG_TAG, "onActivityResumed " + activity.getClass());
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.d(ACRA.LOG_TAG, "onActivityPaused " + activity.getClass());
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.d(ACRA.LOG_TAG, "onActivityStopped " + activity.getClass());
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.i(ACRA.LOG_TAG, "onActivitySaveInstanceState " + activity.getClass());
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.i(ACRA.LOG_TAG, "onActivityDestroyed " + activity.getClass());
                }
            });
        }

        crashReportDataFactory = new CrashReportDataFactory(mContext, prefs, appStartDate, initialConfiguration);

        // If mDfltExceptionHandler is not null, initialization is already done.
        // Don't do it twice to avoid losing the original handler.
        mDfltExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * @return the current instance of ErrorReporter.
     * @throws IllegalStateException
     *             if {@link ACRA#init(android.app.Application)} has not yet
     *             been called.
     * @deprecated since 4.3.0 Use {@link org.acra.ACRA#getErrorReporter()}
     *             instead.
     */
    @Deprecated
    public static ErrorReporter getInstance() {
        return ACRA.getErrorReporter();
    }

    /**
     * Deprecated. Use {@link #putCustomData(String, String)}.
     *
     * @param key
     *            A key for your custom data.
     * @param value
     *            The value associated to your key.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public void addCustomData(String key, String value) {
        crashReportDataFactory.putCustomData(key, value);
    }

    /**
     * <p>
     * Use this method to provide the ErrorReporter with data of your running
     * application. You should call this at several key places in your code the
     * same way as you would output important debug data in a log file. Only the
     * latest value is kept for each key (no history of the values is sent in
     * the report).
     * </p>
     * <p>
     * The key/value pairs will be stored in the GoogleDoc spreadsheet in the
     * "custom" column, as a text containing a 'key = value' pair on each line.
     * </p>
     *
     * @param key
     *            A key for your custom data.
     * @param value
     *            The value associated to your key.
     * @return The previous value for this key if there was one, or null.
     * @see #removeCustomData(String)
     * @see #getCustomData(String)
     */
    @SuppressWarnings("unused")
    public String putCustomData(String key, String value) {
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
     */
    public void setExceptionHandlerInitializer(ExceptionHandlerInitializer initializer) {
        exceptionHandlerInitializer = (initializer != null) ? initializer : NULL_EXCEPTION_HANDLER_INITIALIZER;
    }

    /**
     * Removes a key/value pair from your reports custom data field.
     *
     * @param key
     *            The key of the data to be removed.
     * @return The value for this key before removal.
     * @see #putCustomData(String, String)
     * @see #getCustomData(String)
     */
    @SuppressWarnings("unused")
    public String removeCustomData(String key) {
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
    public String getCustomData(String key) {
        return crashReportDataFactory.getCustomData(key);
    }

    /**
     * Add a {@link ReportSender} to the list of active {@link ReportSender}s.
     *
     * @param sender
     *            The {@link ReportSender} to be added.
     */
    public void addReportSender(ReportSender sender) {
        mReportSenders.add(sender);
    }

    /**
     * Remove a specific instance of {@link ReportSender} from the list of
     * active {@link ReportSender}s.
     *
     * @param sender
     *            The {@link ReportSender} instance to be removed.
     */
    @SuppressWarnings("unused")
    public void removeReportSender(ReportSender sender) {
        mReportSenders.remove(sender);
    }

    /**
     * Remove all {@link ReportSender} instances from a specific class.
     *
     * @param senderClass
     *            ReportSender class whose instances should be removed.
     */
    @SuppressWarnings("unused")
    public void removeReportSenders(Class<?> senderClass) {
        if (ReportSender.class.isAssignableFrom(senderClass)) {
            for (ReportSender sender : mReportSenders) {
                if (senderClass.isInstance(sender)) {
                    mReportSenders.remove(sender);
                }
            }
        }
    }

    /**
     * Clears the list of active {@link ReportSender}s. You should then call
     * {@link #addReportSender(ReportSender)} or ACRA will not send any report
     * anymore.
     */
    public void removeAllReportSenders() {
        mReportSenders.clear();
    }

    /**
     * Removes all previously set {@link ReportSender}s and set the given one as
     * the new {@link ReportSender}.
     *
     * @param sender
     *            ReportSender to set as the sole sender for this ErrorReporter.
     */
    public void setReportSender(ReportSender sender) {
        removeAllReportSenders();
        addReportSender(sender);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            // If we're not enabled then just pass the Exception on to any
            // defaultExceptionHandler.
            if (!enabled) {
                if (mDfltExceptionHandler != null) {
                    Log.e(ACRA.LOG_TAG, "ACRA is disabled for " + mContext.getPackageName()
                        + " - forwarding uncaught Exception on to default ExceptionHandler");
                    mDfltExceptionHandler.uncaughtException(t, e);
                } else {
                    Log.e(ACRA.LOG_TAG, "ACRA is disabled for " + mContext.getPackageName()
                        + " - no default ExceptionHandler");
                    Log.e(ACRA.LOG_TAG,
                          "ACRA caught a " + e.getClass().getSimpleName() + " for " + mContext.getPackageName(), e);
                }
                return;
            }

            Log.e(ACRA.LOG_TAG,
                  "ACRA caught a " + e.getClass().getSimpleName() + " for " + mContext.getPackageName(), e);
            Log.d(ACRA.LOG_TAG, "Building report");

            // Generate and send crash report
            reportBuilder()
                .uncaughtExceptionThread(t)
                .exception(e)
                .endsApplication()
                .send();
        } catch (Throwable fatality) {
            // ACRA failed. Prevent any recursive call to
            // ACRA.uncaughtException(), let the native reporter do its job.
            if (mDfltExceptionHandler != null) {
                mDfltExceptionHandler.uncaughtException(t, e);
            }
        }
    }

    /**
     * End the application.
     */
    private void endApplication(Thread uncaughtExceptionThread, Throwable th) {
        // TODO It would be better to create an explicit config attribute #letDefaultHandlerEndApplication
        // as the intent is clearer and would allows you to switch it off for SILENT.
        final boolean letDefaultHandlerEndApplication = (
             ACRA.getConfig().mode() == ReportingInteractionMode.SILENT ||
            (ACRA.getConfig().mode() == ReportingInteractionMode.TOAST && ACRA.getConfig().forceCloseDialogAfterToast())
        );

        final boolean handlingUncaughtException = uncaughtExceptionThread != null;
        if (handlingUncaughtException && letDefaultHandlerEndApplication && (mDfltExceptionHandler != null)) {
            // Let the system default handler do it's job and display the force close dialog.
            Log.d(ACRA.LOG_TAG, "Handing Exception on to default ExceptionHandler");
            mDfltExceptionHandler.uncaughtException(uncaughtExceptionThread, th);
        } else {
            // If ACRA handles user notifications with a Toast or a Notification
            // the Force Close dialog is one more notification to the user...
            // We choose to close the process ourselves using the same actions.
            Log.e(LOG_TAG, mContext.getPackageName() + " fatal error : " + th.getMessage(), th);

            // Trying to solve
            // https://github.com/ACRA/acra/issues/42#issuecomment-12134144
            // Determine the current/last Activity that was started and close
            // it. Activity#finish (and maybe it's parent too).
            final Activity lastActivity = lastActivityCreated.get();
            if (lastActivity != null) {
                Log.i(LOG_TAG, "Finishing the last Activity prior to killing the Process");
                lastActivity.finish();
                Log.i(LOG_TAG, "Finished " + lastActivity.getClass());
                lastActivityCreated.clear();
            }

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * Send a report for this {@link Throwable} silently (forces the use of
     * {@link ReportingInteractionMode#SILENT} for this report, whatever is the
     * mode set for the application. Very useful for tracking difficult defects.
     *
     * @param e
     *            The {@link Throwable} to be reported. If null the report will
     *            contain a new Exception("Report requested by developer").
     */
    public void handleSilentException(Throwable e) {
        // Mark this report as silent.
        if (enabled) {
            reportBuilder()
                .exception(e)
                .forceSilent()
                .send();
            Log.d(LOG_TAG, "ACRA sent Silent report.");
            return;
        }

        Log.d(LOG_TAG, "ACRA is disabled. Silent report not sent.");
    }

    /**
     * Enable or disable this ErrorReporter. By default it is enabled.
     *
     * @param enabled
     *            Whether this ErrorReporter should capture Exceptions and
     *            forward them as crash reports.
     */
    public void setEnabled(boolean enabled) {
        Log.i(ACRA.LOG_TAG, "ACRA is " + (enabled ? "enabled" : "disabled") + " for " + mContext.getPackageName());
        this.enabled = enabled;
    }

    /**
     * Starts a Thread to start sending outstanding error reports.
     *
     * @param onlySendSilentReports
     *            If true then only send silent reports.
     * @param approveReportsFirst
     *            If true then approve unapproved reports first.
     * @return SendWorker that will be sending the report.s
     */
    SendWorker startSendingReports(boolean onlySendSilentReports, boolean approveReportsFirst) {
        final SendWorker worker = new SendWorker(mContext, mReportSenders, onlySendSilentReports, approveReportsFirst);
        worker.start();
        return worker;
    }

    /**
     * Delete all report files stored.
     */
    void deletePendingReports() {
        deletePendingReports(true, true, 0);
    }

    /**
     * This method looks for pending reports and does the action required
     * depending on the interaction mode set.
     */
    public void checkReportsOnApplicationStart() {

        if (ACRA.getConfig().deleteOldUnsentReportsOnApplicationStart()) {
            // Delete any old unsent reports if this is a newer version of the app
            // than when we last started.
            final long lastVersionNr = prefs.getInt(ACRA.PREF_LAST_VERSION_NR, 0);
            final PackageManagerWrapper packageManagerWrapper = new PackageManagerWrapper(mContext);
            final PackageInfo packageInfo = packageManagerWrapper.getPackageInfo();
            if (packageInfo != null) {
                final boolean newVersion = packageInfo.versionCode > lastVersionNr;
                if (newVersion) {
                    deletePendingReports();
                }
                final SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putInt(ACRA.PREF_LAST_VERSION_NR, packageInfo.versionCode);
                prefsEditor.commit();
            }
        }

        ReportingInteractionMode reportingInteractionMode = ACRA.getConfig().mode();

        if ((reportingInteractionMode == ReportingInteractionMode.NOTIFICATION || reportingInteractionMode == ReportingInteractionMode.DIALOG)
            && ACRA.getConfig().deleteUnapprovedReportsOnApplicationStart()) {
            // NOTIFICATION or DIALOG mode, and there are unapproved reports to
            // send (latest notification/dialog has been ignored: neither
            // accepted
            // nor refused). The application developer has decided that
            // these reports should not be renotified ==> destroy them all but
            // one.
            deletePendingNonApprovedReports(true);
        }

        final CrashReportFinder reportFinder = new CrashReportFinder(mContext);
        String[] filesList = reportFinder.getCrashReportFiles();

        if (filesList != null && filesList.length > 0) {
            // Immediately send reports for SILENT and TOAST modes.
            // Immediately send reports in NOTIFICATION mode only if they are
            // all silent or approved.
            // If there is still one unapproved report in NOTIFICATION mode,
            // notify it.
            // If there are unapproved reports in DIALOG mode, show the dialog


            final boolean onlySilentOrApprovedReports = containsOnlySilentOrApprovedReports(filesList);

            if (reportingInteractionMode == ReportingInteractionMode.SILENT
                || reportingInteractionMode == ReportingInteractionMode.TOAST
                || (onlySilentOrApprovedReports && (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION || reportingInteractionMode == ReportingInteractionMode.DIALOG))) {

                if (reportingInteractionMode == ReportingInteractionMode.TOAST && !onlySilentOrApprovedReports) {
                    // Display the Toast in TOAST mode only if there are
                    // non-silent reports.
                    ToastSender.sendToast(mContext, ACRA.getConfig().resToastText(), Toast.LENGTH_LONG);
                }

                Log.v(ACRA.LOG_TAG, "About to start ReportSenderWorker from #checkReportOnApplicationStart");
                startSendingReports(false, false);
            }

        }
    }

    /**
     * Delete all pending non approved reports.
     *
     * @param keepOne
     *            If you need to keep the latest report, set this to true.
     */
    void deletePendingNonApprovedReports(boolean keepOne) {
        // In NOTIFICATION AND DIALOG mode, we have to keep the latest report
        // which
        // has been written before killing the app.
        final int nbReportsToKeep = keepOne ? 1 : 0;
        deletePendingReports(false, true, nbReportsToKeep);
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
    public void handleException(Throwable e, boolean endApplication) {
        final ReportBuilder builder = reportBuilder()
            .exception(e);
        if (endApplication) {
            builder.endsApplication();
        }
        builder.send();
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
    public void handleException(Throwable e) {
        reportBuilder()
            .exception(e)
            .send();
    }

    /**
     * Creates a new crash report builder
     *
     * @return the newly created {@code ReportBuilder}
     */
    public ReportBuilder reportBuilder() {
        return new ReportBuilder();
    }

    /**
     * Helps manage
     */
    private static class TimeHelper {

        private Long initialTimeMillis;

        public void setInitialTimeMillis(long initialTimeMillis) {
            this.initialTimeMillis = initialTimeMillis;
        }

        /**
         * @return 0 if the initial time has yet to be set otherwise returns the difference between now and the initial time.
         */
        public long getElapsedTime() {
            return (initialTimeMillis == null) ? 0 : System.currentTimeMillis() - initialTimeMillis;
        }
    }

    /**
     * Try to send a report, if an error occurs stores a report file for a later
     * attempt.
     *
     * @param reportBuilder The report builder used to assemble the report
     */
    private void report(final ReportBuilder reportBuilder) {

        if (!enabled) {
            return;
        }

        try {
            exceptionHandlerInitializer.initializeExceptionHandler(this);
        } catch (Exception exceptionInRunnable) {
            Log.d(ACRA.LOG_TAG, "Failed to initlize " + exceptionHandlerInitializer + " from #handleException");
        }

        boolean sendOnlySilentReports = false;
        ReportingInteractionMode reportingInteractionMode;
        if (!reportBuilder.mForceSilent) {
            // No interaction mode defined, we assume it has been set during
            // ACRA.initACRA()
            reportingInteractionMode = ACRA.getConfig().mode();
        } else {
            reportingInteractionMode = ReportingInteractionMode.SILENT;

            // An interaction mode has been provided. If ACRA has been
            // initialized with a non SILENT mode and this mode is overridden
            // with SILENT, then we have to send only reports which have been
            // explicitly declared as silent via handleSilentException().
            if (ACRA.getConfig().mode() != ReportingInteractionMode.SILENT) {
                sendOnlySilentReports = true;
            }
        }

        final boolean shouldDisplayToast = reportingInteractionMode == ReportingInteractionMode.TOAST
            || (ACRA.getConfig().resToastText() != 0 && (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION || reportingInteractionMode == ReportingInteractionMode.DIALOG));

        final TimeHelper sentToastTimeMillis = new TimeHelper();
        if (shouldDisplayToast) {
            new Thread() {

                /*
                 * (non-Javadoc)
                 *
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    Looper.prepare();
                    ToastSender.sendToast(mContext, ACRA.getConfig().resToastText(), Toast.LENGTH_LONG);
                    sentToastTimeMillis.setInitialTimeMillis(System.currentTimeMillis());
                    Looper.loop();
                }

            }.start();

            // We will wait a few seconds at the end of the method to be sure
            // that the Toast can be read by the user.
        }

        final CrashReportData crashReportData = crashReportDataFactory.createCrashData(reportBuilder.mMessage,
                                                                                       reportBuilder.mException, reportBuilder.mCustomData,
                                                                                       reportBuilder.mForceSilent, reportBuilder.mUncaughtExceptionThread);

        // Always write the report file

        final String reportFileName = getReportFileName(crashReportData);
        saveCrashReportFile(reportFileName, crashReportData);

        if (reportBuilder.mEndsApplication && !ACRA.getConfig().sendReportsAtShutdown()) {
            endApplication(reportBuilder.mUncaughtExceptionThread, reportBuilder.mException);
        }

        SendWorker sender = null;

        if (reportingInteractionMode == ReportingInteractionMode.SILENT
            || reportingInteractionMode == ReportingInteractionMode.TOAST
            || prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {

            // Approve and then send reports now
            Log.d(ACRA.LOG_TAG, "About to start ReportSenderWorker from #handleException");
            sender = startSendingReports(sendOnlySilentReports, true);
            if ((reportingInteractionMode == ReportingInteractionMode.SILENT) && !reportBuilder.mEndsApplication) {
                // Report is being sent silently and the application is not ending.
                // So no need to wait around for the sender to complete.
                return;
            }

        } else if (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
            Log.d(ACRA.LOG_TAG, "Creating Notification.");
            createNotification(reportFileName, reportBuilder);
        }

        if (shouldDisplayToast) {
            // A toast is being displayed, we have to wait for its end before
            // doing anything else.
            // The toastWaitEnded flag will be checked before any other
            // operation.
            toastWaitEnded = false;
            new Thread() {

                @Override
                public void run() {
                    Log.d(LOG_TAG, "Waiting for " + ACRAConstants.TOAST_WAIT_DURATION
                        + " millis from " + sentToastTimeMillis.initialTimeMillis
                        + " currentMillis=" + System.currentTimeMillis());
                    while (sentToastTimeMillis.getElapsedTime() < ACRAConstants.TOAST_WAIT_DURATION) {
                        try {
                            // Wait a bit to let the user read the toast
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                            Log.d(LOG_TAG, "Interrupted while waiting for Toast to end.", e1);
                        }
                    }
                    toastWaitEnded = true;
                }
            }.start();
        }

        // Start an AsyncTask waiting for the end of the sender.
        // Once sent, call endApplication() if reportBuilder.mEndApplication
        final SendWorker worker = sender;
        final boolean showDirectDialog = (reportingInteractionMode == ReportingInteractionMode.DIALOG)
            && !prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false);

        new Thread() {

            @Override
            public void run() {
                // We have to wait for BOTH the toast display wait AND
                // the worker job to be completed.
                if (toastWaitEnded || (worker == null)) {
                    // Nothing to wait for.
                    Log.d(LOG_TAG, "Toast (if any) and worker completed - not waiting");
                } else {
                    Log.d(LOG_TAG, "Waiting for " + (toastWaitEnded ? "Toast " : " -- ") + (worker.isAlive() ? "and Worker" : ""));
                    while (!toastWaitEnded || worker.isAlive()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                            Log.e(LOG_TAG, "Error : ", e1);
                        }
                    }
                    Log.d(LOG_TAG, "Finished waiting for Toast + Worker");
                }

                if (showDirectDialog) {
                    // Create a new activity task with the confirmation dialog.
                    // This new task will be persisted on application restart
                    // right after its death.
                    Log.d(LOG_TAG, "Creating CrashReportDialog for " + reportFileName);
                    final Intent dialogIntent = createCrashReportDialogIntent(reportFileName, reportBuilder);
                    dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(dialogIntent);
                }

                Log.d(LOG_TAG, "Wait for Toast + worker ended. Kill Application ? " + reportBuilder.mEndsApplication);

                if (reportBuilder.mEndsApplication) {
                    endApplication(reportBuilder.mUncaughtExceptionThread, reportBuilder.mException);
                }
            }
        }.start();
    }

    /**
     * Creates an Intent that can be used to create and show a CrashReportDialog.
     *
     * @param reportFileName    Name of the error report to display in the crash report dialog.
     * @param reportBuilder     ReportBuilder containing the details of the crash.
     */
    private Intent createCrashReportDialogIntent(String reportFileName, ReportBuilder reportBuilder) {
        Log.d(LOG_TAG, "Creating DialogIntent for " + reportFileName + " exception=" + reportBuilder.mException);
        final Intent dialogIntent = new Intent(mContext, ACRA.getConfig().reportDialogClass());
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_FILE_NAME, reportFileName);
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_EXCEPTION, reportBuilder.mException);
        return dialogIntent;
    }


    /**
     * Creates a status bar notification.
     *
     * The action triggered when the notification is selected is to start the
     * {@link CrashReportDialog} Activity.
     *
     * @param reportFileName Name of the report file to send.
     */
    private void createNotification(String reportFileName, ReportBuilder reportBuilder) {

        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        final ReportsCrashes conf = ACRA.getConfig();

        // Default notification icon is the warning symbol
        final int icon = conf.resNotifIcon();

        final CharSequence tickerText = mContext.getText(conf.resNotifTickerText());
        final long when = System.currentTimeMillis();
        final Notification notification = new Notification(icon, tickerText, when);

        final CharSequence contentTitle = mContext.getText(conf.resNotifTitle());
        final CharSequence contentText = mContext.getText(conf.resNotifText());

        Log.d(LOG_TAG, "Creating Notification for " + reportFileName);
        final Intent crashReportDialogIntent = createCrashReportDialogIntent(reportFileName, reportBuilder);
        final PendingIntent contentIntent = PendingIntent.getActivity(mContext, mNotificationCounter++, crashReportDialogIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
        notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;

        // The deleteIntent is invoked when the user swipes away the Notification.
        // In this case we invoke the CrashReportDialog with EXTRA_FORCE_CANCEL==true
        // which will cause BaseCrashReportDialog to clear the crash report and finish itself.
        final Intent deleteIntent = createCrashReportDialogIntent(reportFileName, reportBuilder);
        deleteIntent.putExtra(ACRAConstants.EXTRA_FORCE_CANCEL, true);
        notification.deleteIntent = PendingIntent.getActivity(mContext, -1, deleteIntent, 0);

        // Send new notification
        notificationManager.notify(ACRAConstants.NOTIF_CRASH_ID, notification);
    }

    private String getReportFileName(CrashReportData crashData) {
        final Time now = new Time();
        now.setToNow();
        final long timestamp = now.toMillis(false);
        final String isSilent = crashData.getProperty(IS_SILENT);
        return "" + timestamp + (isSilent != null ? ACRAConstants.SILENT_SUFFIX : "")
            + ACRAConstants.REPORTFILE_EXTENSION;
    }

    /**
     * When a report can't be sent, it is saved here in a file in the root of
     * the application private directory.
     *
     * @param fileName
     *            In a few rare cases, we write the report again with additional
     *            data (user comment for example). In such cases, you can
     *            provide the already existing file name here to overwrite the
     *            report file. If null, a new file report will be generated
     * @param crashData
     *            Can be used to save an alternative (or previously generated)
     *            report data. Used to store again a report with the addition of
     *            user comment. If null, the default current crash data are
     *            used.
     */
    private void saveCrashReportFile(String fileName, CrashReportData crashData) {
        try {
            Log.d(LOG_TAG, "Writing crash report file " + fileName + ".");
            final CrashReportPersister persister = new CrashReportPersister(mContext);
            persister.store(crashData, fileName);
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred while writing the report file...", e);
        }
    }

    /**
     * Delete pending reports.
     *
     * @param deleteApprovedReports
     *            Set to true to delete approved and silent reports.
     * @param deleteNonApprovedReports
     *            Set to true to delete non approved/silent reports.
     * @param nbOfLatestToKeep
     *            Number of pending reports to retain.
     */
    private void deletePendingReports(boolean deleteApprovedReports, boolean deleteNonApprovedReports,
                                      int nbOfLatestToKeep) {
        // TODO Check logic and instances where nbOfLatestToKeep = X, because
        // that might stop us from deleting any reports.
        final CrashReportFinder reportFinder = new CrashReportFinder(mContext);
        final String[] filesList = reportFinder.getCrashReportFiles();
        Arrays.sort(filesList);
        for (int iFile = 0; iFile < filesList.length - nbOfLatestToKeep; iFile++) {
            final String fileName = filesList[iFile];
            final boolean isReportApproved = fileNameParser.isApproved(fileName);
            if ((isReportApproved && deleteApprovedReports) || (!isReportApproved && deleteNonApprovedReports)) {
                final File fileToDelete = new File(mContext.getFilesDir(), fileName);
                ACRA.log.d(ACRA.LOG_TAG, "Deleting file " + fileName);
                if (!fileToDelete.delete()) {
                    Log.e(ACRA.LOG_TAG, "Could not delete report : " + fileToDelete);
                }
            }
        }
    }

    /**
     * Checks if an array of reports files names contains only silent or
     * approved reports.
     *
     * @param reportFileNames
     *            Array of report locations to check.
     * @return True if there are only silent or approved reports. False if there
     *         is at least one non-approved report.
     */
    private boolean containsOnlySilentOrApprovedReports(String[] reportFileNames) {
        for (String reportFileName : reportFileNames) {
            if (!fileNameParser.isApproved(reportFileName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets relevant ReportSenders to the ErrorReporter, replacing any
     * previously set ReportSender.
     */
    public void setDefaultReportSenders() {
        ReportsCrashes conf = ACRA.getConfig();
        Application mApplication = ACRA.getApplication();
        removeAllReportSenders();

        // Try to send by mail. If a mailTo address is provided, do not add
        // other senders.
        if (!"".equals(conf.mailTo())) {
            Log.w(LOG_TAG, mApplication.getPackageName() + " reports will be sent by email (if accepted by user).");
            setReportSender(new EmailIntentSender(mApplication));
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
        // with default mapping.
        if (conf.formUri() != null && !"".equals(conf.formUri())) {
            setReportSender(new HttpSender(ACRA.getConfig().httpMethod(), ACRA.getConfig().reportType(), null));
        }
    }

    /**
     * Fluent API used to assemble the different options used for a crash report
     */
    public final class ReportBuilder {

        private String mMessage;
        private Thread mUncaughtExceptionThread;
        private Throwable mException;
        private Map<String, String> mCustomData;

        private boolean mForceSilent = false;
        private boolean mEndsApplication = false;

        /**
         * Set the error message to be reported.
         *
         * @param msg the error message
         * @return the updated {@code ReportBuilder}
         */
        public ReportBuilder message(String msg) {
            mMessage = msg;
            return this;
        }

        /**
         * Sets the Thread on which an uncaught Exception occurred.
         *
         * @param thread    Thread on which an uncaught Exception occurred.
         * @return the updated {@code ReportBuilder}
         */
        private ReportBuilder uncaughtExceptionThread(Thread thread) {
            mUncaughtExceptionThread = thread;
            return this;
        }

        /**
         * Set the stack trace to be reported
         *
         * @param e The exception that should be associated with this report
         * @return the updated {@code ReportBuilder}
         */
        public ReportBuilder exception(Throwable e) {
            mException = e;
            return this;
        }

        private void initCustomData() {
            if (mCustomData ==  null)
                mCustomData = new HashMap<String, String>();
        }

        /**
         * Sets additional values to be added to {@code CUSTOM_DATA}. Values
         * specified here take precedence over globally specified custom data.
         *
         * @param customData a map of custom key-values to be attached to the report
         * @return the updated {@code ReportBuilder}
         */
        @SuppressWarnings("unused")
        public ReportBuilder customData(Map<String, String> customData) {
            initCustomData();
            mCustomData.putAll(customData);
            return this;
        }

        /**
         * Sets an additional value to be added to {@code CUSTOM_DATA}. The value
         * specified here takes precedence over globally specified custom data.
         *
         * @param key the key identifying the custom data
         * @param value the value for the custom data entry
         * @return the updated {@code ReportBuilder}
         */
        @SuppressWarnings("unused")
        public ReportBuilder customData(String key, String value) {
            initCustomData();
            mCustomData.put(key, value);
            return this;
        }

        /**
         * Forces the report to be sent silently, ignoring the default interaction mode set in the config
         *
         * @return the updated {@code ReportBuilder}
         */
        public ReportBuilder forceSilent() {
            mForceSilent = true;
            return this;
        }

        /**
         * Ends the application after sending the crash report
         *
         * @return the updated {@code ReportBuilder}
         */
        public ReportBuilder endsApplication() {
            mEndsApplication = true;
            return this;
        }

        /**
         * Assembles and sends the crash report
         */
        public void send() {
            if (mMessage == null && mException == null) {
                mMessage = "Report requested by developer";
            }
            report(this);
        }
    }
}