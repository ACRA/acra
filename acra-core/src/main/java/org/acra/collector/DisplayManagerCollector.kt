/*
 *  Copyright 2016
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
package org.acra.collector

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.SparseArray
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.google.auto.service.AutoService
import org.acra.ACRA
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Collects information about the connected display(s)
 *
 * @author F43nd1r &amp; Various
 */
@Suppress("DEPRECATION")
@AutoService(Collector::class)
class DisplayManagerCollector : BaseReportFieldCollector(ReportField.DISPLAY) {
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        val result = JSONObject()
        for (display in getDisplays(context)) {
            try {
                result.put(display.displayId.toString(), collectDisplayData(display))
            } catch (e: JSONException) {
                ACRA.log.w(ACRA.LOG_TAG, "Failed to collect data for display " + display.displayId, e)
            }
        }
        target.put(ReportField.DISPLAY, result)
    }

    private fun getDisplays(context: Context): Array<Display> {
        return if (Build.VERSION.SDK_INT >= 17) {
            (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).displays
        } else {
            arrayOf((context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay)
        }
    }

    @Throws(JSONException::class)
    private fun collectDisplayData(display: Display): JSONObject {
        display.getMetrics(DisplayMetrics())
        val result = JSONObject()
        collectCurrentSizeRange(display, result)
        collectFlags(display, result)
        collectMetrics(display, result)
        collectRealMetrics(display, result)
        collectName(display, result)
        collectRealSize(display, result)
        collectRectSize(display, result)
        collectSize(display, result)
        collectRotation(display, result)
        collectIsValid(display, result)
        result.put("orientation", display.rotation)
                .put("refreshRate", display.refreshRate.toDouble())
                .put("height", display.height)
                .put("width", display.width)
                .put("pixelFormat", display.pixelFormat)
        return result
    }

    @Throws(JSONException::class)
    private fun collectIsValid(display: Display, container: JSONObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            container.put("isValid", display.isValid)
        }
    }

    @Throws(JSONException::class)
    private fun collectRotation(display: Display, container: JSONObject) {
        container.put("rotation", rotationToString(display.rotation))
    }

    private fun rotationToString(rotation: Int): String {
        return when (rotation) {
            Surface.ROTATION_0 -> "ROTATION_0"
            Surface.ROTATION_90 -> "ROTATION_90"
            Surface.ROTATION_180 -> "ROTATION_180"
            Surface.ROTATION_270 -> "ROTATION_270"
            else -> rotation.toString()
        }
    }

    @Throws(JSONException::class)
    private fun collectRectSize(display: Display, container: JSONObject) {
        val size = Rect().also { display.getRectSize(it) }
        container.put("rectSize", JSONArray(listOf(size.top, size.left, size.width(), size.height())))
    }

    @Throws(JSONException::class)
    private fun collectSize(display: Display, container: JSONObject) {
        val size = Point().also { display.getSize(it) }
        container.put("size", JSONArray(listOf(size.x, size.y)))
    }

    @Throws(JSONException::class)
    private fun collectRealSize(display: Display, container: JSONObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val size = Point().also { display.getRealSize(it) }
            container.put("realSize", JSONArray(listOf(size.x, size.y)))
        }
    }

    @Throws(JSONException::class)
    private fun collectCurrentSizeRange(display: Display, container: JSONObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val smallest = Point()
            val largest = Point()
            display.getCurrentSizeRange(smallest, largest)
            val result = JSONObject()
            result.put("smallest", JSONArray(listOf(smallest.x, smallest.y)))
            result.put("largest", JSONArray(listOf(largest.x, largest.y)))
            container.put("currentSizeRange", result)
        }
    }

    @Throws(JSONException::class)
    private fun collectFlags(display: Display, container: JSONObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val flagNames = SparseArray<String>()
            val flags = display.flags
            for (field in display.javaClass.fields) {
                if (field.name.startsWith("FLAG_")) {
                    try {
                        flagNames.put(field.getInt(null), field.name)
                    } catch (ignored: IllegalAccessException) {
                    }
                }
            }
            container.put("flags", activeFlags(flagNames, flags))
        }
    }

    @Throws(JSONException::class)
    private fun collectName(display: Display, container: JSONObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            container.put("name", display.name)
        }
    }

    @Throws(JSONException::class)
    private fun collectMetrics(display: Display, container: JSONObject) {
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val result = JSONObject()
        collectMetrics(metrics, result)
        container.put("metrics", result)
    }

    @Throws(JSONException::class)
    private fun collectRealMetrics(display: Display, container: JSONObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val metrics = DisplayMetrics()
            display.getRealMetrics(metrics)
            val result = JSONObject()
            collectMetrics(metrics, result)
            container.put("realMetrics", result)
        }
    }

    @Throws(JSONException::class)
    private fun collectMetrics(metrics: DisplayMetrics, container: JSONObject) {
        container.put("density", metrics.density.toDouble())
                .put("densityDpi", metrics.densityDpi)
                .put("scaledDensity", "x" + metrics.scaledDensity)
                .put("widthPixels", metrics.widthPixels)
                .put("heightPixels", metrics.heightPixels)
                .put("xdpi", metrics.xdpi.toDouble())
                .put("ydpi", metrics.ydpi.toDouble())
    }

    /**
     * Some fields contain multiple value types which can be isolated by
     * applying a bitmask. That method returns the concatenation of active
     * values.
     *
     * @param bitfield The bitfield to inspect.
     * @return The names of the different values contained in the bitfield,
     * separated by '+'.
     */
    private fun activeFlags(flagNames: SparseArray<String>, bitfield: Int): String {
        val result = StringBuilder()

        // Look for masks, apply it an retrieve the masked value
        for (i in 0 until flagNames.size()) {
            val maskValue = flagNames.keyAt(i)
            val value = bitfield and maskValue
            if (value > 0) {
                if (result.isNotEmpty()) {
                    result.append('+')
                }
                result.append(flagNames[value])
            }
        }
        return result.toString()
    }
}