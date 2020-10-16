package org.acra.sender;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;
import org.acra.plugins.PluginLoader;
import org.acra.util.BundleWrapper;
import org.acra.util.ToastSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author Lukas
 * @since 31.12.2018
 */
public class SendingConductor {
    private final Context context;
    private final CoreConfiguration config;
    private final ReportLocator locator;

    public SendingConductor(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
        locator = new ReportLocator(context);
    }

    public void sendReports(boolean foreground, BundleWrapper extras) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start sending reports from SenderService");
        try {
            final List<ReportSender> senderInstances = getSenderInstances(foreground);

            if (senderInstances.isEmpty()) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "No ReportSenders configured - adding NullSender");
                senderInstances.add(new NullSender());
            }

            // Get approved reports
            final File[] reports = locator.getApprovedReports();

            final ReportDistributor reportDistributor = new ReportDistributor(context, config, senderInstances, extras);

            // Iterate over approved reports and send via all Senders.
            int reportsSentCount = 0; // Use to rate limit sending
            final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();
            boolean anyNonSilent = false;
            for (final File report : reports) {
                final boolean isNonSilent = !fileNameParser.isSilent(report.getName());
                if (extras.getBoolean(LegacySenderService.EXTRA_ONLY_SEND_SILENT_REPORTS) && isNonSilent) {
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
            if (anyNonSilent && !(toast = reportsSentCount > 0 ? config.getReportSendSuccessToast() : config.getReportSendFailureToast()).isEmpty()) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to show " + (reportsSentCount > 0 ? "success" : "failure") + " toast");
                new Handler(Looper.getMainLooper()).post(() -> ToastSender.sendToast(context, toast, Toast.LENGTH_LONG));
            }
        } catch (Exception e) {
            ACRA.log.e(LOG_TAG, "", e);
        }

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finished sending reports from SenderService");
    }

    @NonNull
    public List<ReportSender> getSenderInstances(boolean foreground) {
        final List<ReportSenderFactory> factories;
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using PluginLoader to find ReportSender factories");
        final PluginLoader loader = config.getPluginLoader();
        factories = loader.loadEnabled(config, ReportSenderFactory.class);
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "reportSenderFactories : " + factories);


        final List<ReportSender> reportSenders = new ArrayList<>();
        for (ReportSenderFactory factory : factories) {
            final ReportSender sender = factory.create(context, config);
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Adding reportSender : " + sender);
            if (foreground == sender.requiresForeground()) {
                reportSenders.add(sender);
            }
        }
        return reportSenders;
    }
}
