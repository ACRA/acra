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
import android.content.Context
import android.os.Build
import android.os.Process
import com.google.auto.service.AutoService
import org.acra.ACRA
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.log.debug
import org.acra.prefs.SharedPreferencesFactory
import org.acra.util.PackageManagerWrapper
import org.acra.util.StreamReader
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Executes logcat commands and collects it's output.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector::class)
class LogCatCollector : BaseReportFieldCollector(ReportField.LOGCAT, ReportField.EVENTSLOG, ReportField.RADIOLOG) {
    override val order: Collector.Order
        get() = Collector.Order.FIRST

    /**
     * Executes the logcat command with arguments taken from [org.acra.annotation.AcraCore.logcatArguments]
     *
     * @param bufferName The name of the buffer to be read: "main" (default), "radio" or "events".
     * @return A string containing the latest lines of the output.
     * Default is 100 lines, use "-t", "300" in [org.acra.annotation.AcraCore.logcatArguments] if you want 300 lines.
     * You should be aware that increasing this value causes a longer report generation time and a bigger footprint on the device data plan consumption.
     */
    @Throws(IOException::class)
    private fun collectLogCat(config: CoreConfiguration, bufferName: String?): String {
        val myPid = Process.myPid()
        // no need to filter on jellybean onwards, android does that anyway
        val myPidStr = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && config.logcatFilterByPid && myPid > 0) "$myPid):" else null
        val commandLine: MutableList<String> = ArrayList()
        commandLine.add("logcat")
        if (bufferName != null) {
            commandLine.add("-b")
            commandLine.add(bufferName)
        }
        val tailCount: Int
        val logcatArgumentsList: List<String> = config.logcatArguments.toList()
        val tailIndex = logcatArgumentsList.indexOf("-t")
        tailCount = if (tailIndex > -1 && tailIndex < logcatArgumentsList.size) {
            logcatArgumentsList[tailIndex + 1].toInt()
        } else {
            -1
        }
        commandLine.addAll(logcatArgumentsList)
        val process = ProcessBuilder().command(commandLine).redirectErrorStream(true).start()
        debug { "Retrieving logcat output (buffer:${bufferName ?: "default"})..." }
        return try {
            streamToString(config, process.inputStream, myPidStr?.let { pid -> { it.contains(pid) } }, tailCount)
        } finally {
            process.destroy()
        }
    }

    override fun shouldCollect(context: Context, config: CoreConfiguration, collect: ReportField, reportBuilder: ReportBuilder): Boolean {
        return (super.shouldCollect(context, config, collect, reportBuilder) &&
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN || PackageManagerWrapper(context).hasPermission(Manifest.permission.READ_LOGS))
                && SharedPreferencesFactory(context, config).create().getBoolean(ACRA.PREF_ENABLE_SYSTEM_LOGS, true))
    }

    @Throws(IOException::class)
    override fun collect(
        reportField: ReportField,
        context: Context,
        config: CoreConfiguration,
        reportBuilder: ReportBuilder,
        target: CrashReportData
    ) {
        val bufferName: String? = when (reportField) {
            ReportField.LOGCAT -> null
            ReportField.EVENTSLOG -> "events"
            ReportField.RADIOLOG -> "radio"
            else -> throw IllegalArgumentException()
        }
        target.put(reportField, collectLogCat(config, bufferName))
    }

    /**
     * Reads an InputStream into a string respecting blocking settings.
     *
     * @param input  the stream
     * @param filter should return false for lines which should be excluded
     * @param limit  the maximum number of lines to read (the last x lines are kept)
     * @return the String that was read.
     * @throws IOException if the stream cannot be read.
     */
    @Throws(IOException::class)
    private fun streamToString(config: CoreConfiguration, input: InputStream, filter: ((String) -> Boolean)?, limit: Int): String {
        val reader = StreamReader(input).setFilter(filter).setLimit(limit)
        if (config.logcatReadNonBlocking) {
            reader.setTimeout(READ_TIMEOUT)
        }
        return reader.read()
    }

    companion object {
        private const val READ_TIMEOUT = 3000
    }
}