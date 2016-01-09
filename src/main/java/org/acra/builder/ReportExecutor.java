package org.acra.builder;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportingInteractionMode;
import org.acra.collector.CrashReportData;
import org.acra.collector.CrashReportDataFactory;
import org.acra.common.CrashReportPersister;
import org.acra.common.SharedPreferencesFactory;
import org.acra.config.ACRAConfig;
import org.acra.dialog.CrashReportDialog;
import org.acra.sender.SenderServiceStarter;
import org.acra.util.ToastSender;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.IS_SILENT;

/**
 * Collates, records and initiates the sending of a report.
 *
 * @since 4.8.0
 */
public final class ReportExecutor {

    private final Context context;
    private final ACRAConfig config;
    private final CrashReportDataFactory crashReportDataFactory;
    private final LastActivityManager lastActivityManager;

    // A reference to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the default exception handling after sending the report.
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private final ReportPrimer reportPrimer;

    private boolean enabled = false;

    // This is used to wait for the crash toast to end it's display duration before killing the Application.
    // TODO make this a local variable. Only here because it cannot be non-final and referenced within an anonymous class.
    private boolean toastWaitEnded = true;

    /**
     * Used to create a new (non-cached) PendingIntent each time a new crash occurs.
     */
    private static int mNotificationCounter = 0;

    public ReportExecutor(Context context, ACRAConfig config, CrashReportDataFactory crashReportDataFactory, LastActivityManager lastActivityManager, Thread.UncaughtExceptionHandler defaultExceptionHandler, ReportPrimer reportPrimer) {
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

    public Thread.UncaughtExceptionHandler getDefaultExceptionHandler() {
        return defaultExceptionHandler;
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
    public void execute(final ReportBuilder reportBuilder) {

        if (!enabled) {
            ACRA.log.d(LOG_TAG, "ACRA is disabled. Report not sent.");
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

        final String reportFileName = getReportFileName(crashReportData);
        saveCrashReportFile(reportFileName, crashReportData);

        if (reportBuilder.isEndApplication() && !config.sendReportsAtShutdown()) {
            endApplication(reportBuilder.getUncaughtExceptionThread(), reportBuilder.getException());
        }

        final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
        if (reportingInteractionMode == ReportingInteractionMode.SILENT
                || reportingInteractionMode == ReportingInteractionMode.TOAST
                || prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {

            // Approve and then send reports now
            startSendingReports(sendOnlySilentReports, true);
            if ((reportingInteractionMode == ReportingInteractionMode.SILENT) && !reportBuilder.isEndApplication()) {
                // Report is being sent silently and the application is not ending.
                // So no need to wait around for the sender to complete.
                return;
            }

        } else if (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
            ACRA.log.d(LOG_TAG, "Creating Notification.");
            createNotification(reportFileName, reportBuilder);
        }

        // This is used to wait for the crash toast to end it's display duration before killing the Application.
        toastWaitEnded = true;

        if (shouldDisplayToast) {
            // A toast is being displayed, we have to wait for its end before doing anything else.
            // The toastWaitEnded flag will be checked before any other operation.
            toastWaitEnded = false;
            new Thread() {

                @Override
                public void run() {
                    ACRA.log.d(LOG_TAG, "Waiting for " + ACRAConstants.TOAST_WAIT_DURATION
                            + " millis from " + sentToastTimeMillis.initialTimeMillis
                            + " currentMillis=" + System.currentTimeMillis());
                    while (sentToastTimeMillis.getElapsedTime() < ACRAConstants.TOAST_WAIT_DURATION) {
                        try {
                            // Wait a bit to let the user read the toast
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                            ACRA.log.d(LOG_TAG, "Interrupted while waiting for Toast to end.", e1);
                        }
                    }
                    toastWaitEnded = true;
                }
            }.start();
        }

        final boolean showDirectDialog = (reportingInteractionMode == ReportingInteractionMode.DIALOG)
                && !prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false);

        new Thread() {

            @Override
            public void run() {
                // We have to wait for the toast display to be completed.
                ACRA.log.d(LOG_TAG, "Waiting for Toast");
                while (!toastWaitEnded) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        ACRA.log.d(LOG_TAG, "Error : ", e1);
                    }
                }
                ACRA.log.d(LOG_TAG, "Finished waiting for Toast");

                if (showDirectDialog) {
                    // Create a new activity task with the confirmation dialog.
                    // This new task will be persisted on application restart
                    // right after its death.
                    ACRA.log.d(LOG_TAG, "Creating CrashReportDialog for " + reportFileName);
                    final Intent dialogIntent = createCrashReportDialogIntent(reportFileName, reportBuilder);
                    dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(dialogIntent);
                }

                ACRA.log.d(LOG_TAG, "Wait for Toast + worker ended. Kill Application ? " + reportBuilder.isEndApplication());

                if (reportBuilder.isEndApplication()) {
                    endApplication(reportBuilder.getUncaughtExceptionThread(), reportBuilder.getException());
                }
            }
        }.start();
    }

    /**
     * End the application.
     */
    private void endApplication(Thread uncaughtExceptionThread, Throwable th) {
        // TODO It would be better to create an explicit config attribute #letDefaultHandlerEndApplication
        // as the intent is clearer and would allows you to switch it off for SILENT.
        final boolean letDefaultHandlerEndApplication = (
                config.mode() == ReportingInteractionMode.SILENT ||
                        (config.mode() == ReportingInteractionMode.TOAST && config.forceCloseDialogAfterToast())
        );

        final boolean handlingUncaughtException = uncaughtExceptionThread != null;
        if (handlingUncaughtException && letDefaultHandlerEndApplication && (defaultExceptionHandler != null)) {
            // Let the system default handler do it's job and display the force close dialog.
            ACRA.log.d(LOG_TAG, "Handing Exception on to default ExceptionHandler");
            defaultExceptionHandler.uncaughtException(uncaughtExceptionThread, th);
        } else {
            // If ACRA handles user notifications with a Toast or a Notification
            // the Force Close dialog is one more notification to the user...
            // We choose to close the process ourselves using the same actions.

            // Trying to solve https://github.com/ACRA/acra/issues/42#issuecomment-12134144
            // Determine the current/last Activity that was started and close
            // it. Activity#finish (and maybe it's parent too).
            final Activity lastActivity = lastActivityManager.getLastActivity();
            if (lastActivity != null) {
                ACRA.log.i(LOG_TAG, "Finishing the last Activity prior to killing the Process");
                lastActivity.finish();
                ACRA.log.i(LOG_TAG, "Finished " + lastActivity.getClass());
                lastActivityManager.clearLastActivity();
            }

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * Starts a Thread to start sending outstanding error reports.
     *
     * @param onlySendSilentReports If true then only send silent reports.
     * @param approveReportsFirst   If true then approve unapproved reports first.
     */
    private void startSendingReports(boolean onlySendSilentReports, boolean approveReportsFirst) {
        if (enabled) {
            final SenderServiceStarter starter = new SenderServiceStarter(context, config);
            starter.startService(onlySendSilentReports, approveReportsFirst);
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
     * @param reportFileName Name of the report file to send.
     */
    private void createNotification(String reportFileName, ReportBuilder reportBuilder) {

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Default notification icon is the warning symbol
        final int icon = config.resNotifIcon();

        final CharSequence tickerText = context.getText(config.resNotifTickerText());
        final long when = System.currentTimeMillis();

        ACRA.log.d(LOG_TAG, "Creating Notification for " + reportFileName);
        final Intent crashReportDialogIntent = createCrashReportDialogIntent(reportFileName, reportBuilder);
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

        notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;

        // The deleteIntent is invoked when the user swipes away the Notification.
        // In this case we invoke the CrashReportDialog with EXTRA_FORCE_CANCEL==true
        // which will cause BaseCrashReportDialog to clear the crash report and finish itself.
        final Intent deleteIntent = createCrashReportDialogIntent(reportFileName, reportBuilder);
        deleteIntent.putExtra(ACRAConstants.EXTRA_FORCE_CANCEL, true);
        notification.deleteIntent = PendingIntent.getActivity(context, -1, deleteIntent, 0);

        // Send new notification
        notificationManager.notify(ACRAConstants.NOTIF_CRASH_ID, notification);
    }

    private String getReportFileName(CrashReportData crashData) {
        final Calendar now = new GregorianCalendar();
        final long timestamp = now.getTimeInMillis();
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
            ACRA.log.d(LOG_TAG, "Writing crash report file " + fileName + ".");
            final CrashReportPersister persister = new CrashReportPersister(context);
            persister.store(crashData, fileName);
        } catch (Exception e) {
            ACRA.log.e(LOG_TAG, "An error occurred while writing the report file...", e);
        }
    }


    /**
     * Creates an Intent that can be used to create and show a CrashReportDialog.
     *
     * @param reportFileName    Name of the error report to display in the crash report dialog.
     * @param reportBuilder     ReportBuilder containing the details of the crash.
     */
    private Intent createCrashReportDialogIntent(String reportFileName, ReportBuilder reportBuilder) {
        ACRA.log.d(LOG_TAG, "Creating DialogIntent for " + reportFileName + " exception=" + reportBuilder.getException());
        final Intent dialogIntent = new Intent(context, config.reportDialogClass());
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_FILE_NAME, reportFileName);
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_EXCEPTION, reportBuilder.getException());
        dialogIntent.putExtra(ACRAConstants.EXTRA_REPORT_CONFIG, config);
        return dialogIntent;
    }
}
