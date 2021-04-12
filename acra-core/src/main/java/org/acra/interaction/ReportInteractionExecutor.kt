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
package org.acra.interaction

import android.content.Context
import org.acra.config.CoreConfiguration
import org.acra.log.debug
import org.acra.plugins.loadEnabled
import java.io.File
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Manages and executes all report interactions
 *
 * @author F43nd1r
 * @since 10.10.2017
 */
class ReportInteractionExecutor(private val context: Context, private val config: CoreConfiguration) {
    private val reportInteractions: List<ReportInteraction> = config.pluginLoader.loadEnabled(config)
    fun hasInteractions(): Boolean = reportInteractions.isNotEmpty()

    fun performInteractions(reportFile: File): Boolean {
        val executorService = Executors.newCachedThreadPool()
        val futures: List<Future<Boolean>> = reportInteractions.map {
            executorService.submit<Boolean> {
                debug { "Calling ReportInteraction of class ${it.javaClass.name}" }
                it.performInteraction(context, config, reportFile)
            }
        }
        var sendReports = true
        for (future in futures) {
            do {
                try {
                    sendReports = sendReports and future.get()
                } catch (ignored: InterruptedException) {
                } catch (e: ExecutionException) {
                    //ReportInteraction crashed, so ignore it
                    break
                }
            } while (!future.isDone)
        }
        return sendReports
    }

}