package org.acra.builder;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Debug;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportingInteractionMode;
import org.acra.collector.CrashReportData;
import org.acra.collector.CrashReportDataFactory;
import org.acra.config.ACRAConfiguration;
import org.acra.dialog.CrashReportDialog;
import org.acra.file.CrashReportPersister;
import org.acra.file.ReportLocator;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.sender.SenderServiceStarter;
import org.acra.util.ToastSender;

import java.io.File;
import java.util.Date;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.IS_SILENT;
import static org.acra.ReportField.USER_CRASH_DATE;

/**
 * Collates, records and initiates the sending of a report.
 *
 * @since 4.8.0
 */
public final class ReportExecutor {

    private static final int THREAD_SLEEP_INTERVAL_MILLIS = 100;

    private final Context context;
    private final ACRAConfiguration config;
    private final CrashReportDataFactory crashReportDataFactory;
    private final LastActivityManager lastActivityManager;

    // A reference to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the default exception handling after sending the report.
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private final ReportPrimer reportPrimer;

    private boolean enabled = false;

    /**
     * Used to create a new (non-cached) PendingIntent each time a new crash occurs.
     */
    private static int mNotificationCounter = 0;

    public ReportExecutor(@NonNull Context context,@NonNull ACRAConfiguration config,@NonNull CrashReportDataFactory crashReportDataFactory,
                          @NonNull LastActivityManager lastActivityManager,@Nullable Thread.UncaughtExceptionHandler defaultExceptionHandler,@NonNull ReportPrimer reportPrimer) {
        this.context = context;
        this.config = config;
        this.crashReportDataFactory = crashReportDataFactory;
        this.lastActivityManager = lastActivityManager;
        this.defaultExceptionHandler = defaultExceptionHandler;
        this.reportPrimer = reportPrimer;
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

    public void handReportToDefaultExceptionHandler(@Nullable Thread t, @NonNull Throwable e) {
        if (defaultExceptionHandler != null) {
            ACRA.log.i(LOG_TAG, "ACRA is disabled for " + context.getPackageName()
                    + " - forwarding uncaught Exception on to default ExceptionHandler");
            defaultExceptionHandler.uncaughtException(t, e);
        } else {
            ACRA.log.e(LOG_TAG, "ACRA is disabled for " + context.getPackageName() + " - no default ExceptionHandler");
            ACRA.log.e(LOG_TAG, "ACRA caught a " + e.getClass().getSimpleName() + " for " + context.getPackageName(), e);
        }

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Try to send a report, if an error occurs stores a report file for a later attempt.
     *
     * @param reportBuilder The report builder used to assemble the report
     */
    public void execute(@NonNull final ReportBuilder reportBuilder) {

        if (!enabled) {
            ACRA.log.v(LOG_TAG, "ACRA is disabled. Report not sent.");
            return;
        }

        // Prime this crash report with any extra data.
        reportPrimer.primeReport(context, reportBuilder);

        boolean sendOnlySilentReports = false;
        final ReportingInteractionMode reportingInteractionMode;
        if (!reportBuilder.isSendSilently()) {
            // No interaction mode defined in the ReportBuilder, we assume it has been set during ACRA.initACRA()
            reportingInteractionMode = config.mode();
        } else {
            reportingInteractionMode = ReportingInteractionMode.SILENT;

            // An interaction mode has been provided. If ACRA has been
            // initialized with a non SILENT mode and this mode is overridden
            // with SILENT, then we have to send only reports which have been
            // explicitly declared as silent via handleSilentException().
            if (config.mode() != ReportingInteractionMode.SILENT) {
                sendOnlySilentReports = true;
            }
        }

        final boolean shouldDisplayToast = reportingInteractionMode == ReportingInteractionMode.TOAST
                || (config.resToastText() != 0 && (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION || reportingInteractionMode == ReportingInteractionMode.DIALOG));

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
                    ToastSender.sendToast(context, config.resToastText(), Toast.LENGTH_LONG);
                    sentToastTimeMillis.setInitialTimeMillis(System.currentTimeMillis());
                    Looper.loop();
                }

            }.start();

            // We will wait a few seconds at the end of the method to be sure
            // that the Toast can be read by the user.
        }

        final CrashReportData crashReportData = crashReportDataFactory.createCrashData(reportBuilder);

        // Always write the report file

        final File reportFile = getReportFileName(crashReportData);
        saveCrashReportFile(reportFile, crashReportData);

        final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
        if (reportingInteractionMode == ReportingInteractionMode.SILENT
                || reportingInteractionMode == ReportingInteractionMode.TOAST
                || prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {

            // Approve and then send reports now
            startSendingReports(sendOnlySilentReports);
            if ((reportingInteractionMode == ReportingInteractionMode.SILENT) && !reportBuilder.isEndApplication()) {
                // Report is being sent silently and the application is not ending.
                // So no need to wait around for the sender to complete.
                return;
            }

        } else if (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Creating Notification.");
            createNotification(reportFile, reportBuilder);
        }

        final boolean showDirectDialog = (reportingInteractionMode == ReportingInteractionMode.DIALOG)
                && !prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false);

        if (shouldDisplayToast) {
            // A toast is being displayed, we have to wait for its end before doing anything else.
            new Thread() {

                @Override
                public void run() {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.d(LOG_TAG, "Waiting for " + ACRAConstants.TOAST_WAIT_DURATION
                                + " millis from " + sentToastTimeMillis.initialTimeMillis
                                + " currentMillis=" + System.currentTimeMillis());
                    final long sleep = ACRAConstants.TOAST_WAIT_DURATION - sentToastTimeMillis.getElapsedTime();
                    try {
                        // Wait a bit to let the user read the toast
                        if (sleep > 0L) Thread.sleep(sleep);
                    } catch (InterruptedException e1) {
                        if (ACRA.DEV_LOGGING)
                            ACRA.log.d(LOG_TAG, "Interrupted while waiting for Toast to end.", e1);
                    }
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finished waiting for Toast");
                    dialogAndEnd(reportBuilder, reportFile, showDirectDialog);
                }
            }.start();
        } else {
            dialogAndEnd(reportBuilder, reportFile, showDirectDialog);
        }
    }

    private void dialogAndEnd(@NonNull ReportBuilder reportBuilder, @NonNull File reportFile, boolean shouldShowDialog) {
        if (shouldShowDialog) {
            // Create a new activity task with the confirmation dialog.
            // This new task will be persisted on application restart
            // right after its death.
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Creating CrashReportDialog for " + reportFile);
            final Intent dialogIntent = createCrashReportDialogIntent(reportFile, reportBuilder);
            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dialogIntent);
        }

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Wait for Toast + worker ended. Kill Application ? " + reportBuilder.isEndApplication());

        if (reportBuilder.isEndApplication()) {
            if(Debug.isDebuggerConnected()){
                //Killing a process with a debugger attached would kill the whole application, so don't do that.
                final String warning = "Warning: Acra may behave differently with a debugger attached";
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(context, warning, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }.start();
                ACRA.log.w(LOG_TAG, warning);
                //do as much cleanup as we can without killing the process
                finishLastActivity(reportBuilder.getUncaughtExceptionThread());
            }else {
                endApplication(reportBuilder.getUncaughtExceptionThread(), reportBuilder.getException());
            }
        }
    }

    /**
     * End the application.
     */
    private void endApplication(@Nullable Thread uncaughtExceptionThread, Throwable th) {
        final boolean letDefaultHandlerEndApplication = config.alsoReportToAndroidFramework();

        final boolean handlingUncaughtException = uncaughtExceptionThread != null;
        if (handlingUncaughtException && letDefaultHandlerEndApplication && defaultExceptionHandler != null) {
            // Let the system default handler do it's job and display the force close dialog.
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Handing Exception on to default ExceptionHandler");
            defaultExceptionHandler.uncaughtException(uncaughtExceptionThread, th);
        } else {
            finishLastActivity(uncaughtExceptionThread);
            // If ACRA handles user notifications with a Toast or a Notification
            // the Force Close dialog is one more notification to the user...
            // We choose to close the process ourselves using the same actions.

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    private void finishLastActivity(Thread uncaughtExceptionThread){
        // Trying to solve https://github.com/ACRA/acra/issues/42#issuecomment-12134144
        // Determine the current/last Activity that was started and close
        // it. Activity#finish (and maybe it's parent too).
        final Activity lastActivity = lastActivityManager.getLastActivity();
        if (lastActivity != null) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finishing the last Activity prior to killing the Process");
            lastActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastActivity.finish();
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finished " + lastActivity.getClass());
                }
            });

            // A crashed activity won't continue its lifecycle. So we only wait if something else crashed
            if (uncaughtExceptionThread != lastActivity.getMainLooper().getThread()) {
                lastActivityManager.waitForActivityStop(100);
            }
            lastActivityManager.clearLastActivity();
        }
    }

    /**
     * Starts a Thread to start sending outstanding error reports.
     *
     * @param onlySendSilentReports If true then only send silent reports.
     */
    private void startSendingReports(boolean onlySendSilentReports) {
        if (enabled) {
            final SenderServiceStarter starter = new SenderServiceStarter(context, config);
            starter.startService(onlySendSilentReports, true);
        } else {
            ACRA.log.w(LOG_TAG, "Would be sending reports, but ACRA is disabled");
        }
    }

    /**
     * Creates a status bar notification.
     *
     * The action triggered when the notification is selected is to start the
     * {@link CrashReportDialog} Activity.
     *
     * @param reportFile    Report file to send.
     */
    private void createNotification(@NonNull File reportFile, @NonNull ReportBuilder reportBuilder) {

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Default notification icon is the warning symbol
        final int icon = config.resNotifIcon();

        final CharSequence tickerText = context.getText(config.resNotifTickerText());
        final long when = System.currentTimeMillis();

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Creating Notification for " + reportFile);
        final Intent crashReportDialogIntent = createCrashReportDialogIntent(reportFile, reportBuilder);
        final PendingIntent contentIntent = PendingIntent.getActivity(context, mNotificationCounter++, crashReportDialogIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final CharSequence contentTitle = context.getText(config.resNotifTitle());
        final CharSequence contentText = context.getText(config.resNotifText());

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        final Notification notification = builder
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setWhen(when)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // The deleteIntent is invoked when the user swipes away the Notification.
        // In this case we invoke the CrashReportDialog with EXTRA_FORCE_CANCEL==true
        // which will cause BaseCrashReportDialog to clear the crash report and finish itself.
        final Intent deleteIntent = createCrashReportDialogIntent(reportFile, reportBuilder);
        deleteIntent.putExtra(ACRAConstants.EXTRA_FORCE_CANCEL, true);
        notification.deleteIntent = PendingIntent.getActivity(context, -1, deleteIntent, 0);

        // Send new notification
        notificationManager.notify(ACRAConstants.NOTIF_CRASH_ID, notification);
    }

    @NonNull
    private File getReportFileName(@NonNull CrashReportData crashData) {
        final String timestamp = crashData.getProperty(USER_CRASH_DATE);
        final String isSilent = crashData.getProperty(IS_SILENT);
        final String fileName = (timestamp != null ? timestamp : new Date().getTime()) // Need to check for null because old version of ACRA did not always capture USER_CRASH_DATE
                + (isSilent != null ? ACRAConstants.SILENT_SUFFIX : "")
                + ACRAConstants.REPORTFILE_EXTENSION;
        final ReportLocator reportLocator = new ReportLocator(context);
        return new File(reportLocator.getUnapprovedFolder(), fileName);
    }

    /**
     * When a report can't be sent, it is saved here in a file in the root of
     * the application private directory.
     *
     * @param file
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
    private void saveCrashReportFile(@NonNull File file, @NonNull CrashReportData crashData) {
        try {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Writing crash report file " + file);
            final CrashReportPersister persister = new CrashReportPersister();
            persister.store(crashData, file);
        } catch (Exception e) {
            ACRA.log.e(LOG_TAG, "An error occurred while writing the report file...", e);
        }
    }


    /**
     * Creates an Intent that can be used to create and show a CrashReportDialog.
     *
     * @param reportFile        Error report file to display in the crash report dialog.
     * @param reportBuilder     ReportBuilder containing the details of the crash.
     */
    @NonNull
    private Intent createCrashReportDialogIntent(@NonNull File reportFile, @NonNull ReportBuilder reportBuilder) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Creating DialogIntent for " + reportFile + " exception=" + reportBuilder.getException());
        final Intent dialogIntent = new Intent(context, config.reportDialogClass());
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_FILE, reportFile);
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_EXCEPTION, reportBuilder.getException());
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_CONFIG, config);
        return dialogIntent;
    }
}
