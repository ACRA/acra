/*
 * Copyright (c) 2016
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

package org.acra.legacy;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Converts and moves legacy files
 *
 * @author F43nd1r
 * @since 12.10.2016
 */

public class LegacyFileHandler {
    private static final String PREF__LEGACY_ALREADY_CONVERTED_TO_4_8_0 = "acra.legacyAlreadyConvertedTo4.8.0";
    private static final String PREF__LEGACY_ALREADY_CONVERTED_TO_JSON = "acra.legacyAlreadyConvertedToJson";
    private final Context context;
    private final SharedPreferences prefs;

    public LegacyFileHandler(Context context, SharedPreferences prefs) {
        this.context = context;
        this.prefs = prefs;
    }

    public void updateToCurrentVersionIfNecessary() {
        // Check prefs to see if we have converted from legacy (pre 4.8.0) ACRA
        if (!prefs.getBoolean(PREF__LEGACY_ALREADY_CONVERTED_TO_4_8_0, false)) {
            // If not then move reports to approved/unapproved folders and mark as converted.
            new ReportMigrator(context).migrate();

            // Mark as converted.
            prefs.edit().putBoolean(PREF__LEGACY_ALREADY_CONVERTED_TO_4_8_0, true).apply();
        }
        if (!prefs.getBoolean(PREF__LEGACY_ALREADY_CONVERTED_TO_JSON, false)) {
            new ReportConverter(context).convert();

            // Mark as converted.
            prefs.edit().putBoolean(PREF__LEGACY_ALREADY_CONVERTED_TO_JSON, true).apply();
        }
    }
}
