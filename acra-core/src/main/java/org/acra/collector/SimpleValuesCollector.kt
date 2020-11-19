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
import android.os.Build
import com.google.auto.service.AutoService
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.util.Installation.id
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * Collects various simple values
 *
 * @author F43nd1r
 * @since 4.9.1
 */
@AutoService(Collector::class)
class SimpleValuesCollector : BaseReportFieldCollector(ReportField.IS_SILENT, ReportField.REPORT_ID, ReportField.INSTALLATION_ID,
        ReportField.PACKAGE_NAME, ReportField.PHONE_MODEL, ReportField.ANDROID_VERSION,
        ReportField.BRAND, ReportField.PRODUCT, ReportField.FILE_PATH, ReportField.USER_IP) {
    @Throws(Exception::class)
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        when (reportField) {
            ReportField.IS_SILENT -> target.put(ReportField.IS_SILENT, reportBuilder.isSendSilently)
            ReportField.REPORT_ID -> target.put(ReportField.REPORT_ID, UUID.randomUUID().toString())
            ReportField.INSTALLATION_ID -> target.put(ReportField.INSTALLATION_ID, id(context))
            ReportField.PACKAGE_NAME -> target.put(ReportField.PACKAGE_NAME, context.packageName)
            ReportField.PHONE_MODEL -> target.put(ReportField.PHONE_MODEL, Build.MODEL)
            ReportField.ANDROID_VERSION -> target.put(ReportField.ANDROID_VERSION, Build.VERSION.RELEASE)
            ReportField.BRAND -> target.put(ReportField.BRAND, Build.BRAND)
            ReportField.PRODUCT -> target.put(ReportField.PRODUCT, Build.PRODUCT)
            ReportField.FILE_PATH -> target.put(ReportField.FILE_PATH, getApplicationFilePath(context))
            ReportField.USER_IP -> target.put(ReportField.USER_IP, getLocalIpAddress())
            else -> throw IllegalArgumentException()
        }
    }

    override fun shouldCollect(context: Context, config: CoreConfiguration, collect: ReportField, reportBuilder: ReportBuilder): Boolean {
        return collect == ReportField.IS_SILENT || collect == ReportField.REPORT_ID || super.shouldCollect(context, config, collect, reportBuilder)
    }

    private fun getApplicationFilePath(context: Context): String {
        return context.filesDir.absolutePath
    }

    companion object {
        @Throws(SocketException::class)
        private fun getLocalIpAddress(): String {
                val result = StringBuilder()
                var first = true
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            if (!first) {
                                result.append('\n')
                            }
                            result.append(inetAddress.hostAddress)
                            first = false
                        }
                    }
                }
                return result.toString()
            }
    }
}