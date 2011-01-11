package org.acra;

import java.lang.reflect.Field;

import android.os.Build;

public class Compatibility {
    static int getAPILevel() {
        int apiLevel;
        try {
            Field SDK_INT = Build.VERSION.class.getField("SDK_INT");
            apiLevel = SDK_INT.getInt(null);
        } catch (SecurityException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (NoSuchFieldException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (IllegalArgumentException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (IllegalAccessException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        }

        return apiLevel;
    }
}
