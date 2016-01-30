package org.acra.prefs;

import android.content.SharedPreferences;
import android.os.Build;

public final class PrefUtils {

    public static void save(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
