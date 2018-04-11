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
package org.acra.builder;

import android.content.Context;
import android.os.Debug;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;
import org.acra.config.ReportingAdministrator;
import org.acra.data.CrashReportData;
import org.acra.data.CrashReportDataFactory;
import org.acra.file.CrashReportPersister;
import org.acra.file.ReportLocator;
import org.acra.interaction.ReportInteractionExecutor;
import org.acra.sender.SenderServiceStarter;
import org.acra.util.ProcessFinisher;
import org.acra.util.ToastSender;

import java.io.File;
import java.util.*;

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
    private final List<ReportingAdministrator> reportingAdministrators;

    // A reference to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the default exception handling after sending the report.
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private final ProcessFinisher processFinisher;

    private boolean enabled = false;

    /**
     * Creates a new instance
     *
     * @param context                 a context
     * @param config                  the config
     * @param crashReportDataFactory  factory used to collect data
     * @param defaultExceptionHandler pass-through handler
     * @param processFinisher         used to end process after reporting
     */
    public ReportExecutor(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull CrashReportDataFactory crashReportDataFactory,
                          @Nullable Thread.UncaughtExceptionHandler defaultExceptionHandler, @NonNull ProcessFinisher processFinisher) {
        this.context = context;
        this.config = config;
        this.crashReportDataFactory = crashReportDataFactory;
        this.defaultExceptionHandler = defaultExceptionHandler;
        this.processFinisher = processFinisher;
        reportingAdministrators = new ArrayList<>();
        //noinspection ForLoopReplaceableByForEach need to catch exception in iterator.next()
        for (final Iterator<ReportingAdministrator> iterator = ServiceLoader.load(ReportingAdministrator.class, getClass().getClassLoader()).iterator(); iterator.hasNext(); ) {
            try {
                final ReportingAdministrator reportingAdministrator = iterator.next();
                if (reportingAdministrator.enabled(config)) {
                    if (ACRA.DEV_LOGGING)
                        ACRA.log.d(ACRA.LOG_TAG, "Loaded ReportingAdministrator of class " + reportingAdministrator.getClass().getName());
                    reportingAdministrators.add(reportingAdministrator);
                }
            } catch (ServiceConfigurationError e) {
                ACRA.log.e(LOG_TAG, "Unable to load ReportingAdministrator", e);
            }
        }
    }

    /**
     * pass-through to default handler
     *
     * @param t the crashed thread
     * @param e the uncaught exception
     */
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
     * Try to create a report. Also starts {@link org.acra.sender.SenderService}
     *
     * @param reportBuilder The report builder used to assemble the report
     */
    public final void execute(@NonNull final ReportBuilder reportBuilder) {

        if (!enabled) {
            ACRA.log.v(LOG_TAG, "ACRA is disabled. Report not sent.");
            return;
        }

        ReportingAdministrator blockingAdministrator = null;
        for (ReportingAdministrator administrator : reportingAdministrators) {
            try {
                if (!administrator.shouldStartCollecting(context, config, reportBuilder)) {
                    blockingAdministrator = administrator;
                }
            } catch (Throwable t) {
                ACRA.log.w(LOG_TAG, "ReportingAdministrator " + administrator.getClass().getName() + " threw exception", t);
            }
        }
        final CrashReportData crashReportData;
        if (blockingAdministrator == null) {
            crashReportData = crashReportDataFactory.createCrashData(reportBuilder);
            for (ReportingAdministrator administrator : reportingAdministrators) {
                try {
                    if (!administrator.shouldSendReport(context, config, crashReportData)) {
                        blockingAdministrator = administrator;
                    }
                } catch (Throwable t) {
                    ACRA.log.w(LOG_TAG, "ReportingAdministrator " + administrator.getClass().getName() + " threw exception", t);
                }
            }
        } else {
            crashReportData = null;
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Not collecting crash report because of ReportingAdministrator " + blockingAdministrator.getClass().getName());
        }
        if (reportBuilder.isEndApplication()) {
            // Finish the last activity early to prevent restarts on android 7+
            processFinisher.finishLastActivity(reportBuilder.getUncaughtExceptionThread());
        }
        if (blockingAdministrator == null) {
            StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
            final File reportFile = getReportFileName(crashReportData);
            saveCrashReportFile(reportFile, crashReportData);

            final ReportInteractionExecutor executor = new ReportInteractionExecutor(context, config);
            StrictMode.setThreadPolicy(oldPolicy);
            if (reportBuilder.isSendSilently()) {
                //if size == 0 we have no interaction and can send all reports
                startSendingReports(executor.hasInteractions());
            } else {
                if (executor.performInteractions(reportFile)) {
                    startSendingReports(false);
                }
            }
        } else {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Not sending crash report because of ReportingAdministrator " + blockingAdministrator.getClass().getName());
            try {
                blockingAdministrator.notifyReportDropped(context, config);
            } catch (Throwable t) {
                ACRA.log.w(LOG_TAG, "ReportingAdministrator " + blockingAdministrator.getClass().getName() + " threw exeption", t);
            }
        }
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Wait for Interactions + worker ended. Kill Application ? " + reportBuilder.isEndApplication());

        if (reportBuilder.isEndApplication()) {
            boolean endApplication = true;
            for (ReportingAdministrator administrator : reportingAdministrators) {
                try {
                    if (!administrator.shouldKillApplication(context, config, reportBuilder, crashReportData)) {
                        endApplication = false;
                    }
                } catch (Throwable t) {
                    ACRA.log.w(LOG_TAG, "ReportingAdministrator " + administrator.getClass().getName() + " threw exception", t);
                }
            }
            if (endApplication) {
                if (Debug.isDebuggerConnected()) {
                    //Killing a process with a debugger attached would kill the whole application including our service, so we can't do that.
                    final String warning = "Warning: Acra may behave differently with a debugger attached";
                    new Thread(() -> {
                        Looper.prepare();
                        ToastSender.sendToast(context, warning, Toast.LENGTH_LONG);
                        Looper.loop();
                    }).start();
                    ACRA.log.w(LOG_TAG, warning);
                } else {
                    endApplication(reportBuilder.getUncaughtExceptionThread(), reportBuilder.getException());
                }
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
     * Starts a Process to start sending outstanding error reports.
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
        final String timestamp = crashData.getString(USER_CRASH_DATE);
        final String isSilent = crashData.getString(IS_SILENT);
        final String fileName = timestamp + (isSilent != null ? ACRAConstants.SILENT_SUFFIX : "") + ACRAConstants.REPORTFILE_EXTENSION;
        final ReportLocator reportLocator = new ReportLocator(context);
        return new File(reportLocator.getUnapprovedFolder(), fileName);
    }

    /**
     * Store a report
     *
     * @param file      the file to store in
     * @param crashData the content
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
