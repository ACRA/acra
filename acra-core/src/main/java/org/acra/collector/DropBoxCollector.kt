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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.DropBoxManager
import com.google.auto.service.AutoService
import org.acra.ACRA
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.log.warn
import org.acra.prefs.SharedPreferencesFactory
import org.acra.util.PackageManagerWrapper
import org.acra.util.SystemServices.getDropBoxManager
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Collects data from the [DropBoxManager]. A set of DropBox tags have been identified in the Android source code.
 * We collect data associated to these tags with hope that some of them could help debugging applications.
 * Application specific tags can be provided by the app dev to track his own usage of the DropBoxManager.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector::class)
class DropBoxCollector : BaseReportFieldCollector(ReportField.DROPBOX) {
    private val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault()) //iCal format (used for backwards compatibility)
    override val order: Collector.Order
        get() = Collector.Order.FIRST

    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        val dropbox = getDropBoxManager(context)
        val calendar = Calendar.getInstance()
        calendar.roll(Calendar.MINUTE, -config.dropboxCollectionMinutes)
        val time = calendar.timeInMillis
        dateFormat.format(calendar.time)
        val tags: MutableList<String> = ArrayList()
        if (config.includeDropBoxSystemTags) {
            tags.addAll(SYSTEM_TAGS)
        }
        val additionalTags: List<String> = config.additionalDropBoxTags.toList()
        if (additionalTags.isNotEmpty()) {
            tags.addAll(additionalTags)
        }
        if (tags.isNotEmpty()) {
            val dropboxContent = JSONObject()
            for (tag in tags) {
                val builder = StringBuilder()
                var entry = dropbox.getNextEntry(tag, time)
                if (entry == null) {
                    builder.append("Nothing.").append('\n')
                    continue
                }
                while (entry != null) {
                    val msec = entry.timeMillis
                    calendar.timeInMillis = msec
                    builder.append('@').append(dateFormat.format(calendar.time)).append('\n')
                    val text = entry.getText(500)
                    if (text != null) {
                        builder.append("Text: ").append(text).append('\n')
                    } else {
                        builder.append("Not Text!").append('\n')
                    }
                    entry.close()
                    entry = dropbox.getNextEntry(tag, msec)
                }
                try {
                    dropboxContent.put(tag, builder.toString())
                } catch (e: JSONException) {
                    warn(e) { "Failed to collect data for tag $tag" }
                }
            }
            target.put(ReportField.DROPBOX, dropboxContent)
        }
    }

    override fun shouldCollect(context: Context, config: CoreConfiguration, collect: ReportField, reportBuilder: ReportBuilder): Boolean {
        return (super.shouldCollect(context, config, collect, reportBuilder) &&
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN || PackageManagerWrapper(context).hasPermission(Manifest.permission.READ_LOGS))
                && SharedPreferencesFactory(context, config).create().getBoolean(ACRA.PREF_ENABLE_SYSTEM_LOGS, true))
    }

    companion object {
        private val SYSTEM_TAGS = arrayOf("system_app_anr", "system_app_wtf", "system_app_crash",
                "system_server_anr", "system_server_wtf", "system_server_crash", "BATTERY_DISCHARGE_INFO",
                "SYSTEM_RECOVERY_LOG", "SYSTEM_BOOT", "SYSTEM_LAST_KMSG", "APANIC_CONSOLE", "APANIC_THREADS",
                "SYSTEM_RESTART", "SYSTEM_TOMBSTONE", "data_app_strictmode")
    }
}