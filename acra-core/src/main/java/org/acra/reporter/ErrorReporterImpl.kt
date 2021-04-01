/*
 *  Copyright 2010 Emmanuel Astier &amp; Kevin Gaudin
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
package org.acra.reporter

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import org.acra.ACRA
import org.acra.ErrorReporter
import org.acra.builder.LastActivityManager
import org.acra.builder.ReportBuilder
import org.acra.builder.ReportExecutor
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportDataFactory
import org.acra.log.debug
import org.acra.log.error
import org.acra.log.info
import org.acra.log.warn
import org.acra.prefs.SharedPreferencesFactory
import org.acra.scheduler.SchedulerStarter
import org.acra.scheduler.SenderScheduler
import org.acra.startup.StartupProcessorExecutor
import org.acra.util.ProcessFinisher
import java.util.*

/**
 *
 *
 * The ErrorReporter is a Singleton object in charge of collecting crash context
 * data and sending crash reports. It registers itself as the Application's
 * Thread default [Thread.UncaughtExceptionHandler].
 *
 *
 *
 * When a crash occurs, it collects data of the crash context (device, system,
 * stack trace...) and writes a report file in the application private
 * directory, which may then be sent.
 *
 * @param context                        Context for the application in which ACRA is running.
 * @param config                         AcraConfig to use when reporting and sending errors.
 * @param enabled                        Whether this ErrorReporter should capture Exceptions and forward their reports.
 * @param supportedAndroidVersion        the minimal supported version
 * @param checkReportsOnApplicationStart If reports should be checked on startup
 */
class ErrorReporterImpl(private val context: Application, config: CoreConfiguration,
                        enabled: Boolean, private val supportedAndroidVersion: Boolean, checkReportsOnApplicationStart: Boolean) : Thread.UncaughtExceptionHandler,
        OnSharedPreferenceChangeListener, ErrorReporter {
    private val reportExecutor: ReportExecutor
    private val customData: MutableMap<String, String> = HashMap()
    private val schedulerStarter: SchedulerStarter
    private val defaultExceptionHandler: Thread.UncaughtExceptionHandler

    override fun putCustomData(key: String, value: String): String? {
        return customData.put(key, value)
    }

    override fun removeCustomData(key: String): String? {
        return customData.remove(key)
    }

    override fun clearCustomData() {
        customData.clear()
    }

    override fun getCustomData(key: String): String? {
        return customData[key]
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        // If we're not enabled then just pass the Exception on to the defaultExceptionHandler.
        if (!reportExecutor.isEnabled) {
            reportExecutor.handReportToDefaultExceptionHandler(t, e)
            return
        }
        try {
            error(e) { "ACRA caught a ${e.javaClass.simpleName} for ${context.packageName}" }
            debug { "Building report" }

            // Generate and send crash report
            ReportBuilder()
                    .uncaughtExceptionThread(t)
                    .exception(e)
                    .customData(customData)
                    .endApplication()
                    .build(reportExecutor)
        } catch (fatality: Exception) {
            // ACRA failed. Prevent any recursive call to ACRA.uncaughtException(), let the native reporter do its job.
            error(fatality) { "ACRA failed to capture the error - handing off to native error reporter" }
            reportExecutor.handReportToDefaultExceptionHandler(t, e)
        }
    }

    override fun handleSilentException(e: Throwable) {
        ReportBuilder()
                .exception(e)
                .customData(customData)
                .sendSilently()
                .build(reportExecutor)
    }

    override fun setEnabled(enabled: Boolean) {
        if (supportedAndroidVersion) {
            info { "ACRA is ${if (enabled) "enabled" else "disabled"} for ${context.packageName}" }
            reportExecutor.isEnabled = enabled
        } else {
            warn { "ACRA requires ICS or greater. ACRA is disabled and will NOT catch crashes or send messages." }
        }
    }

    override fun handleException(e: Throwable, endApplication: Boolean) {
        val builder = ReportBuilder()
        builder.exception(e)
                .customData(customData)
        if (endApplication) {
            builder.endApplication()
        }
        builder.build(reportExecutor)
    }

    override fun handleException(e: Throwable) {
        handleException(e, false)
    }

    override val reportScheduler: SenderScheduler
        get() = schedulerStarter.senderScheduler

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (ACRA.PREF_DISABLE_ACRA == key || ACRA.PREF_ENABLE_ACRA == key) {
            setEnabled(SharedPreferencesFactory.shouldEnableACRA(sharedPreferences))
        }
    }

    fun unregister() {
        Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler)
    }

    /**
     */
    init {
        val crashReportDataFactory = CrashReportDataFactory(context, config)
        crashReportDataFactory.collectStartUp()
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()!!
        Thread.setDefaultUncaughtExceptionHandler(this)
        val lastActivityManager = LastActivityManager(context)
        val processFinisher = ProcessFinisher(context, config, lastActivityManager)
        schedulerStarter = SchedulerStarter(context, config)
        reportExecutor = ReportExecutor(context, config, crashReportDataFactory, defaultExceptionHandler, processFinisher, schedulerStarter, lastActivityManager)
        reportExecutor.isEnabled = enabled

        // Check for approved reports and send them (if enabled).
        if (checkReportsOnApplicationStart) {
            StartupProcessorExecutor(context, config, schedulerStarter).processReports(enabled)
        }
    }
}