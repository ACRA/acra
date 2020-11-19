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
package org.acra.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.acra.ACRA
import org.acra.ACRAConstants
import org.acra.config.CoreConfiguration

/**
 * Responsible for creating a SharedPreferences instance which stores ACRA settings.
 *
 *
 * Retrieves the [SharedPreferences] instance where user adjustable settings for ACRA are stored.
 * Default are the Application default SharedPreferences, but you can provide another SharedPreferences name with [org.acra.annotation.AcraCore.sharedPreferencesName].
 *
 */
class SharedPreferencesFactory(private val context: Context, private val config: CoreConfiguration) {

    /**
     * @return The Shared Preferences where ACRA will retrieve its user adjustable setting.
     */
    fun create(): SharedPreferences {
        return if (ACRAConstants.DEFAULT_STRING_VALUE != config.sharedPreferencesName) {
            context.getSharedPreferences(config.sharedPreferencesName, Context.MODE_PRIVATE)
        } else {
            @Suppress("DEPRECATION")
            PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    companion object {
        /**
         * Check if the application default shared preferences contains true for the
         * key "acra.disable", do not activate ACRA. Also checks the alternative
         * opposite setting "acra.enable" if "acra.disable" is not found.
         *
         * @param prefs SharedPreferences to check to see whether ACRA should be
         * disabled.
         * @return true if prefs indicate that ACRA should be enabled.
         */
        fun shouldEnableACRA(prefs: SharedPreferences): Boolean {
            var enableAcra = true
            try {
                val disableAcra = prefs.getBoolean(ACRA.PREF_DISABLE_ACRA, false)
                enableAcra = prefs.getBoolean(ACRA.PREF_ENABLE_ACRA, !disableAcra)
            } catch (e: Exception) {
                // In case of a ClassCastException
            }
            return enableAcra
        }
    }
}