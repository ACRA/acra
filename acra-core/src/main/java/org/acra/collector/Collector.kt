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
package org.acra.collector

import android.content.Context
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.plugins.Plugin

/**
 * @author F43nd1r
 * @since 29.09.2017
 */
interface Collector : Plugin {
    /**
     * Execute collection
     *
     * @param context         a context
     * @param config          current Configuration
     * @param reportBuilder   current ReportBuilder
     * @param crashReportData put results here
     * @throws CollectorException if collection failed
     */
    @Throws(CollectorException::class)
    fun collect(context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, crashReportData: CrashReportData)

    /**
     * @return when this collector should be called compared to other collectors
     */
    @JvmDefault
    val order: Order
        get() = Order.NORMAL

    enum class Order {
        FIRST, EARLY, NORMAL, LATE, LAST
    }
}