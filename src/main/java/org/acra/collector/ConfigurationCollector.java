/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
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
import android.util.SparseArray;

import org.acra.ACRA;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

/**
 * Inspects a {@link Configuration} object through reflection API in order to
 * generate a human readable String with values replaced with their constants
 * names. The {@link Configuration#toString()} method was not enough as values
 * like 0, 1, 2 or 3 don't look readable to me. Using reflection API allows to
 * retrieve hidden fields and can make us hope to be compatible with all Android
 * API levels, even those which are not published yet.
 * 
 * @author Kevin Gaudin
 * 
 */
public final class ConfigurationCollector {

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

    private final Map<String, SparseArray<String>> mValueArrays = new HashMap<String, SparseArray<String>>();

    private ConfigurationCollector() {

        final SparseArray<String> hardKeyboardHiddenValues = new SparseArray<String>();
        final SparseArray<String> keyboardValues = new SparseArray<String>();
        final SparseArray<String> keyboardHiddenValues = new SparseArray<String>();
        final SparseArray<String> navigationValues = new SparseArray<String>();
        final SparseArray<String> navigationHiddenValues = new SparseArray<String>();
        final SparseArray<String> orientationValues = new SparseArray<String>();
        final SparseArray<String> screenLayoutValues = new SparseArray<String>();
        final SparseArray<String> touchScreenValues = new SparseArray<String>();
        final SparseArray<String> uiModeValues = new SparseArray<String>();

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

        mValueArrays.put(PREFIX_HARDKEYBOARDHIDDEN, hardKeyboardHiddenValues);
        mValueArrays.put(PREFIX_KEYBOARD, keyboardValues);
        mValueArrays.put(PREFIX_KEYBOARDHIDDEN, keyboardHiddenValues);
        mValueArrays.put(PREFIX_NAVIGATION, navigationValues);
        mValueArrays.put(PREFIX_NAVIGATIONHIDDEN, navigationHiddenValues);
        mValueArrays.put(PREFIX_ORIENTATION, orientationValues);
        mValueArrays.put(PREFIX_SCREENLAYOUT, screenLayoutValues);
        mValueArrays.put(PREFIX_TOUCHSCREEN, touchScreenValues);
        mValueArrays.put(PREFIX_UI_MODE, uiModeValues);
    }

    /**
     * Use this method to generate a human readable String listing all values
     * from the provided Configuration instance.
     * 
     * @param conf
     *            The Configuration to be described.
     * @return A String describing all the fields of the given Configuration,
     *         with values replaced by constant names.
     */
    @NonNull
    private String toString(@NonNull Configuration conf) {
        final StringBuilder result = new StringBuilder();
        for (final Field f : conf.getClass().getFields()) {
            try {
                if (!Modifier.isStatic(f.getModifiers())) {
                    final String fieldName = f.getName();
                    result.append(fieldName).append('=');
                    if (f.getType().equals(int.class)) {
                        result.append(getFieldValueName(conf, f));
                    } else if(f.get(conf) != null){
                        result.append(f.get(conf).toString());
                    }
                    result.append('\n');
                }
            } catch (@NonNull IllegalArgumentException e) {
                ACRA.log.e(LOG_TAG, "Error while inspecting device configuration: ", e);
            } catch (@NonNull IllegalAccessException e) {
                ACRA.log.e(LOG_TAG, "Error while inspecting device configuration: ", e);
            }
        }
        return result.toString();
    }

    /**
     * Retrieve the name of the constant defined in the {@link Configuration}
     * class which defines the value of a field in a {@link Configuration}
     * instance.
     * 
     * @param conf
     *            The instance of {@link Configuration} where the value is
     *            stored.
     * @param f
     *            The {@link Field} to be inspected in the {@link Configuration}
     *            instance.
     * @return The value of the field f in instance conf translated to its
     *         constant name.
     * @throws IllegalAccessException if the supplied field is inaccessible.
     */
    private String getFieldValueName(@NonNull Configuration conf, @NonNull Field f) throws IllegalAccessException {
        final String fieldName = f.getName();
        if (fieldName.equals(FIELD_MCC) || fieldName.equals(FIELD_MNC)) {
            return Integer.toString(f.getInt(conf));
        } else if (fieldName.equals(FIELD_UIMODE)) {
            return activeFlags(mValueArrays.get(PREFIX_UI_MODE), f.getInt(conf));
        } else if (fieldName.equals(FIELD_SCREENLAYOUT)) {
            return activeFlags(mValueArrays.get(PREFIX_SCREENLAYOUT), f.getInt(conf));
        } else {
            final SparseArray<String> values = mValueArrays.get(fieldName.toUpperCase() + '_');
            if (values == null) {
                // Unknown field, return the raw int as String
                return Integer.toString(f.getInt(conf));
            }

            final String value = values.get(f.getInt(conf));
            if (value == null) {
                // Unknown value, return the raw int as String
                return Integer.toString(f.getInt(conf));
            }
            return value;
        }
    }

    /**
     * Some fields contain multiple value types which can be isolated by
     * applying a bitmask. That method returns the concatenation of active
     * values.
     * 
     * @param valueNames
     *            The array containing the different values and names for this
     *            field. Must contain mask values too.
     * @param bitfield
     *            The bitfield to inspect.
     * @return The names of the different values contained in the bitfield,
     *         separated by '+'.
     */
    @NonNull
    private static String activeFlags(@NonNull SparseArray<String> valueNames, int bitfield) {
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
     * @param context   Context for the application being reported.
     * @return A String representation of the current configuration for the application.
     */
    @NonNull
    public static String collectConfiguration(@NonNull Context context) {
        try {
            final ConfigurationCollector collector = new ConfigurationCollector();
            final Configuration crashConf = context.getResources().getConfiguration();
            return collector.toString(crashConf);
        } catch (RuntimeException e) {
            ACRA.log.w(LOG_TAG, "Couldn't retrieve CrashConfiguration for : " + context.getPackageName(), e);
            return "Couldn't retrieve crash config";
        }
    }
}
