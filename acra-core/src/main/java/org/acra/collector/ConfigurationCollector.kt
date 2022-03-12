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
package org.acra.collector

import android.content.Context
import android.content.res.Configuration
import android.util.SparseArray
import com.google.auto.service.AutoService
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.log.error
import org.acra.log.warn
import org.acra.util.mapNotNullToSparseArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*

/**
 * Inspects a [Configuration] object through reflection API in order to generate a human readable String with values replaced with their constants names.
 * The [Configuration.toString] method was not enough as values like 0, 1, 2 or 3 aren't readable.
 * Using reflection API allows to retrieve hidden fields and can make us hope to be compatible with all Android API levels, even those which are not published yet.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector::class)
class ConfigurationCollector : BaseReportFieldCollector(ReportField.INITIAL_CONFIGURATION, ReportField.CRASH_CONFIGURATION), ApplicationStartupCollector {

    private var initialConfiguration: JSONObject? = null

    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration,
                         reportBuilder: ReportBuilder, target: CrashReportData) {
        when (reportField) {
            ReportField.INITIAL_CONFIGURATION -> target.put(ReportField.INITIAL_CONFIGURATION, initialConfiguration)
            ReportField.CRASH_CONFIGURATION -> target.put(ReportField.CRASH_CONFIGURATION, collectConfiguration(context))
            else -> throw IllegalArgumentException()
        }
    }

    override fun collectApplicationStartUp(context: Context, config: CoreConfiguration) {
        if (config.reportContent.contains(ReportField.INITIAL_CONFIGURATION)) {
            initialConfiguration = collectConfiguration(context)
        }
    }

    /**
     * Creates a [JSONObject] listing all values human readable
     * from the provided Configuration instance.
     *
     * @param conf The Configuration to be described.
     * @return A JSONObject with all fields of the given Configuration,
     * with values replaced by constant names.
     */
    private fun configToJson(conf: Configuration): JSONObject {
        val result = JSONObject()
        val valueArrays = getValueArrays()
        for (f in conf.javaClass.fields) {
            try {
                if (!Modifier.isStatic(f.modifiers)) {
                    val fieldName = f.name
                    try {
                        if (f.type == Int::class.javaPrimitiveType) {
                            result.put(fieldName, getFieldValueName(valueArrays, conf, f))
                        } else if (f[conf] != null) {
                            result.put(fieldName, f[conf])
                        }
                    } catch (e: JSONException) {
                        warn(e) { "Could not collect configuration field $fieldName" }
                    }
                }
            } catch (e: IllegalArgumentException) {
                error(e) { "Error while inspecting device configuration: " }
            } catch (e: IllegalAccessException) {
                error(e) { "Error while inspecting device configuration: " }
            }
        }
        return result
    }

    private fun getValueArrays(): Map<Prefix, SparseArray<String>> {
        return Configuration::class.java.fields.filter { Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) }
                .groupBy { field -> Prefix.values().firstOrNull { field.name.startsWith(it.text) } }
                .filterKeys { it != null }.map { (prefix, fields) ->
                    prefix!! to fields.mapNotNullToSparseArray {
                        try {
                            return@mapNotNullToSparseArray it.getInt(null) to it.name
                        } catch (e: IllegalArgumentException) {
                            warn(e) { "Error while inspecting device configuration: " }
                        } catch (e: IllegalAccessException) {
                            warn(e) { "Error while inspecting device configuration: " }
                        }
                        null
                    }
                }.toMap(EnumMap(Prefix::class.java)).withDefault { SparseArray() }
    }

    /**
     * Retrieve the name of the constant defined in the [Configuration]
     * class which defines the value of a field in a [Configuration]
     * instance.
     *
     * @param conf The instance of [Configuration] where the value is
     * stored.
     * @param f    The [Field] to be inspected in the [Configuration]
     * instance.
     * @return The value of the field f in instance conf translated to its
     * constant name.
     * @throws IllegalAccessException if the supplied field is inaccessible.
     */
    @Throws(IllegalAccessException::class)
    private fun getFieldValueName(valueArrays: Map<Prefix, SparseArray<String>>, conf: Configuration, f: Field): Any {
        return when (val fieldName = f.name) {
            FIELD_MCC, FIELD_MNC -> f.getInt(conf)
            FIELD_UIMODE -> activeFlags(valueArrays.getValue(Prefix.UI_MODE), f.getInt(conf))
            FIELD_SCREENLAYOUT -> activeFlags(valueArrays.getValue(Prefix.SCREENLAYOUT), f.getInt(conf))
            else -> {
                val values = Prefix.values().firstOrNull { it.text == fieldName.uppercase() + '_' }?.let { valueArrays.getValue(it) } ?: return f.getInt(
                        conf) // Unknown field, return the raw int as String
                val value = values[f.getInt(conf)] ?: return f.getInt(conf) // Unknown value, return the raw int as String
                value
            }
        }
    }

    /**
     * Some fields contain multiple value types which can be isolated by
     * applying a bitmask. That method returns the concatenation of active
     * values.
     *
     * @param valueNames The array containing the different values and names for this
     * field. Must contain mask values too.
     * @param bitfield   The bitfield to inspect.
     * @return The names of the different values contained in the bitfield,
     * separated by '+'.
     */
    private fun activeFlags(valueNames: SparseArray<String>, bitfield: Int): String {
        val result = StringBuilder()

        // Look for masks, apply it an retrieve the masked value
        for (i in 0 until valueNames.size()) {
            val maskValue = valueNames.keyAt(i)
            if (valueNames[maskValue].endsWith(SUFFIX_MASK)) {
                val value = bitfield and maskValue
                if (value > 0) {
                    if (result.isNotEmpty()) {
                        result.append('+')
                    }
                    result.append(valueNames[value])
                }
            }
        }
        return result.toString()
    }

    /**
     * Returns the current Configuration for this application.
     *
     * @param context Context for the application being reported.
     * @return A String representation of the current configuration for the application.
     */
    private fun collectConfiguration(context: Context): JSONObject? {
        return try {
            configToJson(context.resources.configuration)
        } catch (e: RuntimeException) {
            warn(e) { "Couldn't retrieve CrashConfiguration for : " + context.packageName }
            null
        }
    }

    enum class Prefix(val text: String) {
        UI_MODE("UI_MODE_"),
        TOUCHSCREEN("TOUCHSCREEN_"),
        SCREENLAYOUT("SCREENLAYOUT_"),
        ORIENTATION("ORIENTATION_"),
        NAVIGATIONHIDDEN("NAVIGATIONHIDDEN_"),
        NAVIGATION("NAVIGATION_"),
        KEYBOARDHIDDEN("KEYBOARDHIDDEN_"),
        KEYBOARD("KEYBOARD_"),
        HARDKEYBOARDHIDDEN("HARDKEYBOARDHIDDEN_"),
    }

    companion object {
        private const val SUFFIX_MASK = "_MASK"
        private const val FIELD_SCREENLAYOUT = "screenLayout"
        private const val FIELD_UIMODE = "uiMode"
        private const val FIELD_MNC = "mnc"
        private const val FIELD_MCC = "mcc"
    }
}