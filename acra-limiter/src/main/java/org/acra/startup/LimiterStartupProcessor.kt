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
package org.acra.startup

import android.content.Context
import com.google.auto.service.AutoService
import org.acra.ACRA
import org.acra.config.CoreConfiguration
import org.acra.config.LimiterConfiguration
import org.acra.config.LimiterData
import org.acra.config.getPluginConfiguration
import org.acra.log.warn
import org.acra.plugins.HasConfigPlugin
import org.acra.prefs.SharedPreferencesFactory
import org.acra.util.PackageManagerWrapper
import org.acra.util.versionCodeLong
import java.io.IOException

/**
 * @author lukas
 * @since 15.09.18
 */
@AutoService(StartupProcessor::class)
class LimiterStartupProcessor : HasConfigPlugin(LimiterConfiguration::class.java), StartupProcessor {
    override fun processReports(context: Context, config: CoreConfiguration, reports: List<Report>) {
        val limiterConfiguration = config.getPluginConfiguration<LimiterConfiguration>()
        if (limiterConfiguration.deleteReportsOnAppUpdate || limiterConfiguration.resetLimitsOnAppUpdate) {
            val prefs = SharedPreferencesFactory(context, config).create()
            val lastVersionNr = try {
                try {
                    prefs.getLong(ACRA.PREF_LAST_VERSION_NR, 0)
                } catch (e: ClassCastException) {
                    prefs.getInt(ACRA.PREF_LAST_VERSION_NR, 0).toLong()
                }
            } catch (e: Exception) {
                0
            }
            val appVersion = getAppVersion(context)
            if (appVersion > lastVersionNr) {
                if (limiterConfiguration.deleteReportsOnAppUpdate) {
                    prefs.edit().putLong(ACRA.PREF_LAST_VERSION_NR, appVersion).apply()
                    for (report in reports) {
                        report.delete = true
                    }
                }
                if (limiterConfiguration.resetLimitsOnAppUpdate) {
                    try {
                        LimiterData().store(context)
                    } catch (e: IOException) {
                        warn(e) { "Failed to reset LimiterData" }
                    }
                }
            }
        }
    }

    /**
     * @return app version or 0 if PackageInfo was not available.
     */
    private fun getAppVersion(context: Context): Long = PackageManagerWrapper(context).getPackageInfo()?.versionCodeLong ?: 0
}