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
package org.acra.builder

import java.util.*

/**
 * Fluent API used to assemble the different options used for a crash report.
 *
 * @since 4.8.0
 */
class ReportBuilder {
    /**
     * @return the error message, or null if none is present
     */
    var message: String? = null
        private set

    /**
     * @return the Thread on which an uncaught Exception occurred, or null if none present
     */
    var uncaughtExceptionThread: Thread? = null
        private set

    /**
     * @return the exception, or null if none present
     */
    var exception: Throwable? = null
        private set
    private val customData: MutableMap<String, String> = HashMap()

    /**
     * @return if this should send silently
     */
    var isSendSilently = false
        private set

    /**
     * @return if this should stop the application after collecting
     */
    var isEndApplication = false
        private set

    /**
     * Set the error message to be reported.
     *
     * @param msg the error message
     * @return the updated `ReportBuilder`
     */
    fun message(msg: String?): ReportBuilder {
        message = msg
        return this
    }

    /**
     * Sets the Thread on which an uncaught Exception occurred.
     *
     * @param thread Thread on which an uncaught Exception occurred.
     * @return the updated `ReportBuilder`
     */
    fun uncaughtExceptionThread(thread: Thread?): ReportBuilder {
        uncaughtExceptionThread = thread
        return this
    }

    /**
     * Set the stack trace to be reported
     *
     * @param e The exception that should be associated with this report
     * @return the updated `ReportBuilder`
     */
    fun exception(e: Throwable?): ReportBuilder {
        exception = e
        return this
    }

    /**
     * Sets additional values to be added to [org.acra.ReportField.CUSTOM_DATA]. Values
     * specified here take precedence over globally specified custom data.
     *
     * @param customData a map of custom key-values to be attached to the report
     * @return the updated `ReportBuilder`
     */
    fun customData(customData: Map<String, String>): ReportBuilder {
        this.customData.putAll(customData)
        return this
    }

    /**
     * Sets an additional value to be added to [org.acra.ReportField.CUSTOM_DATA]. The value
     * specified here takes precedence over globally specified custom data.
     *
     * @param key   the key identifying the custom data
     * @param value the value for the custom data entry
     * @return the updated `ReportBuilder`
     */
    fun customData(key: String, value: String): ReportBuilder {
        customData[key] = value
        return this
    }

    /**
     * @return a map with all custom data
     */
    fun getCustomData(): Map<String, String> {
        return HashMap(customData)
    }

    /**
     * Forces the report to be sent silently, ignoring all interactions
     *
     * @return the updated `ReportBuilder`
     */
    fun sendSilently(): ReportBuilder {
        isSendSilently = true
        return this
    }

    /**
     * Ends the application after sending the crash report
     *
     * @return the updated `ReportBuilder`
     */
    fun endApplication(): ReportBuilder {
        isEndApplication = true
        return this
    }

    /**
     * Assembles and sends the crash report.
     *
     * @param reportExecutor ReportExecutor to use to build the report.
     */
    fun build(reportExecutor: ReportExecutor) {
        if (message == null && exception == null) {
            message = "Report requested by developer"
        }
        reportExecutor.execute(this)
    }
}