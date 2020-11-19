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

import org.acra.ACRAConstants
import org.acra.ReportField
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * Represents possible report formats
 *
 * @author F43nd1r
 * @since 14.11.2017
 */
enum class StringFormat(val matchingHttpContentType: String) {
    JSON("application/json") {
        @Throws(JSONException::class)
        override fun toFormattedString(data: CrashReportData, order: List<ReportField>, mainJoiner: String, subJoiner: String, urlEncode: Boolean): String {
            val map = data.toMap().toMutableMap()
            val stringer = JSONStringer().`object`()
            for (field in order) {
                stringer.key(field.toString()).value(map.remove(field.toString()))
            }
            for ((key, value) in map) {
                stringer.key(key).value(value)
            }
            return stringer.endObject().toString()
        }
    },
    KEY_VALUE_LIST("application/x-www-form-urlencoded") {
        @Throws(UnsupportedEncodingException::class)
        override fun toFormattedString(data: CrashReportData, order: List<ReportField>, mainJoiner: String, subJoiner: String, urlEncode: Boolean): String {
            val map = toStringMap(data.toMap(), subJoiner).toMutableMap()
            val builder = StringBuilder()
            for (field in order) {
                append(builder, field.toString(), map.remove(field.toString()), mainJoiner, urlEncode)
            }
            for ((key, value) in map) {
                append(builder, key, value, mainJoiner, urlEncode)
            }
            return builder.toString()
        }

        @Throws(UnsupportedEncodingException::class)
        private fun append(builder: StringBuilder, key: String?, value: String?, joiner: String?, urlEncode: Boolean) {
            var k = key
            var v = value
            if (builder.isNotEmpty()) {
                builder.append(joiner)
            }
            if (urlEncode) {
                k = k?.let { URLEncoder.encode(it, ACRAConstants.UTF8) }
                v = v?.let { URLEncoder.encode(it, ACRAConstants.UTF8) }
            }
            builder.append(k).append('=').append(v)
        }

        private fun toStringMap(map: Map<String, Any?>, joiner: String): Map<String, String> {
            return map.mapValues { valueToString(joiner, it) }.toMap()
        }

        private fun valueToString(joiner: String, value: Any?): String {
            return if (value is JSONObject) {
                flatten(value).joinToString(joiner)
            } else {
                value.toString()
            }
        }

        private fun flatten(json: JSONObject): List<String> {
            return json.keys().asSequence().toList().flatMap { key ->
                val value: Any? = try {
                    json[key]
                } catch (e: JSONException) {
                    null
                }
                if (value is JSONObject) {
                    flatten(value).map { "$key.$it" }
                } else {
                    listOf("$key=$value")
                }
            }
        }
    };

    @Throws(Exception::class)
    abstract fun toFormattedString(data: CrashReportData, order: List<ReportField>, mainJoiner: String, subJoiner: String, urlEncode: Boolean): String
}