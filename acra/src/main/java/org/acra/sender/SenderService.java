/*
 *  Copyright 2017
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

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.ACRAConfiguration;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;
import org.acra.util.InstanceCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

public class SenderService extends JobIntentService {

    public static final String EXTRA_ONLY_SEND_SILENT_REPORTS = "onlySendSilentReports";
    public static final String EXTRA_APPROVE_REPORTS_FIRST = "approveReportsFirst";
    public static final String EXTRA_ACRA_CONFIG = "acraConfig";

    private final ReportLocator locator;

    public SenderService() {
        locator = new ReportLocator(this);
    }

    @Override
    protected void onHandleWork(@NonNull final Intent intent) {
        if (!intent.hasExtra(EXTRA_ACRA_CONFIG)) {
            if(ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "SenderService was started but no valid intent was delivered, will now quit");
            return;
        }

        final boolean onlySendSilentReports = intent.getBooleanExtra(EXTRA_ONLY_SEND_SILENT_REPORTS, false);
        final boolean approveReportsFirst = intent.getBooleanExtra(EXTRA_APPROVE_REPORTS_FIRST, false);

        final ACRAConfiguration config = (ACRAConfiguration) intent.getSerializableExtra(EXTRA_ACRA_CONFIG);

        final Collection<Class<? extends ReportSenderFactory>> senderFactoryClasses = config.reportSenderFactoryClasses();

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start sending reports from SenderService");
        try {
            final List<ReportSender> senderInstances = getSenderInstances(config, senderFactoryClasses);

            // Mark reports as approved
            if (approveReportsFirst) {
                markReportsAsApproved();
            }

            // Get approved reports
            final File[] reports = locator.getApprovedReports();

            final ReportDistributor reportDistributor = new ReportDistributor(this, config, senderInstances);

            // Iterate over approved reports and send via all Senders.
            int reportsSentCount = 0; // Use to rate limit sending
            final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();
            for (final File report : reports) {
                if (onlySendSilentReports && !fileNameParser.isSilent(report.getName())) {
                    continue;
                }

                if (reportsSentCount >= ACRAConstants.MAX_SEND_REPORTS) {
                    break; // send only a few reports to avoid overloading the network
                }

                reportDistributor.distribute(report);
                reportsSentCount++;
            }
        } catch (Exception e) {
            ACRA.log.e(LOG_TAG, "", e);
        }

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finished sending reports from SenderService");
    }

    @NonNull
    private List<ReportSender> getSenderInstances(@NonNull ACRAConfiguration config, @NonNull Collection<Class<? extends ReportSenderFactory>> factoryClasses) {
        final List<ReportSender> reportSenders = new ArrayList<ReportSender>();
        final InstanceCreator instanceCreator = new InstanceCreator();
        for (ReportSenderFactory factory : instanceCreator.create(factoryClasses)) {
            reportSenders.add(factory.create(this.getApplication(), config));
        }
        return reportSenders;
    }

    /**
     * Flag all pending reports as "approved" by the user. These reports can be sent.
     */
    private void markReportsAsApproved() {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Mark all pending reports as approved.");

        for (File report : locator.getUnapprovedReports()) {
            final File approvedReport = new File(locator.getApprovedFolder(), report.getName());
            if (!report.renameTo(approvedReport)) {
                ACRA.log.w(LOG_TAG, "Could not rename approved report from " + report + " to " + approvedReport);
            }
        }
    }
}
