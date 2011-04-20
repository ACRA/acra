package org.acra;

import java.lang.reflect.Field;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class SettingsCollector {
    public static String collectSystemSettings(Context ctx) {
        StringBuilder result = new StringBuilder();
        Field[] keys = Settings.System.class.getFields();
        for (Field key : keys) {
            if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class) {
                try {
                    Object value = Settings.System.getString(ctx.getContentResolver(), (String) key.get(null));
                    if (value != null) {
                        result.append(key.getName()).append("=").append(value).append("\n");
                    }
                } catch (IllegalArgumentException e) {
                    Log.w(ACRA.LOG_TAG, "Error : ", e);
                } catch (IllegalAccessException e) {
                    Log.w(ACRA.LOG_TAG, "Error : ", e);
                }
            }
        }

        return result.toString();
    }

    public static String collectSecureSettings(Context ctx) {
        StringBuilder result = new StringBuilder();
        Field[] keys = Settings.Secure.class.getFields();
        for (Field key : keys) {
            if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class) {
                try {
                    Object value = Settings.Secure.getString(ctx.getContentResolver(), (String) key.get(null));
                    if (value != null) {
                        result.append(key.getName()).append("=").append(value).append("\n");
                    }
                } catch (IllegalArgumentException e) {
                    Log.w(ACRA.LOG_TAG, "Error : ", e);
                } catch (IllegalAccessException e) {
                    Log.w(ACRA.LOG_TAG, "Error : ", e);
                }
            }
        }

        return result.toString();
    }

}
