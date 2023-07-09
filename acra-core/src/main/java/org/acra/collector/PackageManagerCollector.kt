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

import android.content.Context
import com.google.auto.service.AutoService
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.util.PackageManagerWrapper
import org.acra.util.versionCodeLong

/**
 * Collects [android.content.pm.PackageInfo] values
 *
 * @author F43nd1r
 * @since 4.9.1
 */
@AutoService(Collector::class)
class PackageManagerCollector : BaseReportFieldCollector(ReportField.APP_VERSION_NAME, ReportField.APP_VERSION_CODE) {
    @Throws(CollectorException::class)
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        val info = PackageManagerWrapper(context).getPackageInfo()
        if (info == null) {
            throw CollectorException("Failed to get package info")
        } else {
            when (reportField) {
                ReportField.APP_VERSION_NAME -> target.put(ReportField.APP_VERSION_NAME, info.versionName)
                ReportField.APP_VERSION_CODE -> target.put(ReportField.APP_VERSION_CODE, info.versionCodeLong)
                else -> throw IllegalArgumentException()
            }
        }
    }
}