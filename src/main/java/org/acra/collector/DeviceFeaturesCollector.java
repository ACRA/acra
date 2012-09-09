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

import static org.acra.ACRA.LOG_TAG;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Features declared as available on the device. Available only with API level > 5.
 * 
 * @author Kevin Gaudin
 * 
 */
final class DeviceFeaturesCollector {

    public static String getFeatures(Context ctx) {

        if (Compatibility.getAPILevel() < 5) {
            return "Data available only with API Level >= 5";
        }

        final StringBuilder result = new StringBuilder();
        try {
            final PackageManager pm = ctx.getPackageManager();
            final Method getSystemAvailableFeatures = PackageManager.class.getMethod("getSystemAvailableFeatures", (Class[]) null);
            final Object[] features = (Object[]) getSystemAvailableFeatures.invoke(pm);
            for (final Object feature : features) {
                final String featureName = (String) feature.getClass().getField("name").get(feature);
                if(featureName != null) {
                    result.append(featureName);
                } else {
                    final Method getGlEsVersion = feature.getClass().getMethod("getGlEsVersion", (Class[]) null);
                    final String glEsVersion = (String) getGlEsVersion.invoke(feature);
                    result.append("glEsVersion = ");
                    result.append(glEsVersion);
                }
                result.append("\n");
            }
        } catch (Throwable e) {
            Log.w(LOG_TAG, "Couldn't retrieve DeviceFeatures for " + ctx.getPackageName(), e);
            result.append("Could not retrieve data: ");
            result.append(e.getMessage());
        }

        return result.toString();
    }
}
