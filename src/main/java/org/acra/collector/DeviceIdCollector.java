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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.util.PackageManagerWrapper;

import java.util.Set;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects the device ID
 *
 * @author F43nd1r
 * @since 4.9.1
 */
final class DeviceIdCollector extends Collector {
    private final Context context;
    private final PackageManagerWrapper pm;
    private final SharedPreferences prefs;

    DeviceIdCollector(Context context, PackageManagerWrapper pm, SharedPreferences prefs) {
        super(ReportField.DEVICE_ID);
        this.context = context;
        this.pm = pm;
        this.prefs = prefs;
    }

    @Override
    boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return super.shouldCollect(crashReportFields, collect, reportBuilder) && prefs.getBoolean(ACRA.PREF_ENABLE_DEVICE_ID, true)
                && pm.hasPermission(Manifest.permission.READ_PHONE_STATE);
    }

    @NonNull
    @Override
    String collect(ReportField reportField, ReportBuilder reportBuilder) {
        String result = getDeviceId();
        return result != null ? result : ACRAConstants.NOT_AVAILABLE;
    }

    /**
     * Returns the DeviceId according to the TelephonyManager.
     *
     * @return Returns the DeviceId according to the TelephonyManager or null if there is no TelephonyManager.
     */
    @SuppressLint("HardwareIds")
    @Nullable
    private String getDeviceId() {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (RuntimeException e) {
            ACRA.log.w(LOG_TAG, "Couldn't retrieve DeviceId for : " + context.getPackageName(), e);
            return null;
        }
    }
}
