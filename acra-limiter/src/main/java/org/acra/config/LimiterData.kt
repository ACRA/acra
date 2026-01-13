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
import org.acra.ACRAConstants
import org.acra.ReportField
import org.acra.data.CrashReportData
import org.acra.log.warn
import org.acra.util.IOUtils.writeStringToFile
import org.acra.util.StreamReader
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author F43nd1r
 * @since 26.10.2017
 */
class LimiterData() {
    val reportMetadata: MutableList<ReportMetadata> = mutableListOf()

    private constructor(json: String?) : this() {
        if (json != null && json.isNotEmpty()) {
            val array = JSONArray(json)
            val length = array.length()
            for (i in 0 until length) {
                reportMetadata.add(ReportMetadata(array.optJSONObject(i)))
            }
        }
    }

    @Throws(IOException::class)
    fun store(context: Context) {
        writeStringToFile(context.getFileStreamPath(FILE_LIMITER_DATA), toJSON())
    }

    fun purgeOldData(keepAfter: Calendar) {
        reportMetadata.removeAll { keepAfter.after(it.timestamp) }
    }

    fun toJSON(): String {
        return JSONArray(reportMetadata).toString()
    }

    class ReportMetadata : JSONObject {
        internal constructor(crashReportData: CrashReportData) {
            val stacktrace = crashReportData.getString(ReportField.STACK_TRACE)
            put(KEY_STACK_TRACE, stacktrace)
            val index = stacktrace.indexOf('\n')
            val firstLine = if (index == -1) stacktrace else stacktrace.substring(0, index)
            val index2 = firstLine.indexOf(':')
            val className = if (index2 == -1) firstLine else firstLine.substring(0, index2)
            try {
                Class.forName(className)
                put(KEY_EXCEPTION_CLASS, className)
            } catch (ignored: ClassNotFoundException) {
            }
            put(KEY_TIMESTAMP, crashReportData.getString(ReportField.USER_CRASH_DATE))
        }

        internal constructor(copyFrom: JSONObject) : super(copyFrom, jsonArrayToList(copyFrom.names()))

        val stacktrace: String
            get() = optString(KEY_STACK_TRACE)
        val exceptionClass: String
            get() = optString(KEY_EXCEPTION_CLASS)
        val timestamp: Calendar?
            get() {
                val timestamp = optString(KEY_TIMESTAMP).takeIf { it.isNotEmpty() }
                if (timestamp != null) {
                    try {
                        val calendar = Calendar.getInstance()
                        calendar.time = SimpleDateFormat(ACRAConstants.DATE_TIME_FORMAT_STRING, Locale.ENGLISH).parse(timestamp)!!
                        return calendar
                    } catch (ignored: ParseException) {
                    }
                }
                return null
            }

        companion object {
            private const val KEY_STACK_TRACE = "stacktrace"
            private const val KEY_EXCEPTION_CLASS = "class"
            private const val KEY_TIMESTAMP = "timestamp"
        }
    }

    companion object {
        private const val FILE_LIMITER_DATA = "ACRA-limiter.json"

        @JvmStatic
        fun load(context: Context): LimiterData {
            return try {
                LimiterData(StreamReader(context.openFileInput(FILE_LIMITER_DATA)).read())
            } catch (e: FileNotFoundException) {
                LimiterData()
            } catch (e: IOException) {
                warn(e) { "Failed to load LimiterData" }
                LimiterData()
            } catch (e: JSONException) {
                warn(e) { "Failed to load LimiterData" }
                LimiterData()
            }
        }

        private fun jsonArrayToList(array: JSONArray?): Array<String> {
            val list: MutableList<String> = ArrayList()
            if (array != null) {
                val length = array.length()
                for (i in 0 until length) {
                    list.add(array.opt(i).toString())
                }
            }
            return list.toTypedArray()
        }
    }
}