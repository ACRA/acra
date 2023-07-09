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
package org.acra.collector

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.auto.service.AutoService
import org.acra.ACRA
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.prefs.SharedPreferencesFactory
import org.acra.util.PackageManagerWrapper
import org.acra.util.SystemServices.getTelephonyManager

/**
 * Collects the device ID
 *
 * @author F43nd1r
 * @since 4.9.1
 */
@Suppress("DEPRECATION")
@AutoService(Collector::class)
class DeviceIdCollector : BaseReportFieldCollector(ReportField.DEVICE_ID) {
    override fun shouldCollect(context: Context, config: CoreConfiguration, collect: ReportField, reportBuilder: ReportBuilder): Boolean {
        return (super.shouldCollect(context, config, collect, reportBuilder) && SharedPreferencesFactory(context, config).create().getBoolean(ACRA.PREF_ENABLE_DEVICE_ID, true)
                && PackageManagerWrapper(context).hasPermission(Manifest.permission.READ_PHONE_STATE))
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @Throws(Exception::class)
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        val deviceId = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) getTelephonyManager(context).deviceId else null
        target.put(ReportField.DEVICE_ID, deviceId)
    }
}
