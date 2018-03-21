package org.acra.sender;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;

import static org.acra.ACRA.LOG_TAG;

/**
 * Starts the Service(Intent)Service to process and send pending reports.
 */
public class SenderServiceStarter {

    private final Context context;
    private final ACRAConfiguration config;

    public SenderServiceStarter(@NonNull Context context, @NonNull ACRAConfiguration config) {
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
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start SenderService");
        final Intent intent = new Intent();
        intent.putExtra(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
        intent.putExtra(SenderService.EXTRA_APPROVE_REPORTS_FIRST, approveReportsFirst);
        intent.putExtra(SenderService.EXTRA_ACRA_CONFIG, config);
        JobIntentService.enqueueWork(context, SenderService.class, 0, intent);
    }
}
