package org.acra.scheduler;

import android.content.Context;
import android.support.annotation.NonNull;
import org.acra.config.CoreConfiguration;
import org.acra.plugins.Plugin;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
public interface SenderScheduler extends Plugin {
    void scheduleReportSending(@NonNull Context context, @NonNull CoreConfiguration config, boolean onlySendSilentReports);
}
