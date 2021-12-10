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
package org.acra

import android.app.Application
import android.os.Build
import org.acra.config.ACRAConfigurationException
import org.acra.config.CoreConfiguration
import org.acra.config.CoreConfigurationBuilder
import org.acra.log.ACRALog
import org.acra.log.AndroidLogDelegate
import org.acra.log.debug
import org.acra.log.info
import org.acra.log.warn
import org.acra.prefs.SharedPreferencesFactory
import org.acra.reporter.ErrorReporterImpl
import org.acra.util.StreamReader
import org.acra.util.StubCreator
import java.io.IOException

/**
 * Use this class to initialize the crash reporting feature using
 * [.init] as soon as possible in your [Application]
 * subclass [Application.onCreate] method. Configuration items must have
 * been set by using [org.acra.annotation.AcraCore] above the declaration of your
 * [Application] subclass.
 *
 * @author Kevin Gaudin
 */
@Suppress("MemberVisibilityCanBePrivate")
object ACRA {
    @JvmField
    var /*non-final*/  DEV_LOGGING = false // Should be false for release.

    @JvmField
    val LOG_TAG: String = ACRA::class.java.simpleName

    @JvmField
    var log: ACRALog = AndroidLogDelegate()
    private const val ACRA_PRIVATE_PROCESS_NAME = ":acra"

    /**
     * The key of the application default SharedPreference where you can put a
     * 'true' Boolean value to disable ACRA.
     */
    const val PREF_DISABLE_ACRA = "acra.disable"

    /**
     * Alternatively, you can use this key if you prefer your users to have the
     * checkbox ticked to enable crash reports. If both acra.disable and
     * acra.enable are set, the value of acra.disable takes over the other.
     */
    const val PREF_ENABLE_ACRA = "acra.enable"

    /**
     * The key of the SharedPreference allowing the user to disable sending
     * content of logcat/dropbox. System logs collection is also dependent of
     * the READ_LOGS permission.
     */
    const val PREF_ENABLE_SYSTEM_LOGS = "acra.syslog.enable"

    /**
     * The key of the SharedPreference allowing the user to disable sending his
     * device id. Device ID collection is also dependent of the READ_PHONE_STATE
     * permission.
     */
    const val PREF_ENABLE_DEVICE_ID = "acra.deviceid.enable"

    /**
     * The key of the SharedPreference allowing the user to always include his
     * email address.
     */
    const val PREF_USER_EMAIL_ADDRESS = "acra.user.email"

    /**
     * The key of the SharedPreference allowing the user to automatically accept
     * sending reports.
     */
    const val PREF_ALWAYS_ACCEPT = "acra.alwaysaccept"

    /**
     * The version number of the application the last time ACRA was started.
     * This is used to determine whether unsent reports should be discarded
     * because they are old and out of date.
     */
    const val PREF_LAST_VERSION_NR = "acra.lastVersionNr"

    /**
     * the current instance of ErrorReporter.
     * not available if [ACRA.init] has not yet been called.
     */
    @JvmStatic
    var errorReporter = StubCreator.createErrorReporterStub()
        private set

    /**
     *
     *
     * Initialize ACRA for a given Application.
     *
     *
     * The call to this method should be placed as soon as possible in the [Application.attachBaseContext]  method.
     *
     *
     * @param app                            Your Application class.
     * @param builder                        ConfigurationBuilder to manually set up ACRA configuration.
     * @param checkReportsOnApplicationStart Whether to invoke ErrorReporter.checkReportsOnApplicationStart().
     */
    @JvmOverloads
    @JvmStatic
    fun init(app: Application, builder: CoreConfigurationBuilder = CoreConfigurationBuilder(), checkReportsOnApplicationStart: Boolean = true) {
        try {
            init(app, builder.build(), checkReportsOnApplicationStart)
        } catch (e: ACRAConfigurationException) {
            warn(e) { "Configuration Error - ACRA not started." }
        }
    }

    /**
     *
     *
     * Initialize ACRA for a given Application. The call to this method should
     * be placed as soon as possible in the [Application.attachBaseContext]
     * method.
     *
     *
     * @param app                            Your Application class.
     * @param config                         CoreConfiguration to manually set up ACRA configuration.
     * @param checkReportsOnApplicationStart Whether to invoke ErrorReporter.checkReportsOnApplicationStart().
     * @throws IllegalStateException if it is called more than once.
     */
    @JvmOverloads
    @JvmStatic
    fun init(app: Application, config: CoreConfiguration, checkReportsOnApplicationStart: Boolean = true) {
        val senderServiceProcess = isACRASenderServiceProcess()
        if (senderServiceProcess) {
            debug {
                "Not initialising ACRA to listen for uncaught Exceptions as this is the SendWorker process and we only send reports, we don't capture them to avoid infinite loops"
            }
        }
        val supportedAndroidVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
        if (!supportedAndroidVersion) {
            // NB We keep initialising so that everything is configured. But ACRA is never enabled below.
            warn { "ACRA 5.1.0+ requires ICS or greater. ACRA is disabled and will NOT catch crashes or send messages." }
        }
        if (isInitialised) {
            warn { "ACRA#init called more than once. This might have unexpected side effects. Doing this outside of tests is discouraged." }
            debug { "Removing old ACRA config..." }
            (errorReporter as ErrorReporterImpl).unregister()
            errorReporter = StubCreator.createErrorReporterStub()
        }
        val prefs = SharedPreferencesFactory(app, config).create()
        if (!senderServiceProcess) {
            // Initialize ErrorReporter with all required data
            val enableAcra = supportedAndroidVersion && SharedPreferencesFactory.shouldEnableACRA(prefs)
            // Indicate that ACRA is or is not listening for crashes.
            info { "ACRA is ${if (enableAcra) "enabled" else "disabled"} for ${app.packageName}, initializing..." }
            val reporter = ErrorReporterImpl(app, config, enableAcra, supportedAndroidVersion, checkReportsOnApplicationStart)
            errorReporter = reporter

            // register after initAcra is called to avoid a
            // NPE in ErrorReporter.disable() because
            // the context could be null at this moment.
            prefs.registerOnSharedPreferenceChangeListener(reporter)
        }
    }

    /**
     * @return true is ACRA has been initialised.
     */
    val isInitialised: Boolean
        get() = errorReporter is ErrorReporterImpl

    /**
     * @return true if the current process is the process running the SenderService.
     * NB this assumes that your SenderService is configured to used the default ':acra' process.
     */
    @JvmStatic
    fun isACRASenderServiceProcess(): Boolean {
        val processName = currentProcessName()
        debug { "ACRA processName='$processName'" }
        //processName sometimes (or always?) starts with the package name, so we use endsWith instead of equals
        return processName != null && processName.endsWith(ACRA_PRIVATE_PROCESS_NAME)
    }

    private fun currentProcessName(): String? = try {
        StreamReader("/proc/self/cmdline").read().trim { it <= ' ' }
    } catch (e: IOException) {
        null
    }
}