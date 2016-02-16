/*
 *  Copyright 2012 Kevin Gaudin
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
package org.acra.sender;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.file.CrashReportPersister;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Distributes reports to all Senders.
 *
 * @author William Ferguson
 * @since 4.8.0
 */
final class ReportDistributor {

    private final Context context;
    private final ACRAConfiguration config;
    private final List<ReportSender> reportSenders;

    /**
     * Creates a new {@link ReportDistributor} to try sending pending reports.
     *
     * @param context               ApplicationContext in which the reports are being sent.
     * @param config                Configuration to use while sending.
     * @param reportSenders         List of ReportSender to use to send the crash reports.
     */
    public ReportDistributor(Context context, ACRAConfiguration config, List<ReportSender> reportSenders) {
        this.context = context;
        this.config = config;
        this.reportSenders = reportSenders;
    }

    /**
     * Send report via all senders.
     *
     * @param reportFile    Report to send.
     */
    public void distribute(@NonNull File reportFile) {

        ACRA.log.i(LOG_TAG, "Sending report " + reportFile );
        try {
            final CrashReportPersister persister = new CrashReportPersister();
            final CrashReportData previousCrashReport = persister.load(reportFile);
            sendCrashReport(previousCrashReport);
            deleteFile(reportFile);
        } catch (RuntimeException e) {
            ACRA.log.e(LOG_TAG, "Failed to send crash reports for " + reportFile, e);
            deleteFile(reportFile);
        } catch (IOException e) {
            ACRA.log.e(LOG_TAG, "Failed to load crash report for " + reportFile, e);
            deleteFile(reportFile);
        } catch (ReportSenderException e) {
            ACRA.log.e(LOG_TAG, "Failed to send crash report for " + reportFile, e);
            // An issue occurred while sending this report but we can still try to
            // send other reports. Report sending is limited by ACRAConstants.MAX_SEND_REPORTS
            // so there's not much to fear about overloading a failing server.
        }
    }

    /**
     * Sends the report with all configured ReportSenders. If at least one
     * sender completed its job, the report is considered as sent and will not
     * be sent again for failing senders.
     *
     * @param errorContent  Crash data.
     * @throws ReportSenderException if unable to send the crash report.
     */
    private void sendCrashReport(CrashReportData errorContent) throws ReportSenderException {
        if (!isDebuggable() || config.sendReportsInDevMode()) {
            boolean sentAtLeastOnce = false;
            ReportSenderException sendFailure = null;
            String failedSender = null;
            for (ReportSender sender : reportSenders) {
                try {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Sending report using " + sender.getClass().getName());
                    sender.send(context, errorContent);
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Sent report using " + sender.getClass().getName());

                    // If at least one sender worked, don't re-send the report later.
                    sentAtLeastOnce = true;
                } catch (ReportSenderException e) {
                    sendFailure = e;
                    failedSender = sender.getClass().getName();
                }
            }

            if (sendFailure != null) {
                // We had some failure
                if (!sentAtLeastOnce) {
                    throw sendFailure; // Don't log here because we aren't dealing with the Exception here.
                } else {
                    ACRA.log.w(LOG_TAG,
                               "ReportSender of class "
                                   + failedSender
                                   + " failed but other senders completed their task. ACRA will not send this report again.");
                }
            }
        }
    }

    private void deleteFile(@NonNull File file) {
        final boolean deleted = file.delete();
        if (!deleted) {
            ACRA.log.w(LOG_TAG, "Could not delete error report : " + file);
        }
    }

    /**
     * Returns true if the application is debuggable.
     *
     * @return true if the application is debuggable.
     */
    private boolean isDebuggable() {
        PackageManager pm = context.getPackageManager();
        try {
            return ((pm.getApplicationInfo(context.getPackageName(), 0).flags & ApplicationInfo.FLAG_DEBUGGABLE) > 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
