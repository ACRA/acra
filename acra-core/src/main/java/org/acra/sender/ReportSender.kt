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
package org.acra.sender

import android.content.Context
import android.os.Bundle
import org.acra.annotation.OpenAPI
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.log.debug
import org.acra.plugins.loadEnabled

/**
 * A simple interface for defining various crash report senders.
 *
 * @author Kevin Gaudin
 */
@OpenAPI
interface ReportSender {
    /**
     * Send crash report data.
     *
     *
     * Method will be called from the [ReportDistributor].
     *
     * @param context      Android Context in which to send the crash report.
     * @param errorContent Stores key/value pairs for each report field.
     * @throws ReportSenderException If anything goes fatally wrong during the handling of crash data, you can (should) throw a [ReportSenderException] with a custom message.
     */
    @JvmDefault
    @Throws(ReportSenderException::class)
    fun send(context: Context, errorContent: CrashReportData) {
    }

    /**
     * Send crash report data.
     *
     *
     * Method will be called from the [ReportDistributor].
     *
     * @param context      Android Context in which to send the crash report.
     * @param errorContent Stores key/value pairs for each report field.
     * @param extras       additional information set in a [org.acra.scheduler.DefaultSenderScheduler]
     * @throws ReportSenderException If anything goes fatally wrong during the handling of crash data, you can (should) throw a [ReportSenderException] with a custom message.
     */
    @JvmDefault
    @Throws(ReportSenderException::class)
    fun send(context: Context, errorContent: CrashReportData, extras: Bundle) = send(context, errorContent)

    @JvmDefault
    fun requiresForeground(): Boolean {
        return false
    }

    companion object {
        fun loadSenders(context: Context, config: CoreConfiguration): List<ReportSender> {
            debug { "Using PluginLoader to find ReportSender factories" }
            val factories: List<ReportSenderFactory> = config.pluginLoader.loadEnabled(config)
            debug { "reportSenderFactories : $factories" }
            return factories.map { it.create(context, config).also { debug { "Adding reportSender : $it" } } }
        }

        fun hasForegroundSenders(context: Context, config: CoreConfiguration) = loadSenders(context, config).any { it.requiresForeground() }

        fun hasBackgroundSenders(context: Context, config: CoreConfiguration) = loadSenders(context, config).any { !it.requiresForeground() }
    }
}