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

import java.util.Map;
import java.util.TreeMap;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Collects the content (key/value pairs) of SharedPreferences, from the
 * application default preferences or any other preferences asked by the
 * application developer.
 */
final class SharedPreferencesCollector {

    /**
     * Collects all key/value pairs in SharedPreferences and writes them in a
     * result String. The application default SharedPreferences are always
     * collected, and the developer can provide additional SharedPreferences
     * names in the {@link ReportsCrashes#additionalSharedPreferences()}
     * configuration item.
     * 
     * 
     * 
     * @param context
     *            the application context.
     * @return A readable formatted String containing all key/value pairs.
     */
    public static String collect(Context context) {
        final StringBuilder result = new StringBuilder();
        final Map<String, SharedPreferences> shrdPrefs = new TreeMap<String, SharedPreferences>();
        shrdPrefs.put("default", PreferenceManager.getDefaultSharedPreferences(context));
        final String[] shrdPrefsIds = ACRA.getConfig().additionalSharedPreferences();
        if (shrdPrefsIds != null) {
            for (final String shrdPrefId : shrdPrefsIds) {
                shrdPrefs.put(shrdPrefId, context.getSharedPreferences(shrdPrefId, Context.MODE_PRIVATE));
            }
        }

        for (final String prefsId : shrdPrefs.keySet()) {
            final SharedPreferences prefs = shrdPrefs.get(prefsId);
            if (prefs != null) {
                final Map<String, ?> kv = prefs.getAll();
                if (kv != null && kv.size() > 0) {
                    for (final String key : kv.keySet()) {
                        if (!filteredKey(key)) {
                            if (kv.get(key) != null) {
                                result.append(prefsId).append('.').append(key).append('=').append(kv.get(key).toString()).append("\n");
                            } else {
                                result.append(prefsId).append('.').append(key).append('=').append("null\n");
                            }
                        }
                    }
                } else {
                    result.append(prefsId).append('=').append("empty\n");
                }
            } else {
                result.append("null\n");
            }
            result.append('\n');
        }

        return result.toString();
    }

    /**
     * Checks if the key matches one of the patterns provided by the developer
     * to exclude some preferences from reports.
     * 
     * @param key
     *            the name of the preference to be checked
     * @return true if the key has to be excluded from reports.
     */
    private static boolean filteredKey(String key) {
        for (String regex : ACRA.getConfig().excludeMatchingSharedPreferencesKeys()) {
            if(key.matches(regex)) {
               return true; 
            }
        }
        return false;
    }
}
