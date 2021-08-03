/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.collector

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import com.google.auto.service.AutoService
import org.acra.ACRA
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.prefs.SharedPreferencesFactory
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Collects the content (key/value pairs) of SharedPreferences, from the application default preferences or any other preferences asked by the application developer.
 *
 * @author F43nd1r &amp; Various
 */
@AutoService(Collector::class)
class SharedPreferencesCollector : BaseReportFieldCollector(ReportField.USER_EMAIL, ReportField.SHARED_PREFERENCES) {
    @Throws(Exception::class)
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        when (reportField) {
            ReportField.USER_EMAIL -> target.put(ReportField.USER_EMAIL, SharedPreferencesFactory(context, config).create().getString(ACRA.PREF_USER_EMAIL_ADDRESS, null))
            ReportField.SHARED_PREFERENCES -> target.put(ReportField.SHARED_PREFERENCES, collect(context, config))
            else -> throw IllegalArgumentException()
        }
    }

    /**
     * Collects all key/value pairs in SharedPreferences.
     * The application default SharedPreferences are always
     * collected, and the developer can provide additional SharedPreferences
     * names in the [org.acra.annotation.AcraCore.additionalSharedPreferences]
     * configuration item.
     *
     * @return the collected key/value pairs.
     */
    @Suppress("DEPRECATION")
    @Throws(JSONException::class)
    private fun collect(context: Context, config: CoreConfiguration): JSONObject {
        val result = JSONObject()

        // Include the default SharedPreferences
        val sharedPrefs: MutableMap<String, SharedPreferences> = TreeMap()
        sharedPrefs["default"] = PreferenceManager.getDefaultSharedPreferences(context)

        // Add in any additional SharedPreferences
        for (sharedPrefId in config.additionalSharedPreferences) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sharedPrefs[sharedPrefId] = context.createDeviceProtectedStorageContext().getSharedPreferences(sharedPrefId, Context.MODE_PRIVATE)
            }else{
                sharedPrefs[sharedPrefId] = context.getSharedPreferences(sharedPrefId, Context.MODE_PRIVATE)

            }
        }

        // Iterate over all included preference files and add the preferences from each.
        for ((sharedPrefId, prefs) in sharedPrefs) {
            val prefEntries = prefs.all

            // Show that we have no preferences saved for that preference file.
            if (prefEntries.isEmpty()) {
                result.put(sharedPrefId, "empty")
            } else {
                val iterator = prefEntries.keys.iterator()
                while (iterator.hasNext()) {
                    if (filteredKey(config, iterator.next())) {
                        iterator.remove()
                    }
                }
                result.put(sharedPrefId, JSONObject(prefEntries))
            }
        }
        return result
    }

    /**
     * Checks if the key matches one of the patterns provided by the developer
     * to exclude some preferences from reports.
     *
     * @param key the name of the preference to be checked
     * @return true if the key has to be excluded from reports.
     */
    private fun filteredKey(config: CoreConfiguration, key: String): Boolean {
        for (regex in config.excludeMatchingSharedPreferencesKeys) {
            if (key.matches(Regex(regex))) {
                return true
            }
        }
        return false
    }
}