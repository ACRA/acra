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

import org.acra.ACRA
import org.acra.ACRAConstants
import org.acra.ReportField
import org.acra.log.warn
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Stores a crash report data
 */
class CrashReportData {
    private val content: JSONObject

    constructor() {
        content = JSONObject()
    }

    constructor(json: String) {
        content = JSONObject(json)
    }

    @Synchronized
    fun put(key: String, value: Boolean) {
        try {
            content.put(key, value)
        } catch (e: JSONException) {
            warn {  "Failed to put value into CrashReportData: $value" }
        }
    }

    @Synchronized
    fun put(key: String, value: Double) {
        try {
            content.put(key, value)
        } catch (e: JSONException) {
            warn {  "Failed to put value into CrashReportData: $value" }
        }
    }

    @Synchronized
    fun put(key: String, value: Int) {
        try {
            content.put(key, value)
        } catch (e: JSONException) {
            warn {  "Failed to put value into CrashReportData: $value" }
        }
    }

    @Synchronized
    fun put(key: String, value: Long) {
        try {
            content.put(key, value)
        } catch (e: JSONException) {
            warn {  "Failed to put value into CrashReportData: $value" }
        }
    }

    @Synchronized
    fun put(key: String, value: String?) {
        if (value == null) {
            putNA(key)
            return
        }
        try {
            content.put(key, value)
        } catch (e: JSONException) {
            warn {  "Failed to put value into CrashReportData: $value" }
        }
    }

    @Synchronized
    fun put(key: String, value: JSONObject?) {
        if (value == null) {
            putNA(key)
            return
        }
        try {
            content.put(key, value)
        } catch (e: JSONException) {
            warn {  "Failed to put value into CrashReportData: $value" }
        }
    }

    @Synchronized
    fun put(key: String, value: JSONArray?) {
        if (value == null) {
            putNA(key)
            return
        }
        try {
            content.put(key, value)
        } catch (e: JSONException) {
            warn {  "Failed to put value into CrashReportData: $value" }
        }
    }

    @Synchronized
    fun put(key: ReportField, value: Boolean) {
        put(key.toString(), value)
    }

    @Synchronized
    fun put(key: ReportField, value: Double) {
        put(key.toString(), value)
    }

    @Synchronized
    fun put(key: ReportField, value: Int) {
        put(key.toString(), value)
    }

    @Synchronized
    fun put(key: ReportField, value: Long) {
        put(key.toString(), value)
    }

    @Synchronized
    fun put(key: ReportField, value: String?) {
        put(key.toString(), value)
    }

    @Synchronized
    fun put(key: ReportField, value: JSONObject?) {
        put(key.toString(), value)
    }

    @Synchronized
    fun put(key: ReportField, value: JSONArray?) {
        put(key.toString(), value)
    }

    private fun putNA(key: String) {
        try {
            content.put(key, ACRAConstants.NOT_AVAILABLE)
        } catch (ignored: JSONException) {
        }
    }

    /**
     * Returns the property with the specified key.
     *
     * @param key the key of the property to find.
     * @return the keyd property value, or `null` if it can't be found.
     */
    fun getString(key: ReportField): String? {
        return content.optString(key.toString())
    }

    operator fun get(key: String): Any? {
        return content.opt(key)
    }

    fun containsKey(key: String): Boolean {
        return content.has(key)
    }

    fun containsKey(key: ReportField): Boolean {
        return containsKey(key.toString())
    }

    @Throws(JSONException::class)
    fun toJSON(): String {
        return try {
            StringFormat.JSON.toFormattedString(this, emptyList(), "", "", false)
        } catch (e: JSONException) {
            throw e
        } catch (e: Exception) {
            throw JSONException(e.message)
        }
    }

    fun toMap(): Map<String, Any?> {
        return content.keys().asSequence().map { it to get(it) }.toMap()
    }
}