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
import android.os.Build;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.ACRAConfiguration;
import org.acra.model.ComplexElement;
import org.acra.model.Element;
import org.json.JSONException;

import java.lang.reflect.Field;

import static org.acra.ACRA.LOG_TAG;

/**
 * collects data from {@link System}, {@link Global} and {@link Secure} Settings
 * classes.
 *
 * @author Kevin Gaudin & F43nd1r
 */
final class SettingsCollector extends Collector {

    private static final String ERROR = "Error: ";

    private final Context context;
    private final ACRAConfiguration config;

    SettingsCollector(@NonNull Context context, @NonNull ACRAConfiguration config) {
        super(ReportField.SETTINGS_SYSTEM, ReportField.SETTINGS_SECURE, ReportField.SETTINGS_GLOBAL);
        this.context = context;
        this.config = config;
    }

    /**
     * Collect data from {@link System}. This
     * collector uses reflection to be sure to always get the most accurate data
     * whatever Android API level it runs on.
     *
     * @return collected key-value pairs.
     */
    @NonNull
    private Element collectSystemSettings() throws JSONException {
        final ComplexElement result = new ComplexElement();
        final Field[] keys = System.class.getFields();
        for (final Field key : keys) {
            // Avoid retrieving deprecated fields... it is useless, has an
            // impact on prefs, and the system writes many warnings in the
            // logcat.
            if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class) {
                try {
                    final Object value = System.getString(context.getContentResolver(), (String) key.get(null));
                    if (value != null) {
                        result.put(key.getName(), value);
                    }
                } catch (@NonNull IllegalArgumentException e) {
                    ACRA.log.w(LOG_TAG, ERROR, e);
                } catch (@NonNull IllegalAccessException e) {
                    ACRA.log.w(LOG_TAG, ERROR, e);
                }
            }
        }
        return result;
    }

    /**
     * Collect data from {@link Secure}. This
     * collector uses reflection to be sure to always get the most accurate data
     * whatever Android API level it runs on.
     *
     * @return collected key-value pairs.
     */
    @NonNull
    private Element collectSecureSettings() throws JSONException {
        final ComplexElement result = new ComplexElement();
        final Field[] keys = Secure.class.getFields();
        for (final Field key : keys) {
            if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class && isAuthorized(key)) {
                try {
                    final Object value = Secure.getString(context.getContentResolver(), (String) key.get(null));
                    if (value != null) {
                        result.put(key.getName(), value);
                    }
                } catch (@NonNull IllegalArgumentException e) {
                    ACRA.log.w(LOG_TAG, ERROR, e);
                } catch (@NonNull IllegalAccessException e) {
                    ACRA.log.w(LOG_TAG, ERROR, e);
                }
            }
        }
        return result;
    }

    /**
     * Collect data from {@link Global}. This
     * collector uses reflection to be sure to always get the most accurate data
     * whatever Android API level it runs on.
     *
     * @return collected key-value pairs.
     */
    @NonNull
    private Element collectGlobalSettings() throws JSONException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return ACRAConstants.NOT_AVAILABLE;
        }

        final ComplexElement result = new ComplexElement();
        final Field[] keys = Global.class.getFields();
        for (final Field key : keys) {
            if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class && isAuthorized(key)) {
                try {
                    final Object value = Global.getString(context.getContentResolver(), (String) key.get(null));
                    if (value != null) {
                        result.put(key.getName(), value);
                    }
                } catch (@NonNull IllegalArgumentException e) {
                    ACRA.log.w(LOG_TAG, ERROR, e);
                } catch (@NonNull SecurityException e) {
                    ACRA.log.w(LOG_TAG, ERROR, e);
                } catch (@NonNull IllegalAccessException e) {
                    ACRA.log.w(LOG_TAG, ERROR, e);
                }
            }
        }
        return result;
    }

    private boolean isAuthorized(@Nullable Field key) {
        if (key == null || key.getName().startsWith("WIFI_AP")) {
            return false;
        }
        for (String regex : config.excludeMatchingSettingsKeys()) {
            if (key.getName().matches(regex)) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    @Override
    Element collect(ReportField reportField, ReportBuilder reportBuilder) {
        try {
        switch (reportField) {
            case SETTINGS_SYSTEM:
                    return collectSystemSettings();
            case SETTINGS_SECURE:
                return collectSecureSettings();
            case SETTINGS_GLOBAL:
                return collectGlobalSettings();
            default:
                //will not happen if used correctly
                throw new IllegalArgumentException();
        }
        } catch (JSONException e) {
            ACRA.log.w("Could not collect Settings", e);
            return ACRAConstants.NOT_AVAILABLE;
        }
    }
}
