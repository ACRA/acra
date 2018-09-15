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
import android.support.annotation.NonNull;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.file.BulkReportDeleter;
import org.acra.prefs.SharedPreferencesFactory;

/**
 * Looks for any existing reports and starts sending them.
 */
@Deprecated
public final class ApplicationStartupProcessor {

    private final Context context;
    private final CoreConfiguration config;
    private final BulkReportDeleter reportDeleter;

    public ApplicationStartupProcessor(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
        reportDeleter = new BulkReportDeleter(context);
    }

    public void checkReports() {
        //run it on a background thread because we're doing disk I/O
        new Thread(() -> {
            if (config.deleteOldUnsentReportsOnApplicationStart()) {
                deleteUnsentReportsFromOldAppVersion();
            }
        }).start();
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
     * @return app version or 0 if PackageInfo was not available.
     */
    private int getAppVersion() {
        final PackageManagerWrapper packageManagerWrapper = new PackageManagerWrapper(context);
        final PackageInfo packageInfo = packageManagerWrapper.getPackageInfo();
        return (packageInfo == null) ? 0 : packageInfo.versionCode;
    }
}
