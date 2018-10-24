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

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.widget.Toast;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;
import org.acra.util.InstanceCreator;
import org.acra.util.ToastSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

public class SenderService extends JobIntentService {

    public static final String EXTRA_ONLY_SEND_SILENT_REPORTS = "onlySendSilentReports";
    public static final String EXTRA_ACRA_CONFIG = "acraConfig";

    private final ReportLocator locator;

    public SenderService() {
        locator = new ReportLocator(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (!intent.hasExtra(EXTRA_ACRA_CONFIG)) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "SenderService was started but no valid intent was delivered, will now quit");
            return;
        }

        final boolean onlySendSilentReports = intent.getBooleanExtra(EXTRA_ONLY_SEND_SILENT_REPORTS, false);

        final CoreConfiguration config = (CoreConfiguration) intent.getSerializableExtra(EXTRA_ACRA_CONFIG);

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start sending reports from SenderService");
        try {
            final List<ReportSender> senderInstances = getSenderInstances(config);

            // Get approved reports
            final File[] reports = locator.getApprovedReports();

            final ReportDistributor reportDistributor = new ReportDistributor(this, config, senderInstances);

            // Iterate over approved reports and send via all Senders.
            int reportsSentCount = 0; // Use to rate limit sending
            final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();
            boolean anyNonSilent = false;
            for (final File report : reports) {
                final boolean isNonSilent = !fileNameParser.isSilent(report.getName());
                if (onlySendSilentReports && isNonSilent) {
                    continue;
                }
                anyNonSilent |= isNonSilent;

                if (reportsSentCount >= ACRAConstants.MAX_SEND_REPORTS) {
                    break; // send only a few reports to avoid overloading the network
                }

                if (reportDistributor.distribute(report)) {
                    reportsSentCount++;
                }
            }
            final String toast;
            if (anyNonSilent && (toast = reportsSentCount > 0 ? config.reportSendSuccessToast() : config.reportSendFailureToast()) != null) {
                new Handler(Looper.getMainLooper()).post(() -> ToastSender.sendToast(this, toast, Toast.LENGTH_LONG));
            }
        } catch (Exception e) {
            ACRA.log.e(LOG_TAG, "", e);
        }

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finished sending reports from SenderService");
    }

    @NonNull
    private List<ReportSender> getSenderInstances(@NonNull CoreConfiguration config) {
        List<Class<? extends ReportSenderFactory>> factoryClasses = config.reportSenderFactoryClasses();
        List<ReportSenderFactory> factories = !factoryClasses.isEmpty() ? new InstanceCreator().create(factoryClasses) : config.pluginLoader()
                .loadEnabled(this, config, ReportSenderFactory.class);
        final List<ReportSender> reportSenders = new ArrayList<>();
        for (ReportSenderFactory factory : factories) {
            reportSenders.add(factory.create(this.getApplication(), config));
        }
        if (reportSenders.isEmpty()) reportSenders.add(new NullSender());
        return reportSenders;
    }
}
