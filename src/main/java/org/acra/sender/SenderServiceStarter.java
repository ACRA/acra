package org.acra.sender;

import android.content.Context;
import android.content.Intent;
import org.acra.ACRA;
import org.acra.config.ACRAConfig;

import static org.acra.ACRA.LOG_TAG;

/**
 * Starts the Service(Intent)Service to process and send pending reports.
 */
public class SenderServiceStarter {

    private final Context context;
    private final ACRAConfig config;

    public SenderServiceStarter(Context context, ACRAConfig config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Starts a Thread to start sending outstanding error reports.
     *
     * @param onlySendSilentReports If true then only send silent reports.
     * @param approveReportsFirst   If true then approve unapproved reports first.
     */
    public void startService(boolean onlySendSilentReports, boolean approveReportsFirst) {
        ACRA.log.v(LOG_TAG, "About to start SenderService");
        final Intent intent = new Intent(context, SenderService.class);
        intent.putExtra(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
        intent.putExtra(SenderService.EXTRA_APPROVE_REPORTS_FIRST, approveReportsFirst);
        intent.putExtra(SenderService.EXTRA_REPORT_SENDER_FACTORIES, config.reportSenderFactoryClasses());
        intent.putExtra(SenderService.EXTRA_ACRA_CONFIG, config);
        context.startService(intent);
    }
}
