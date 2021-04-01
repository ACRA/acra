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
package org.acra.collector

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Secure
import com.google.auto.service.AutoService
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.log.warn
import org.json.JSONObject
import java.lang.Deprecated
import java.lang.reflect.Field
import kotlin.Boolean
import kotlin.Exception
import kotlin.IllegalArgumentException
import kotlin.String
import kotlin.Throws

/**
 * collects data from [System], [Global] and [Secure] Settings classes.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector::class)
class SettingsCollector : BaseReportFieldCollector(ReportField.SETTINGS_SYSTEM, ReportField.SETTINGS_SECURE, ReportField.SETTINGS_GLOBAL) {
    @Throws(Exception::class)
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        when (reportField) {
            ReportField.SETTINGS_SYSTEM -> target.put(ReportField.SETTINGS_SYSTEM, collectSettings(context, config, Settings.System::class.java))
            ReportField.SETTINGS_SECURE -> target.put(ReportField.SETTINGS_SECURE, collectSettings(context, config, Secure::class.java))
            ReportField.SETTINGS_GLOBAL -> target.put(
                    ReportField.SETTINGS_GLOBAL,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) collectSettings(context, config, Settings.Global::class.java) else null)
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(NoSuchMethodException::class)
    private fun collectSettings(context: Context, config: CoreConfiguration, settings: Class<*>): JSONObject {
        val result = JSONObject()
        val keys = settings.fields
        val getString = settings.getMethod("getString", ContentResolver::class.java, String::class.java)
        for (key in keys) {
            if (!key.isAnnotationPresent(Deprecated::class.java) && key.type == String::class.java && isAuthorized(config, key)) {
                try {
                    val value = getString.invoke(null, context.contentResolver, key[null])
                    if (value != null) {
                        result.put(key.name, value)
                    }
                } catch (e: Exception) {
                    warn(e) { ERROR }
                }
            }
        }
        return result
    }

    private fun isAuthorized(config: CoreConfiguration, key: Field?): Boolean {
        if (key == null || key.name.startsWith("WIFI_AP")) {
            return false
        }
        for (regex in config.excludeMatchingSettingsKeys) {
            if (key.name.matches(Regex(regex))) {
                return false
            }
        }
        return true
    }

    companion object {
        private const val ERROR = "Error: "
    }
}