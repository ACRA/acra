/*
 * Copyright (c) 2018
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
package org.acra.startup

import android.content.Context
import com.google.auto.service.AutoService
import org.acra.config.CoreConfiguration
import java.util.*

/**
 * @author lukas
 * @since 15.09.18
 */
@AutoService(StartupProcessor::class)
class UnapprovedStartupProcessor : StartupProcessor {
    override fun processReports(context: Context, config: CoreConfiguration, reports: List<Report>) {
        if (config.deleteUnapprovedReportsOnApplicationStart) {
            val sort: MutableList<Report> = ArrayList()
            for (report in reports) {
                if (!report.approved) {
                    sort.add(report)
                }
            }
            if (sort.isNotEmpty()) {
                sort.sortBy { it.file.lastModified() }
                for (i in 0 until sort.size - 1) {
                    sort[i].delete = true
                }
                sort[sort.size - 1].approve = true
            }
        }
    }
}