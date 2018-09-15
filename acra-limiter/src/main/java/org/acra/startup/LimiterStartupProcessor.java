/*
 * Copyright (c) 2018
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

package org.acra.startup;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import com.google.auto.service.AutoService;
import org.acra.ACRA;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.LimiterConfiguration;
import org.acra.config.LimiterData;
import org.acra.plugins.HasConfigPlugin;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.util.PackageManagerWrapper;

import java.io.IOException;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author lukas
 * @since 15.09.18
 */
@AutoService(StartupProcessor.class)
public class LimiterStartupProcessor extends HasConfigPlugin implements StartupProcessor {
    public LimiterStartupProcessor() {
        super(LimiterConfiguration.class);
    }

    @Override
    public void processReports(@NonNull Context context, @NonNull CoreConfiguration config, List<Report> reports) {
        final LimiterConfiguration limiterConfiguration = ConfigUtils.getPluginConfiguration(config, LimiterConfiguration.class);
        if(limiterConfiguration.deleteReportsOnAppUpdate() || limiterConfiguration.resetLimitsOnAppUpdate()) {
            final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
            final long lastVersionNr = prefs.getInt(ACRA.PREF_LAST_VERSION_NR, 0);
            final int appVersion = getAppVersion(context);

            if (appVersion > lastVersionNr) {
                if(limiterConfiguration.deleteReportsOnAppUpdate()) {
                    prefs.edit().putInt(ACRA.PREF_LAST_VERSION_NR, appVersion).apply();
                    for (Report report : reports) {
                        report.delete();
                    }
                }
                if(limiterConfiguration.resetLimitsOnAppUpdate()) {
                    try {
                        new LimiterData().store(context);
                    } catch (IOException e) {
                        ACRA.log.w(LOG_TAG, "Failed to reset LimiterData", e);
                    }
                }
            }
        }
    }

    /**
     * @return app version or 0 if PackageInfo was not available.
     */
    private int getAppVersion(@NonNull Context context) {
        final PackageManagerWrapper packageManagerWrapper = new PackageManagerWrapper(context);
        final PackageInfo packageInfo = packageManagerWrapper.getPackageInfo();
        return (packageInfo == null) ? 0 : packageInfo.versionCode;
    }
}
