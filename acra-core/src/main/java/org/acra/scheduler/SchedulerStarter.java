package org.acra.scheduler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.file.ReportLocator;
import org.acra.plugins.PluginLoader;

import java.io.File;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
public class SchedulerStarter {

    private final Context context;
    private final CoreConfiguration config;
    private final ReportLocator locator;
    private final SenderScheduler senderScheduler;

    public SchedulerStarter(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
        locator = new ReportLocator(context);
        List<SenderScheduler> schedulers = new PluginLoader(config).load(SenderScheduler.class);
        if (schedulers.isEmpty()) {
            senderScheduler = new DefaultSenderScheduler();
        } else {
            senderScheduler = schedulers.get(0);
            if (schedulers.size() > 1) ACRA.log.w(ACRA.LOG_TAG, "More than one SenderScheduler found. Will use only " + senderScheduler.getClass().getSimpleName());
        }
    }

    /**
     * Starts a process to start sending outstanding error reports.
     *
     * @param report                If not null, this report will be approved before scheduling.
     * @param onlySendSilentReports If true then only send silent reports.
     */
    public void scheduleReports(@Nullable File report, boolean onlySendSilentReports) {
        if (report != null) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Mark " + report.getName() + " as approved.");
            final File approvedReport = new File(locator.getApprovedFolder(), report.getName());
            if (!report.renameTo(approvedReport)) {
                ACRA.log.w(LOG_TAG, "Could not rename approved report from " + report + " to " + approvedReport);
            }
        }
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Schedule report sending");
        senderScheduler.scheduleReportSending(context, config, onlySendSilentReports);
    }
}
