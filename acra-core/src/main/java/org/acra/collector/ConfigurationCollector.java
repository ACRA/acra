/*
 *  Copyright 2010 Emmanuel Astier &amp; Kevin Gaudin
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
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

/**
 * Inspects a {@link Configuration} object through reflection API in order to generate a human readable String with values replaced with their constants names.
 * The {@link Configuration#toString()} method was not enough as values like 0, 1, 2 or 3 aren't readable.
 * Using reflection API allows to retrieve hidden fields and can make us hope to be compatible with all Android API levels, even those which are not published yet.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector.class)
public final class ConfigurationCollector extends BaseReportFieldCollector implements ApplicationStartupCollector {

    private static final String SUFFIX_MASK = "_MASK";
    private static final String FIELD_SCREENLAYOUT = "screenLayout";
    private static final String FIELD_UIMODE = "uiMode";
    private static final String FIELD_MNC = "mnc";
    private static final String FIELD_MCC = "mcc";
    private static final String PREFIX_UI_MODE = "UI_MODE_";
    private static final String PREFIX_TOUCHSCREEN = "TOUCHSCREEN_";
    private static final String PREFIX_SCREENLAYOUT = "SCREENLAYOUT_";
    private static final String PREFIX_ORIENTATION = "ORIENTATION_";
    private static final String PREFIX_NAVIGATIONHIDDEN = "NAVIGATIONHIDDEN_";
    private static final String PREFIX_NAVIGATION = "NAVIGATION_";
    private static final String PREFIX_KEYBOARDHIDDEN = "KEYBOARDHIDDEN_";
    private static final String PREFIX_KEYBOARD = "KEYBOARD_";
    private static final String PREFIX_HARDKEYBOARDHIDDEN = "HARDKEYBOARDHIDDEN_";

    private JSONObject initialConfiguration;

    public ConfigurationCollector() {
        super(ReportField.INITIAL_CONFIGURATION, ReportField.CRASH_CONFIGURATION);
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config,
                 @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) {
        switch (reportField) {
            case INITIAL_CONFIGURATION:
                target.put(ReportField.INITIAL_CONFIGURATION, initialConfiguration);
                break;
            case CRASH_CONFIGURATION:
                target.put(ReportField.CRASH_CONFIGURATION, collectConfiguration(context));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectApplicationStartUp(@NonNull Context context, @NonNull CoreConfiguration config) {
        if(config.reportContent().contains(ReportField.INITIAL_CONFIGURATION)) {
            initialConfiguration = collectConfiguration(context);
        }
    }

    /**
     * Creates a {@link JSONObject} listing all values human readable
     * from the provided Configuration instance.
     *
     * @param conf The Configuration to be described.
     * @return A JSONObject with all fields of the given Configuration,
     * with values replaced by constant names.
     */
    @NonNull
    private JSONObject configToJson(@NonNull Configuration conf) {
        final JSONObject result = new JSONObject();
        final Map<String, SparseArray<String>> valueArrays = getValueArrays();
        for (final Field f : conf.getClass().getFields()) {
            try {
                if (!Modifier.isStatic(f.getModifiers())) {
                    final String fieldName = f.getName();
                    try {
                        if (f.getType().equals(int.class)) {
                            result.put(fieldName, getFieldValueName(valueArrays, conf, f));
                        } else if (f.get(conf) != null) {
                            result.put(fieldName, f.get(conf));
                        }
                    } catch (JSONException e) {
                        ACRA.log.w(LOG_TAG, "Could not collect configuration field " + fieldName, e);
                    }
                }
            } catch (@NonNull IllegalArgumentException e) {
                ACRA.log.e(LOG_TAG, "Error while inspecting device configuration: ", e);
            } catch (@NonNull IllegalAccessException e) {
                ACRA.log.e(LOG_TAG, "Error while inspecting device configuration: ", e);
            }
        }
        return result;
    }

    @NonNull
    private Map<String, SparseArray<String>> getValueArrays() {
        final Map<String, SparseArray<String>> valueArrays = new HashMap<>();
        final SparseArray<String> hardKeyboardHiddenValues = new SparseArray<>();
        final SparseArray<String> keyboardValues = new SparseArray<>();
        final SparseArray<String> keyboardHiddenValues = new SparseArray<>();
        final SparseArray<String> navigationValues = new SparseArray<>();
        final SparseArray<String> navigationHiddenValues = new SparseArray<>();
        final SparseArray<String> orientationValues = new SparseArray<>();
        final SparseArray<String> screenLayoutValues = new SparseArray<>();
        final SparseArray<String> touchScreenValues = new SparseArray<>();
        final SparseArray<String> uiModeValues = new SparseArray<>();

        for (final Field f : Configuration.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
                final String fieldName = f.getName();
                try {
                    if (fieldName.startsWith(PREFIX_HARDKEYBOARDHIDDEN)) {
                        hardKeyboardHiddenValues.put(f.getInt(null), fieldName);
                    } else if (fieldName.startsWith(PREFIX_KEYBOARD)) {
                        keyboardValues.put(f.getInt(null), fieldName);
                    } else if (fieldName.startsWith(PREFIX_KEYBOARDHIDDEN)) {
                        keyboardHiddenValues.put(f.getInt(null), fieldName);
                    } else if (fieldName.startsWith(PREFIX_NAVIGATION)) {
                        navigationValues.put(f.getInt(null), fieldName);
                    } else if (fieldName.startsWith(PREFIX_NAVIGATIONHIDDEN)) {
                        navigationHiddenValues.put(f.getInt(null), fieldName);
                    } else if (fieldName.startsWith(PREFIX_ORIENTATION)) {
                        orientationValues.put(f.getInt(null), fieldName);
                    } else if (fieldName.startsWith(PREFIX_SCREENLAYOUT)) {
                        screenLayoutValues.put(f.getInt(null), fieldName);
                    } else if (fieldName.startsWith(PREFIX_TOUCHSCREEN)) {
                        touchScreenValues.put(f.getInt(null), fieldName);
                    } else if (fieldName.startsWith(PREFIX_UI_MODE)) {
                        uiModeValues.put(f.getInt(null), fieldName);
                    }
                } catch (@NonNull IllegalArgumentException e) {
                    ACRA.log.w(LOG_TAG, "Error while inspecting device configuration: ", e);
                } catch (@NonNull IllegalAccessException e) {
                    ACRA.log.w(LOG_TAG, "Error while inspecting device configuration: ", e);
                }
            }
        }

        valueArrays.put(PREFIX_HARDKEYBOARDHIDDEN, hardKeyboardHiddenValues);
        valueArrays.put(PREFIX_KEYBOARD, keyboardValues);
        valueArrays.put(PREFIX_KEYBOARDHIDDEN, keyboardHiddenValues);
        valueArrays.put(PREFIX_NAVIGATION, navigationValues);
        valueArrays.put(PREFIX_NAVIGATIONHIDDEN, navigationHiddenValues);
        valueArrays.put(PREFIX_ORIENTATION, orientationValues);
        valueArrays.put(PREFIX_SCREENLAYOUT, screenLayoutValues);
        valueArrays.put(PREFIX_TOUCHSCREEN, touchScreenValues);
        valueArrays.put(PREFIX_UI_MODE, uiModeValues);
        return valueArrays;
    }

    /**
     * Retrieve the name of the constant defined in the {@link Configuration}
     * class which defines the value of a field in a {@link Configuration}
     * instance.
     *
     * @param conf The instance of {@link Configuration} where the value is
     *             stored.
     * @param f    The {@link Field} to be inspected in the {@link Configuration}
     *             instance.
     * @return The value of the field f in instance conf translated to its
     * constant name.
     * @throws IllegalAccessException if the supplied field is inaccessible.
     */
    private Object getFieldValueName(@NonNull Map<String, SparseArray<String>> valueArrays, @NonNull Configuration conf, @NonNull Field f) throws IllegalAccessException {
        final String fieldName = f.getName();
        switch (fieldName) {
            case FIELD_MCC:
            case FIELD_MNC:
                return f.getInt(conf);
            case FIELD_UIMODE:
                return activeFlags(valueArrays.get(PREFIX_UI_MODE), f.getInt(conf));
            case FIELD_SCREENLAYOUT:
                return activeFlags(valueArrays.get(PREFIX_SCREENLAYOUT), f.getInt(conf));
            default:
                final SparseArray<String> values = valueArrays.get(fieldName.toUpperCase() + '_');
                if (values == null) {
                    // Unknown field, return the raw int as String
                    return f.getInt(conf);
                }

                final String value = values.get(f.getInt(conf));
                if (value == null) {
                    // Unknown value, return the raw int as String
                    return f.getInt(conf);
                }
                return value;
        }
    }

    /**
     * Some fields contain multiple value types which can be isolated by
     * applying a bitmask. That method returns the concatenation of active
     * values.
     *
     * @param valueNames The array containing the different values and names for this
     *                   field. Must contain mask values too.
     * @param bitfield   The bitfield to inspect.
     * @return The names of the different values contained in the bitfield,
     * separated by '+'.
     */
    @NonNull
    private String activeFlags(@NonNull SparseArray<String> valueNames, int bitfield) {
        final StringBuilder result = new StringBuilder();

        // Look for masks, apply it an retrieve the masked value
        for (int i = 0; i < valueNames.size(); i++) {
            final int maskValue = valueNames.keyAt(i);
            if (valueNames.get(maskValue).endsWith(SUFFIX_MASK)) {
                final int value = bitfield & maskValue;
                if (value > 0) {
                    if (result.length() > 0) {
                        result.append('+');
                    }
                    result.append(valueNames.get(value));
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns the current Configuration for this application.
     *
     * @param context Context for the application being reported.
     * @return A String representation of the current configuration for the application.
     */
    @Nullable
    private JSONObject collectConfiguration(@NonNull Context context) {
        try {
            return configToJson(context.getResources().getConfiguration());
        } catch (RuntimeException e) {
            ACRA.log.w(LOG_TAG, "Couldn't retrieve CrashConfiguration for : " + context.getPackageName(), e);
            return null;
        }
    }
}
