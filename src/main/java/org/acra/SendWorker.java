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
package org.acra;

import static org.acra.ACRA.LOG_TAG;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.content.Context;
import android.util.Log;

/**
 * Checks and send reports on a separate Thread.
 * 
 * @author Kevin Gaudin
 */
final class SendWorker extends Thread {

    private final Context context;
    private final boolean sendOnlySilentReports;
    private final boolean approvePendingReports;
    private final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();
    private final List<ReportSender> reportSenders;

    /**
     * Creates a new {@link SendWorker} to try sending pending reports.
     * 
     * @param context
     *            ApplicationContext in which the reports are being sent.
     * @param reportSenders
     *            List of ReportSender to use to send the crash reports.
     * @param sendOnlySilentReports
     *            If set to true, will send only reports which have been
     *            explicitly declared as silent by the application developer.
     * @param approvePendingReports
     *            if this endWorker should approve pending reports before
     *            sending any reports.
     */
    public SendWorker(Context context, List<ReportSender> reportSenders, boolean sendOnlySilentReports,
            boolean approvePendingReports) {
        this.context = context;
        this.reportSenders = reportSenders;
        this.sendOnlySilentReports = sendOnlySilentReports;
        this.approvePendingReports = approvePendingReports;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        if (approvePendingReports) {
            approvePendingReports();
        }
        checkAndSendReports(context, sendOnlySilentReports);
    }

    /**
     * Flag all pending reports as "approved" by the user. These reports can be
     * sent.
     */
    private void approvePendingReports() {
        Log.d(LOG_TAG, "Mark all pending reports as approved.");

        final CrashReportFinder reportFinder = new CrashReportFinder(context);
        final String[] reportFileNames = reportFinder.getCrashReportFiles();

        for (String reportFileName : reportFileNames) {
            if (!fileNameParser.isApproved(reportFileName)) {
                final File reportFile = new File(context.getFilesDir(), reportFileName);

                // TODO look into how this could cause a file to go from
                // -approved.stacktrace to -approved-approved.stacktrace
                final String newName = reportFileName.replace(ACRAConstants.REPORTFILE_EXTENSION,
                        ACRAConstants.APPROVED_SUFFIX + ACRAConstants.REPORTFILE_EXTENSION);

                // TODO Look into whether rename is atomic. Is there a better
                // option?
                final File newFile = new File(context.getFilesDir(), newName);
                if (!reportFile.renameTo(newFile)) {
                    Log.e(LOG_TAG, "Could not rename approved report from " + reportFile + " to " + newFile);
                }
            }
        }
    }

    /**
     * Send pending reports.
     * 
     * @param context
     *            The application context.
     * @param sendOnlySilentReports
     *            Send only reports explicitly declared as SILENT by the
     *            developer (sent via
     *            {@link ErrorReporter#handleSilentException(Throwable)}.
     */
    private void checkAndSendReports(Context context, boolean sendOnlySilentReports) {
        Log.d(LOG_TAG, "#checkAndSendReports - start");
        final CrashReportFinder reportFinder = new CrashReportFinder(context);
        final String[] reportFiles = reportFinder.getCrashReportFiles();
        Arrays.sort(reportFiles);

        int reportsSentCount = 0;

        for (String curFileName : reportFiles) {
            if (sendOnlySilentReports && !fileNameParser.isSilent(curFileName)) {
                continue;
            }

            if (reportsSentCount >= ACRAConstants.MAX_SEND_REPORTS) {
                break; // send only a few reports to avoid overloading the
                       // network
            }

            Log.i(LOG_TAG, "Sending file " + curFileName);
            try {
                final CrashReportPersister persister = new CrashReportPersister(context);
                final CrashReportData previousCrashReport = persister.load(curFileName);
                sendCrashReport(previousCrashReport);
                deleteFile(context, curFileName);
            } catch (RuntimeException e) {
                Log.e(ACRA.LOG_TAG, "Failed to send crash reports for " + curFileName, e);
                deleteFile(context, curFileName);
                break; // Something really unexpected happened. Don't try to
                       // send any more reports now.
            } catch (IOException e) {
                Log.e(ACRA.LOG_TAG, "Failed to load crash report for " + curFileName, e);
                deleteFile(context, curFileName);
                break; // Something unexpected happened when reading the crash
                       // report. Don't try to send any more reports now.
            } catch (ReportSenderException e) {
                Log.e(ACRA.LOG_TAG, "Failed to send crash report for " + curFileName, e);
                // An issue occurred while sending this report but we can still try to
                // send other reports. Report sending is limited by ACRAConstants.MAX_SEND_REPORTS
                // so there's not much to fear about overloading a failing server.
            }
            reportsSentCount++;
        }
        Log.d(LOG_TAG, "#checkAndSendReports - finish");
    }

    /**
     * Sends the report with all configured ReportSenders. If at least one
     * sender completed its job, the report is considered as sent and will not
     * be sent again for failing senders.
     * 
     * @param errorContent
     *            Crash data.
     * @throws ReportSenderException
     *             if unable to send the crash report.
     */
    private void sendCrashReport(CrashReportData errorContent) throws ReportSenderException {
        if (!ACRA.isDebuggable() || ACRA.getConfig().sendReportsInDevMode()) {
            boolean sentAtLeastOnce = false;
            for (ReportSender sender : reportSenders) {
                try {
                    sender.send(errorContent);
                    // If at least one sender worked, don't re-send the report
                    // later.
                    sentAtLeastOnce = true;
                } catch (ReportSenderException e) {
                    if (!sentAtLeastOnce) {
                        throw e; // Don't log here because we aren't dealing
                                 // with the Exception here.
                    } else {
                        Log.w(LOG_TAG,
                                "ReportSender of class "
                                        + sender.getClass().getName()
                                        + " failed but other senders completed their task. ACRA will not send this report again.");
                    }
                }
            }
        }
    }

    private void deleteFile(Context context, String fileName) {
        final boolean deleted = context.deleteFile(fileName);
        if (!deleted) {
            Log.w(ACRA.LOG_TAG, "Could not delete error report : " + fileName);
        }
    }
}
