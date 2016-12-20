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
import org.acra.model.ComplexElement;
import org.acra.model.Element;

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
    Element collect(ReportField reportField, ReportBuilder reportBuilder) {
        return createCustomInfoElement(reportBuilder.getCustomData());
    }


    /**
     * Generates the Element which is posted in the single custom data field
     *
     * @return An Element with  key-value-pairs for the supplied custom data.
     */
    @NonNull
    private Element createCustomInfoElement(@Nullable Map<String, String> reportCustomData) {
        Map<String, String> params = customParameters;
        if (reportCustomData != null) {
            params = new HashMap<String, String>(params);
            params.putAll(reportCustomData);
        }
        return new ComplexElement(params);
    }
}
