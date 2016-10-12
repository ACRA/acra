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
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.model.ComplexElement;
import org.acra.model.Element;

import static org.acra.ACRA.LOG_TAG;

/**
 * Features declared as available on the device.
 *
 * @author Kevin Gaudin & F43nd1r
 */
final class DeviceFeaturesCollector extends Collector {
    private final Context context;

    DeviceFeaturesCollector(Context context) {
        super(ReportField.DEVICE_FEATURES);
        this.context = context;
    }

    /**
     * collects device features
     *
     * @param reportField   the ReportField to collect
     * @param reportBuilder the current reportBuilder
     * @return Element of all device feature names
     */
    @NonNull
    @Override
    Element collect(ReportField reportField, ReportBuilder reportBuilder) {
        final ComplexElement result = new ComplexElement();
        try {
            final PackageManager pm = context.getPackageManager();
            final FeatureInfo[] features = pm.getSystemAvailableFeatures();
            for (final FeatureInfo feature : features) {
                final String featureName = feature.name;
                if (featureName != null) {
                    result.put(featureName, true);
                } else {
                    result.put("glEsVersion", feature.getGlEsVersion());
                }
            }
        } catch (Throwable e) {
            ACRA.log.w(LOG_TAG, "Couldn't retrieve DeviceFeatures for " + context.getPackageName(), e);
        }

        return result;
    }
}
