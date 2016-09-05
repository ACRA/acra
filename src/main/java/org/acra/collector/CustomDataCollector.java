/*
 *  Copyright 2016
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Collects custom data supplied by the user
 *
 * @author F43nd1r
 * @since 4.9.1
 */
final class CustomDataCollector extends Collector {
    private final Map<String, String> customParameters;

    CustomDataCollector(Map<String, String> customParameters){
        super(ReportField.CUSTOM_DATA);
        this.customParameters = customParameters;
    }
    @NonNull
    @Override
    String collect(ReportField reportField, ReportBuilder reportBuilder) {
        return createCustomInfoString(reportBuilder.getCustomData());
    }


    /**
     * Generates the string which is posted in the single custom data field
     *
     * @return A string with a 'key = value' pair on each line.
     */
    @NonNull
    private String createCustomInfoString(@Nullable Map<String, String> reportCustomData) {
        Map<String, String> params = customParameters;

        if (reportCustomData != null) {
            params = new HashMap<String, String>(params);
            params.putAll(reportCustomData);
        }

        final StringBuilder customInfo = new StringBuilder();
        for (final Map.Entry<String, String> currentEntry : params.entrySet()) {
            customInfo.append(currentEntry.getKey());
            customInfo.append(" = ");

            // We need to escape new lines in values or they are transformed into new
            // custom fields. => let's replace all '\n' with "\\n"
            final String currentVal = currentEntry.getValue();
            if (currentVal != null) {
                customInfo.append(currentVal.replaceAll("\n", "\\\\n"));
            } else {
                customInfo.append("null");
            }
            customInfo.append('\n');
        }
        return customInfo.toString();
    }
}
