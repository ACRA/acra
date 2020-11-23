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
package org.acra.config

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.auto.service.AutoService
import org.acra.builder.ReportBuilder
import org.acra.config.ConfigUtils.getPluginConfiguration
import org.acra.config.LimiterData.Companion.load
import org.acra.config.LimiterData.ReportMetadata
import org.acra.data.CrashReportData
import org.acra.file.ReportLocator
import org.acra.log.debug
import org.acra.log.warn
import org.acra.plugins.HasConfigPlugin
import org.acra.util.ToastSender.sendToast
import org.json.JSONException
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

/**
 * @author F43nd1r
 * @since 26.10.2017
 */
@AutoService(ReportingAdministrator::class)
class LimitingReportAdministrator : HasConfigPlugin(LimiterConfiguration::class.java), ReportingAdministrator {
    override fun shouldStartCollecting(context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder): Boolean {
        try {
            val limiterConfiguration = getPluginConfiguration(config, LimiterConfiguration::class.java)
            val reportLocator = ReportLocator(context)
            if (reportLocator.approvedReports.size + reportLocator.unapprovedReports.size >= limiterConfiguration.failedReportLimit) {
                debug { "Reached failedReportLimit, not collecting" }
                return false
            }
            val reportMetadata = loadLimiterData(context, limiterConfiguration).reportMetadata
            if (reportMetadata.size >= limiterConfiguration.overallLimit) {
                debug { "Reached overallLimit, not collecting" }
                return false
            }
        } catch (e: IOException) {
            warn(e) { "Failed to load LimiterData" }
        }
        return true
    }

    override fun shouldSendReport(context: Context, config: CoreConfiguration, crashReportData: CrashReportData): Boolean {
        try {
            val limiterConfiguration = getPluginConfiguration(config, LimiterConfiguration::class.java)
            val limiterData = loadLimiterData(context, limiterConfiguration)
            var sameTrace = 0
            var sameClass = 0
            val m = ReportMetadata(crashReportData)
            for (metadata in limiterData.reportMetadata) {
                if (m.stacktrace == metadata.stacktrace) {
                    sameTrace++
                }
                if (m.exceptionClass == metadata.exceptionClass) {
                    sameClass++
                }
            }
            if (sameTrace >= limiterConfiguration.stacktraceLimit) {
                debug { "Reached stacktraceLimit, not sending" }
                return false
            }
            if (sameClass >= limiterConfiguration.exceptionClassLimit) {
                debug { "Reached exceptionClassLimit, not sending" }
                return false
            }
            limiterData.reportMetadata.add(m)
            limiterData.store(context)
        } catch (e: IOException) {
            warn(e) { "Failed to load LimiterData" }
        } catch (e: JSONException) {
            warn(e) { "Failed to load LimiterData" }
        }
        return true
    }

    override fun notifyReportDropped(context: Context, config: CoreConfiguration) {
        val limiterConfiguration = getPluginConfiguration(config, LimiterConfiguration::class.java)
        if (limiterConfiguration.ignoredCrashToast.isNotEmpty()) {
            val future = Executors.newSingleThreadExecutor().submit {
                Looper.prepare()
                sendToast(context, limiterConfiguration.ignoredCrashToast, Toast.LENGTH_LONG)
                val looper = Looper.myLooper()
                if (looper != null) {
                    Handler(looper).postDelayed({
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            looper.quitSafely()
                        } else {
                            looper.quit()
                        }
                    }, 4000)
                    Looper.loop()
                }
            }
            while (!future.isDone) {
                try {
                    future.get()
                } catch (ignored: InterruptedException) {
                } catch (e: ExecutionException) {
                    //ReportInteraction crashed, so ignore it
                    break
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun loadLimiterData(context: Context, limiterConfiguration: LimiterConfiguration): LimiterData {
        val limiterData = load(context)
        val keepAfter = Calendar.getInstance()
        keepAfter.add(Calendar.MINUTE, (-limiterConfiguration.periodUnit.toMinutes(limiterConfiguration.period)).toInt())
        debug { "purging reports older than ${keepAfter.time}" }
        limiterData.purgeOldData(keepAfter)
        limiterData.store(context)
        return limiterData
    }
}