package org.acra.builder;

import android.content.Context;
import android.os.Debug;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.collector.CrashReportData;
import org.acra.collector.CrashReportDataFactory;
import org.acra.config.CoreConfiguration;
import org.acra.file.CrashReportPersister;
import org.acra.file.ReportLocator;
import org.acra.interaction.ReportInteraction;
import org.acra.sender.SenderServiceStarter;
import org.acra.util.ProcessFinisher;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.IS_SILENT;
import static org.acra.ReportField.USER_CRASH_DATE;

/**
 * Collates, records and initiates the sending of a report.
 *
 * @since 4.8.0
 */
public class ReportExecutor {

    private final Context context;
    private final CoreConfiguration config;
    private final CrashReportDataFactory crashReportDataFactory;

    // A reference to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the default exception handling after sending the report.
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private final ReportPrimer reportPrimer;
    private final ProcessFinisher processFinisher;

    private boolean enabled = false;

    public ReportExecutor(@NonNull Context context, @NonNull CoreConfiguration config,
                          @NonNull CrashReportDataFactory crashReportDataFactory, @Nullable Thread.UncaughtExceptionHandler defaultExceptionHandler,
                          @NonNull ReportPrimer reportPrimer, @NonNull ProcessFinisher processFinisher) {
        this.context = context;
        this.config = config;
        this.crashReportDataFactory = crashReportDataFactory;
        this.defaultExceptionHandler = defaultExceptionHandler;
        this.reportPrimer = reportPrimer;
        this.processFinisher = processFinisher;
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
    public final void execute(@NonNull final ReportBuilder reportBuilder) {

        if (!enabled) {
            ACRA.log.v(LOG_TAG, "ACRA is disabled. Report not sent.");
            return;
        }

        // Prime this crash report with any extra data.
        reportPrimer.primeReport(context, config, reportBuilder);
        final CrashReportData crashReportData = crashReportDataFactory.createCrashData(reportBuilder);

        // Always write the report file

        final File reportFile = getReportFileName(crashReportData);
        saveCrashReportFile(reportFile, crashReportData);

        if (reportBuilder.isEndApplication()) {
            // Finish the last activity early to prevent restarts on android 7+
            processFinisher.finishLastActivity(reportBuilder.getUncaughtExceptionThread());
        }

        final List<ReportInteraction> reportInteractions = new ArrayList<>();
        for (ReportInteraction interaction : ServiceLoader.load(ReportInteraction.class)) {
            reportInteractions.add(interaction);
        }
        if (reportBuilder.isSendSilently()) {
            //if size == 0 we have no interaction and can send all reports
            startSendingReports(reportInteractions.size() != 0);
        } else {
            final ExecutorService executorService = Executors.newCachedThreadPool();
            final List<Future<Boolean>> futures = new ArrayList<>();
            for (final ReportInteraction reportInteraction : reportInteractions) {
                futures.add(executorService.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return !reportInteraction.performInteraction(context, config, reportBuilder, reportFile);
                    }
                }));
            }
            boolean sendReports = true;
            for (Future<Boolean> future : futures){
                while (!future.isDone()) {
                    try {
                        sendReports &= future.get();
                    } catch (InterruptedException ignored) {
                    } catch (ExecutionException e) {
                        //ReportInteraction crashed, so ignore it
                        break;
                    }
                }
            }
            if (sendReports) {
                startSendingReports(false);
            }
        }
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Wait for Interactions + worker ended. Kill Application ? " + reportBuilder.isEndApplication());

        if (reportBuilder.isEndApplication()) {
            if (Debug.isDebuggerConnected()) {
                //Killing a process with a debugger attached would kill the whole application including our service, so we can't do that.
                final String warning = "Warning: Acra may behave differently with a debugger attached";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(context, warning, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }).start();
                ACRA.log.w(LOG_TAG, warning);
            } else {
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
            processFinisher.endApplication();
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
     * @param file      In a few rare cases, we write the report again with additional
     *                  data (user comment for example). In such cases, you can
     *                  provide the already existing file name here to overwrite the
     *                  report file. If null, a new file report will be generated
     * @param crashData Can be used to save an alternative (or previously generated)
     *                  report data. Used to store again a report with the addition of
     *                  user comment. If null, the default current crash data are
     *                  used.
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
}
