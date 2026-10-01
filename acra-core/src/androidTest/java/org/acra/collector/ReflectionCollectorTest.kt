/*
 * Copyright (c) 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.collector

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.CrashReportData
import org.json.JSONArray
import org.json.JSONObject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReflectionCollectorTest {
    @Test
    fun buildConfigFieldsAreCollected() {
        val config = CoreConfigurationBuilder()
            .withBuildConfigClass(TestBuildConfig::class.java)
            .withReportContent(ReportField.BUILD_CONFIG)
            .build()
        val report = CrashReportData()

        ReflectionCollector().collect(ApplicationProvider.getApplicationContext(), config, ReportBuilder(), report)

        val buildConfig = report[ReportField.BUILD_CONFIG.name] as JSONObject
        assertThat(buildConfig.get("BOOLEAN"), equalTo(true))
        assertThat(buildConfig.get("BYTE"), equalTo(1.toByte()))
        assertThat(buildConfig.get("CHAR"), equalTo('a'))
        assertThat(buildConfig.get("SHORT"), equalTo(2.toShort()))
        assertThat(buildConfig.get("INT"), equalTo(3))
        assertThat(buildConfig.get("LONG"), equalTo(4L))
        assertThat(buildConfig.get("FLOAT"), equalTo(5.5F))
        assertThat(buildConfig.get("DOUBLE"), equalTo(6.5))
        assertThat(buildConfig.get("STRING"), equalTo("value"))

        assertThat(buildConfig.getJSONArray("BOOLEANS").asList(), contains(true, false))
        assertThat(buildConfig.getJSONArray("BYTES").asList(), contains(1.toByte(), 2.toByte()))
        assertThat(buildConfig.getJSONArray("CHARS").asList(), contains('a', 'b'))
        assertThat(buildConfig.getJSONArray("SHORTS").asList(), contains(3.toShort(), 4.toShort()))
        assertThat(buildConfig.getJSONArray("INTS").asList(), contains(5, 6))
        assertThat(buildConfig.getJSONArray("LONGS").asList(), contains(7L, 8L))
        assertThat(buildConfig.getJSONArray("FLOATS").asList(), contains(9.5F, 10.5F))
        assertThat(buildConfig.getJSONArray("DOUBLES").asList(), contains(11.5, 12.5))
        assertThat(buildConfig.getJSONArray("STRINGS").asList(), contains("one", "two"))
    }

    private fun JSONArray.asList(): List<Any> = List(length(), ::get)

    @Suppress("MayBeConstant", "unused")
    class TestBuildConfig {
        companion object {
            @JvmField val BOOLEAN = true
            @JvmField val BYTE: Byte = 1
            @JvmField val CHAR = 'a'
            @JvmField val SHORT: Short = 2
            @JvmField val INT = 3
            @JvmField val LONG = 4L
            @JvmField val FLOAT = 5.5F
            @JvmField val DOUBLE = 6.5
            @JvmField val STRING = "value"

            @JvmField val BOOLEANS = booleanArrayOf(true, false)
            @JvmField val BYTES = byteArrayOf(1, 2)
            @JvmField val CHARS = charArrayOf('a', 'b')
            @JvmField val SHORTS = shortArrayOf(3, 4)
            @JvmField val INTS = intArrayOf(5, 6)
            @JvmField val LONGS = longArrayOf(7L, 8L)
            @JvmField val FLOATS = floatArrayOf(9.5F, 10.5F)
            @JvmField val DOUBLES = doubleArrayOf(11.5, 12.5)
            @JvmField val STRINGS = arrayOf("one", "two")
        }
    }
}
