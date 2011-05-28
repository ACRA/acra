package org.acra;

import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesCollector {

    public static String collect(Context context) {
        StringBuilder result = new StringBuilder();
        Map<String, SharedPreferences> shrdPrefs = new TreeMap<String, SharedPreferences>();
        shrdPrefs.put("default", PreferenceManager.getDefaultSharedPreferences(context));
        String[] shrdPrefsIds = ACRA.getConfig().additionalSharedPreferences();
        if (shrdPrefsIds != null) {
            for (String shrdPrefId : shrdPrefsIds) {
                shrdPrefs.put(shrdPrefId, context.getSharedPreferences(shrdPrefId, Context.MODE_PRIVATE));
            }
        }

        SharedPreferences prefs = null;
        for (String prefsId : shrdPrefs.keySet()) {
            result.append(prefsId).append("\n");
            prefs = shrdPrefs.get(prefsId);
            if (prefs != null) {
                Map<String, ?> kv = prefs.getAll();
                if (kv != null && kv.size() > 0) {
                    for (String key : kv.keySet()) {
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
