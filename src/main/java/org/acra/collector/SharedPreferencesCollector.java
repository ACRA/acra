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
package org.acra.collector;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;

import java.util.Map;
import java.util.TreeMap;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects the content (key/value pairs) of SharedPreferences, from the
 * application default preferences or any other preferences asked by the
 * application developer.
 */
final class SharedPreferencesCollector {

    private final Context context;
    private final ACRAConfiguration config;

    public SharedPreferencesCollector(@NonNull Context context, @NonNull ACRAConfiguration config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Collects all key/value pairs in SharedPreferences and writes them in a
     * result String. The application default SharedPreferences are always
     * collected, and the developer can provide additional SharedPreferences
     * names in the {@link ReportsCrashes#additionalSharedPreferences()}
     * configuration item.
     *
     * @return A readable formatted String containing all key/value pairs.
     */
    @NonNull
    public String collect() {
        final StringBuilder result = new StringBuilder();

        // Include the default SharedPreferences
        final Map<String, SharedPreferences> sharedPrefs = new TreeMap<String, SharedPreferences>();
        sharedPrefs.put("default", PreferenceManager.getDefaultSharedPreferences(context));

        // Add in any additional SharedPreferences
        final String[] sharedPrefIds = config.additionalSharedPreferences();
        if (sharedPrefIds != null) {
            for (final String sharedPrefId : sharedPrefIds) {
                sharedPrefs.put(sharedPrefId, context.getSharedPreferences(sharedPrefId, Context.MODE_PRIVATE));
            }
        }

        // Iterate over all included preference files and add the preferences from each.
        for (Map.Entry<String, SharedPreferences> entry : sharedPrefs.entrySet()) {
            final String sharedPrefId = entry.getKey();
            final SharedPreferences prefs = entry.getValue();

            final Map<String, ?> prefEntries = prefs.getAll();

            // Show that we have no preferences saved for that preference file.
            if (prefEntries.isEmpty()) {
                result.append(sharedPrefId).append('=').append("empty\n");
                continue;
            }

            // Add all non-filtered preferences from that preference file.
            for (final Map.Entry<String, ?> prefEntry : prefEntries.entrySet()) {
                if (filteredKey(prefEntry.getKey())) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Filtered out sharedPreference=" + sharedPrefId + "  key=" + prefEntry.getKey() + " due to filtering rule");
                } else {
                    final Object prefValue = prefEntry.getValue();
                    result.append(sharedPrefId).append('.').append(prefEntry.getKey()).append('=');
                    result.append(prefValue == null ? "null" : prefValue.toString());
                    result.append("\n");
                }
            }
            result.append('\n');
        }

        return result.toString();
    }

    /**
     * Checks if the key matches one of the patterns provided by the developer
     * to exclude some preferences from reports.
     *
     * @param key the name of the preference to be checked
     * @return true if the key has to be excluded from reports.
     */
    private boolean filteredKey(@NonNull String key) {
        for (String regex : config.excludeMatchingSharedPreferencesKeys()) {
            if (key.matches(regex)) {
                return true;
            }
        }
        return false;
    }
}
