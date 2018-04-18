package org.acra.scheduler;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.sender.SenderService;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
public class DefaultSenderScheduler implements SenderScheduler {
    @Override
    public void scheduleReportSending(@NonNull Context context, @NonNull CoreConfiguration config, boolean onlySendSilentReports) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start SenderService");
        final Intent intent = new Intent();
        intent.putExtra(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
        intent.putExtra(SenderService.EXTRA_ACRA_CONFIG, config);
        JobIntentService.enqueueWork(context, SenderService.class, 0, intent);
    }
}
