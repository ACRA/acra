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
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.builder.ReportBuilder;
import org.acra.config.ACRAConfiguration;
import org.acra.model.ComplexElement;
import org.acra.model.Element;
import org.acra.model.StringElement;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects the content (key/value pairs) of SharedPreferences, from the
 * application default preferences or any other preferences asked by the
 * application developer.
 */
final class SharedPreferencesCollector extends Collector {

    private final Context context;
    private final ACRAConfiguration config;
    private final SharedPreferences prefs;

    SharedPreferencesCollector(@NonNull Context context, @NonNull ACRAConfiguration config, SharedPreferences prefs) {
        super(ReportField.USER_EMAIL, ReportField.SHARED_PREFERENCES);
        this.context = context;
        this.config = config;
        this.prefs = prefs;
    }

    /**
     * Collects all key/value pairs in SharedPreferences.
     * The application default SharedPreferences are always
     * collected, and the developer can provide additional SharedPreferences
     * names in the {@link ReportsCrashes#additionalSharedPreferences()}
     * configuration item.
     *
     * @return the collected key/value pairs.
     */
    @NonNull
    private Element collect() throws JSONException {
        final ComplexElement result = new ComplexElement();

        // Include the default SharedPreferences
        final Map<String, SharedPreferences> sharedPrefs = new TreeMap<String, SharedPreferences>();
        sharedPrefs.put("default", PreferenceManager.getDefaultSharedPreferences(context));

        // Add in any additional SharedPreferences
        for (final String sharedPrefId : config.additionalSharedPreferences()) {
            sharedPrefs.put(sharedPrefId, context.getSharedPreferences(sharedPrefId, Context.MODE_PRIVATE));
        }

        // Iterate over all included preference files and add the preferences from each.
        for (Map.Entry<String, SharedPreferences> entry : sharedPrefs.entrySet()) {
            final String sharedPrefId = entry.getKey();
            final SharedPreferences prefs = entry.getValue();

            final Map<String, ?> prefEntries = prefs.getAll();

            // Show that we have no preferences saved for that preference file.
            if (prefEntries.isEmpty()) {
                result.put(sharedPrefId, "empty");
            } else {
                for (Iterator<String> iterator = prefEntries.keySet().iterator(); iterator.hasNext();){
                    if(filteredKey(iterator.next())){
                        iterator.remove();
                    }
                }
                result.put(sharedPrefId, new JSONObject(prefEntries));
            }
        }

        return result;
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

    @NonNull
    @Override
    Element collect(ReportField reportField, ReportBuilder reportBuilder) {
        switch (reportField) {
            case USER_EMAIL:
                String email = prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, null);
                return email != null ? new StringElement(email) : ACRAConstants.NOT_AVAILABLE;
            case SHARED_PREFERENCES:
                try {
                    return collect();
                } catch (JSONException e) {
                    ACRA.log.w(LOG_TAG, "Could not collect shared preferences", e);
                    return ACRAConstants.NOT_AVAILABLE;
                }
            default:
                //will not happen if used correctly
                throw new IllegalArgumentException();
        }
    }
}
