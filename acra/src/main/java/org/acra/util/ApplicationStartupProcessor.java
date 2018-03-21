package org.acra.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.config.ACRAConfiguration;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;
import org.acra.prefs.PrefUtils;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.sender.SenderServiceStarter;

import java.io.File;

/**
 * Looks for any existing reports and starts sending them.
 */
public final class ApplicationStartupProcessor {

    private final Context context;
    private final ACRAConfiguration config;

    public ApplicationStartupProcessor(@NonNull Context context, @NonNull ACRAConfiguration config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Delete any old unsent reports if this is a newer version of the app than when we last started.
     */
    public void deleteUnsentReportsFromOldAppVersion() {
        final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
        final long lastVersionNr = prefs.getInt(ACRA.PREF_LAST_VERSION_NR, 0);
        final int appVersion = getAppVersion();

        if (appVersion > lastVersionNr) {
            final BulkReportDeleter reportDeleter = new BulkReportDeleter(context);
            reportDeleter.deleteReports(true, 0);
            reportDeleter.deleteReports(false, 0);

            final SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putInt(ACRA.PREF_LAST_VERSION_NR, appVersion);
            PrefUtils.save(prefsEditor);
        }
    }

    /**
     * Deletes all the unapproved reports except for the last one.
     *
     * NOTIFICATION or DIALOG mode require explicit approval by user.
     * If latest notification/dialog has been ignored: neither accepted nor refused; they will accumulate.
     * So destroy all unapproved reports bar the last one.
     */
    public void deleteAllUnapprovedReportsBarOne() {
        new BulkReportDeleter(context).deleteReports(false, 1);
    }

    /**
     * If ReportingInteractionMode == Toast and at least one non silent report then show a Toast.
     * All approved reports will be sent.
     */
    public void sendApprovedReports() {

        final ReportLocator reportLocator = new ReportLocator(context);
        final File[] reportFiles = reportLocator.getApprovedReports();

        if (reportFiles.length == 0) {
            return; // There are no approved reports, so bail now.
        }

        if (config.reportingInteractionMode() == ReportingInteractionMode.TOAST && hasNonSilentApprovedReports(reportFiles)) {
            ToastSender.sendToast(context, config.resToastText(), Toast.LENGTH_LONG);
        }

        // Send the approved reports.
        final SenderServiceStarter starter = new SenderServiceStarter(context, config);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                starter.startService(false, false);
            }
        });

    }

    /**
     * @return app version or 0 if PackageInfo was not available.
     */
    private int getAppVersion() {
        final PackageManagerWrapper packageManagerWrapper = new PackageManagerWrapper(context);
        final PackageInfo packageInfo = packageManagerWrapper.getPackageInfo();
        return (packageInfo == null) ? 0 : packageInfo.versionCode;
    }

    private boolean hasNonSilentApprovedReports(File[] reportFiles) {
        final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();
        for (final File file : reportFiles) {
            if (!fileNameParser.isSilent(file.getName())) {
                return true;
            }
        }
        return false;
    }
}
