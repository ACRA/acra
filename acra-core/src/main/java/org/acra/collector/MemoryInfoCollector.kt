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

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import com.google.auto.service.AutoService
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.log.error
import org.acra.util.StreamReader
import java.io.IOException
import java.util.*

/**
 * Collects results of the `dumpsys` command.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector::class)
class MemoryInfoCollector : BaseReportFieldCollector(ReportField.DUMPSYS_MEMINFO, ReportField.TOTAL_MEM_SIZE, ReportField.AVAILABLE_MEM_SIZE) {
    override fun shouldCollect(context: Context, config: CoreConfiguration, collect: ReportField, reportBuilder: ReportBuilder): Boolean {
        return super.shouldCollect(context, config, collect, reportBuilder) && reportBuilder.exception !is OutOfMemoryError
    }

    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        when (reportField) {
            ReportField.DUMPSYS_MEMINFO -> target.put(ReportField.DUMPSYS_MEMINFO, collectMemInfo())
            ReportField.TOTAL_MEM_SIZE -> target.put(ReportField.TOTAL_MEM_SIZE, getTotalInternalMemorySize())
            ReportField.AVAILABLE_MEM_SIZE -> target.put(ReportField.AVAILABLE_MEM_SIZE, getAvailableInternalMemorySize())
            else -> throw IllegalArgumentException()
        }
    }

    /**
     * Collect results of the `dumpsys meminfo` command restricted to this application process.
     *
     * @return The execution result.
     */
    private fun collectMemInfo(): String? {
        return try {
            val commandLine: MutableList<String> = ArrayList()
            commandLine.add("dumpsys")
            commandLine.add("meminfo")
            commandLine.add(Process.myPid().toString())
            val process = Runtime.getRuntime().exec(commandLine.toTypedArray())
            StreamReader(process.inputStream).read()
        } catch (e: IOException) {
            error(e) { "MemoryInfoCollector.meminfo could not retrieve data"}
            null
        }
    }

    /**
     * Calculates the free memory of the device. This is based on an inspection of the filesystem, which in android
     * devices is stored in RAM.
     *
     * @return Number of bytes available.
     */
    @Suppress("DEPRECATION")
    private fun getAvailableInternalMemorySize(): Long {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long
            val availableBlocks: Long
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.blockSizeLong
                availableBlocks = stat.availableBlocksLong
            } else {
                blockSize = stat.blockSize.toLong()
                availableBlocks = stat.availableBlocks.toLong()
            }
            return availableBlocks * blockSize
        }

    /**
     * Calculates the total memory of the device. This is based on an inspection of the filesystem, which in android devices is stored in RAM.
     *
     * @return Total number of bytes.
     */
    @Suppress("DEPRECATION")
    private fun getTotalInternalMemorySize(): Long {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long
            val totalBlocks: Long
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.blockSizeLong
                totalBlocks = stat.blockCountLong
            } else {
                blockSize = stat.blockSize.toLong()
                totalBlocks = stat.blockCount.toLong()
            }
            return totalBlocks * blockSize
        }
}