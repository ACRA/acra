/*
 * Copyright (c) 2017 the ACRA team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;

/**
 * Responsible for creating a SharedPreferences instance which stores ACRA settings.
 * <p>
 * Retrieves the {@link SharedPreferences} instance where user adjustable settings for ACRA are stored.
 * Default are the Application default SharedPreferences, but you can provide another SharedPreferences name with {@link org.acra.annotation.AcraCore#sharedPreferencesName()}.
 * </p>
 */
public class SharedPreferencesFactory {

    private final Context context;
    private final CoreConfiguration config;

    public SharedPreferencesFactory(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Check if the application default shared preferences contains true for the
     * key "acra.disable", do not activate ACRA. Also checks the alternative
     * opposite setting "acra.enable" if "acra.disable" is not found.
     *
     * @param prefs SharedPreferences to check to see whether ACRA should be
     *              disabled.
     * @return true if prefs indicate that ACRA should be enabled.
     */
    public static boolean shouldEnableACRA(@NonNull SharedPreferences prefs) {
        boolean enableAcra = true;
        try {
            final boolean disableAcra = prefs.getBoolean(ACRA.PREF_DISABLE_ACRA, false);
            enableAcra = prefs.getBoolean(ACRA.PREF_ENABLE_ACRA, !disableAcra);
        } catch (Exception e) {
            // In case of a ClassCastException
        }
        return enableAcra;
    }

    /**
     * @return The Shared Preferences where ACRA will retrieve its user adjustable setting.
     */
    @NonNull
    public SharedPreferences create() {
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalStateException("Cannot call ACRA.getACRASharedPreferences() before ACRA.init().");
        } else if (!ACRAConstants.DEFAULT_STRING_VALUE.equals(config.sharedPreferencesName())) {
            return context.getSharedPreferences(config.sharedPreferencesName(), Context.MODE_PRIVATE);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }
}
