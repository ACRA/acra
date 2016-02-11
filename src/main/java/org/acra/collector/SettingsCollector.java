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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.System;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.acra.ACRA.LOG_TAG;

/**
 * Helper to collect data from {@link System} and {@link Secure} Settings
 * classes.
 * 
 * @author Kevin Gaudin
 * 
 */
final class SettingsCollector {

    private final Context context;
    private final ACRAConfiguration config;

    public SettingsCollector(Context context, ACRAConfiguration config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Collect data from {@link android.provider.Settings.System}. This
     * collector uses reflection to be sure to always get the most accurate data
     * whatever Android API level it runs on.
     * 
     * @return A human readable String containing one key=value pair per line.
     */
    public String collectSystemSettings() {
        final StringBuilder result = new StringBuilder();
        final Field[] keys = Settings.System.class.getFields();
        for (final Field key : keys) {
            // Avoid retrieving deprecated fields... it is useless, has an
            // impact on perfs, and the system writes many warnings in the
            // logcat.
            if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class) {
                try {
                    final Object value = Settings.System.getString(context.getContentResolver(), (String) key.get(null));
                    if (value != null) {
                        result.append(key.getName()).append("=").append(value).append("\n");
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    ACRA.log.w(LOG_TAG, "Error : ", e);
                }
            }
        }

        return result.toString();
    }

    /**
     * Collect data from {@link android.provider.Settings.Secure}. This
     * collector uses reflection to be sure to always get the most accurate data
     * whatever Android API level it runs on.
     * 
     * @return A human readable String containing one key=value pair per line.
     */
    public String collectSecureSettings() {
        final StringBuilder result = new StringBuilder();
        final Field[] keys = Settings.Secure.class.getFields();
        for (final Field key : keys) {
            if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class && isAuthorized(key)) {
                try {
                    final Object value = Settings.Secure.getString(context.getContentResolver(), (String) key.get(null));
                    if (value != null) {
                        result.append(key.getName()).append("=").append(value).append("\n");
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    ACRA.log.w(LOG_TAG, "Error : ", e);
                }
            }
        }

        return result.toString();
    }

    /**
     * Collect data from {@link android.provider.Settings.Global}. This
     * collector uses reflection to be sure to always get the most accurate data
     * whatever Android API level it runs on.
     * 
     * @return A human readable String containing one key=value pair per line.
     */
    public String collectGlobalSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return "";
        }

        final StringBuilder result = new StringBuilder();
        try {
            final Class<?> globalClass = Class.forName("android.provider.Settings$Global");
            final Field[] keys = globalClass.getFields();
            final Method getString = globalClass.getMethod("getString", ContentResolver.class, String.class);
            for (final Field key : keys) {
                if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class && isAuthorized(key)) {
                    final Object value = getString.invoke(null, context.getContentResolver(), key.get(null));
                    if (value != null) {
                        result.append(key.getName()).append("=").append(value).append("\n");
                    }
                }
            }
        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException e) {
            ACRA.log.w(LOG_TAG, "Error : ", e);
        }

        return result.toString();
    }

    private boolean isAuthorized(Field key) {
        if (key == null || key.getName().startsWith("WIFI_AP")) {
            return false;
        }
        for (String regex : config.excludeMatchingSettingsKeys()) {
            if(key.getName().matches(regex)) {
               return false; 
            }
        }
        return true;
    }

}
