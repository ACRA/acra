package org.acra.sender;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.ACRAConfiguration;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

public class SenderService extends IntentService {

    public static final String EXTRA_ONLY_SEND_SILENT_REPORTS = "onlySendSilentReports";
    public static final String EXTRA_APPROVE_REPORTS_FIRST = "approveReportsFirst";
    public static final String EXTRA_REPORT_SENDER_FACTORIES = "reportSenderFactories";
    public static final String EXTRA_ACRA_CONFIG = "acraConfig";

    private final ReportLocator locator = new ReportLocator(this);

    public SenderService() {
        super("ACRA SenderService");
    }

    @Override
    protected void onHandleIntent(@NonNull final Intent intent) {

        final boolean onlySendSilentReports = intent.getBooleanExtra(EXTRA_ONLY_SEND_SILENT_REPORTS, false);
        final boolean approveReportsFirst = intent.getBooleanExtra(EXTRA_APPROVE_REPORTS_FIRST, false);

        //noinspection unchecked
        final List<Class<? extends ReportSenderFactory>> senderFactoryClasses = (List<Class<? extends ReportSenderFactory>>) intent.getSerializableExtra(EXTRA_REPORT_SENDER_FACTORIES);

        final ACRAConfiguration config = (ACRAConfiguration) intent.getSerializableExtra(EXTRA_ACRA_CONFIG);

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
    private List<ReportSender> getSenderInstances(@NonNull ACRAConfiguration config, @NonNull List<Class<? extends ReportSenderFactory>> factoryClasses) {
        final List<ReportSender> reportSenders = new ArrayList<ReportSender>();
        for (final Class<? extends ReportSenderFactory> factoryClass : factoryClasses) {
            try {
                final ReportSenderFactory factory = factoryClass.newInstance();
                final ReportSender sender = factory.create(this.getApplication(), config);
                reportSenders.add(sender);
            } catch (InstantiationException e) {
                ACRA.log.w(LOG_TAG, "Could not construct ReportSender from " + factoryClass, e);
            } catch (IllegalAccessException e) {
                ACRA.log.w(LOG_TAG, "Could not construct ReportSender from " + factoryClass, e);
            }
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
