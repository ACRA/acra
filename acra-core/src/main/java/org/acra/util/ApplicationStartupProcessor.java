/*
 * Copyright (c) 2017 the ACRA team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;
import org.acra.interaction.ReportInteractionExecutor;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.sender.SenderServiceStarter;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

    public void checkReports(boolean enableAcra) {
        final Calendar now = Calendar.getInstance();
        //application is not ready in onAttachBaseContext, so delay this. also run it on a background thread because we're doing disk I/O
        new Handler(context.getMainLooper()).post(() -> new Thread(() -> {
            if (config.deleteOldUnsentReportsOnApplicationStart()) {
                deleteUnsentReportsFromOldAppVersion();
            }
            if (config.deleteUnapprovedReportsOnApplicationStart()) {
                reportDeleter.deleteReports(false, 1);
            }
            if (enableAcra) {
                sendApprovedReports();
                approveOneReport(now);
            }
        }).start());
    }

    private void approveOneReport(Calendar ignoreReportsAfter) {
        final File[] reports = reportLocator.getUnapprovedReports();

        if (reports.length == 0) {
            return; // There are no unapproved reports, so bail now.
        }
        //if a report was created after the application launch, it might be currently handled, so ignore it for now.
        if (new CrashReportFileNameParser().getTimestamp(reports[0].getName()).before(ignoreReportsAfter)) {
            //only approve one report at a time to prevent overwhelming users
            new ReportInteractionExecutor(context, config).performInteractions(reports[0]);
        }
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
