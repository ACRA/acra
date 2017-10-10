package org.acra.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.file.BulkReportDeleter;
import org.acra.file.ReportLocator;
import org.acra.interaction.ReportInteractionExecutor;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.sender.SenderServiceStarter;

import java.io.File;

/**
 * Looks for any existing reports and starts sending them.
 */
public final class ApplicationStartupProcessor {

    private final Context context;
    private final CoreConfiguration config;
    private final BulkReportDeleter reportDeleter;
    private final ReportLocator reportLocator;

    public ApplicationStartupProcessor(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
        reportDeleter = new BulkReportDeleter(context);
        reportLocator = new ReportLocator(context);
    }

    public void checkReports(boolean enableAcra){
        if (config.deleteOldUnsentReportsOnApplicationStart()) {
            deleteUnsentReportsFromOldAppVersion();
        }
        if (config.deleteUnapprovedReportsOnApplicationStart()) {
            deleteAllUnapprovedReportsBarOne();
        }
        if (enableAcra) {
            sendApprovedReports();
            approveReports();
        }
    }

    private void approveReports() {
        final File[] reports = reportLocator.getUnapprovedReports();

        if (reports.length == 0) {
            return; // There are no unapproved reports, so bail now.
        }
        //only approve one report at a time to prevent overwhelming users
        new ReportInteractionExecutor().performInteractions(context, config, reports[0]);
    }

    /**
     * Delete any old unsent reports if this is a newer version of the app than when we last started.
     */
    private void deleteUnsentReportsFromOldAppVersion() {
        final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
        final long lastVersionNr = prefs.getInt(ACRA.PREF_LAST_VERSION_NR, 0);
        final int appVersion = getAppVersion();

        if (appVersion > lastVersionNr) {
            reportDeleter.deleteReports(true, 0);
            reportDeleter.deleteReports(false, 0);

            prefs.edit().putInt(ACRA.PREF_LAST_VERSION_NR, appVersion).apply();
        }
    }

    /**
     * Deletes all the unapproved reports except for the last one.
     *
     * NOTIFICATION or DIALOG mode require explicit approval by user.
     * If latest notification/dialog has been ignored: neither accepted nor refused; they will accumulate.
     * So destroy all unapproved reports bar the last one.
     */
    private void deleteAllUnapprovedReportsBarOne() {
        reportDeleter.deleteReports(false, 1);
    }

    /**
     * If ReportingInteractionMode == Toast and at least one non silent report then show a Toast.
     * All approved reports will be sent.
     */
    private void sendApprovedReports() {
        final File[] reportFiles = reportLocator.getApprovedReports();

        if (reportFiles.length == 0) {
            return; // There are no approved reports, so bail now.
        }

        // Send the approved reports.
        final SenderServiceStarter starter = new SenderServiceStarter(context, config);
        starter.startService(false, false);

    }

    /**
     * @return app version or 0 if PackageInfo was not available.
     */
    private int getAppVersion() {
        final PackageManagerWrapper packageManagerWrapper = new PackageManagerWrapper(context);
        final PackageInfo packageInfo = packageManagerWrapper.getPackageInfo();
        return (packageInfo == null) ? 0 : packageInfo.versionCode;
    }
}
