/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acra.data

import android.content.Context
import org.acra.builder.ReportBuilder
import org.acra.collector.ApplicationStartupCollector
import org.acra.collector.Collector
import org.acra.collector.CollectorException
import org.acra.config.CoreConfiguration
import org.acra.log.debug
import org.acra.log.warn
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Responsible for collecting the CrashReportData for an Exception.
 *
 * @author F43nd1r
 * @since 4.3.0
 */
class CrashReportDataFactory(private val context: Context, private val config: CoreConfiguration) {
    private val collectors: List<Collector> = config.pluginLoader.loadEnabled(config, Collector::class.java).sortedBy { it.safeOrder }

    private val Collector.safeOrder
        get() = try {
            order
        } catch (t: Exception) {
            Collector.Order.NORMAL
        }

    /**
     * Collects crash data.
     *
     * @param builder ReportBuilder for whom to crete the crash report.
     * @return CrashReportData identifying the current crash.
     */
    fun createCrashData(builder: ReportBuilder): CrashReportData {
        val executorService = if (config.parallel) Executors.newCachedThreadPool() else Executors.newSingleThreadExecutor()
        val crashReportData = CrashReportData()
        collectors.groupBy { it.safeOrder }.toSortedMap().forEach { (order, collectors) ->
            debug { "Starting collectors with priority ${order.name}" }
            collect(collectors, executorService, builder, crashReportData)
            debug { "Finished collectors with priority ${order.name}" }
        }
        return crashReportData
    }

    private fun collect(collectors: List<Collector>, executorService: ExecutorService, builder: ReportBuilder, crashReportData: CrashReportData) {
        val futures = collectors.map { collector ->
            executorService.submit {
                //catch absolutely everything possible here so no collector obstructs the others
                try {
                    debug { "Calling collector ${collector.javaClass.name}" }
                    collector.collect(context, config, builder, crashReportData)
                    debug { "Collector ${collector.javaClass.name} completed" }
                } catch (e: CollectorException) {
                    warn(e) { "" }
                } catch (t: Throwable) {
                    warn(t) { "Error in collector ${collector.javaClass.simpleName}" }
                }
            }
        }
        for (future in futures) {
            while (!future.isDone) {
                try {
                    future.get()
                } catch (ignored: InterruptedException) {
                } catch (e: ExecutionException) {
                    break
                }
            }
        }
    }

    fun collectStartUp() {
        for (collector in collectors) {
            if (collector is ApplicationStartupCollector) {
                //catch absolutely everything possible here so no collector obstructs the others
                try {
                    collector.collectApplicationStartUp(context, config)
                } catch (t: Throwable) {
                    warn(t) { "${collector.javaClass.simpleName} failed to collect its startup data" }
                }
            }
        }
    }

}