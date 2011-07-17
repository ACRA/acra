package org.acra.collector;

import java.util.Map;
import java.util.TreeMap;

import org.acra.ACRA;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

final class SharedPreferencesCollector {

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
            result.append(prefsId).append("\n");
            final SharedPreferences prefs = shrdPrefs.get(prefsId);
            if (prefs != null) {
                final Map<String, ?> kv = prefs.getAll();
                if (kv != null && kv.size() > 0) {
                    for (final String key : kv.keySet()) {
                        result.append(key).append("=").append(kv.get(key).toString()).append("\n");
                    }
                } else {
                    result.append("empty\n");
                }
            } else {
                result.append("null\n");
            }
            result.append("\n");
        }

        return result.toString();
    }
}
