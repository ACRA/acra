/*
 *  Copyright 2010 Kevin Gaudin
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
import android.os.Build
import android.os.Environment
import com.google.auto.service.AutoService
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException

/**
 * Collector retrieving key/value pairs from static fields and getters.
 * Reflection API usage allows to retrieve data without having to implement a class for each android version of each interesting class.
 * It can also help find hidden properties.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector::class)
class ReflectionCollector : BaseReportFieldCollector(ReportField.BUILD, ReportField.BUILD_CONFIG, ReportField.ENVIRONMENT) {
    @Throws(JSONException::class, ClassNotFoundException::class)
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        val result = JSONObject()
        when (reportField) {
            ReportField.BUILD -> {
                collectConstants(Build::class.java, result)
                val version = JSONObject()
                collectConstants(Build.VERSION::class.java, version)
                result.put("VERSION", version)
            }
            ReportField.BUILD_CONFIG -> collectConstants(getBuildConfigClass(context, config), result)
            ReportField.ENVIRONMENT -> collectStaticGettersResults(Environment::class.java, result)
            else -> throw IllegalArgumentException()
        }
        target.put(reportField, result)
    }

    /**
     * Retrieves key/value pairs from static getters of a class (get*() or is*()).
     *
     * @param someClass the class to be inspected.
     */
    @Throws(JSONException::class)
    private fun collectStaticGettersResults(someClass: Class<*>, container: JSONObject) {
        val methods = someClass.methods
        for (method in methods) {
            if (method.parameterTypes.isEmpty() && (method.name.startsWith("get") || method.name.startsWith("is"))
                    && "getClass" != method.name) {
                try {
                    container.put(method.name, method.invoke(null))
                } catch (ignored: IllegalArgumentException) {
                    // NOOP
                } catch (ignored: InvocationTargetException) {
                    // NOOP
                } catch (ignored: IllegalAccessException) {
                    // NOOP
                }
            }
        }
    }

    /**
     * get the configured BuildConfigClass or guess it if not configured
     *
     * @return the BuildConfigClass
     * @throws ClassNotFoundException if the class cannot be found
     */
    @Throws(ClassNotFoundException::class)
    private fun getBuildConfigClass(context: Context, config: CoreConfiguration): Class<*> {
        val configuredBuildConfig: Class<*> = config.buildConfigClass
        if (configuredBuildConfig != Any::class.java) {
            // If set via annotations or programmatically then it will have a real value,
            // otherwise it will be Object.class (default).
            return configuredBuildConfig
        }
        val className = context.packageName + ".BuildConfig"
        return Class.forName(className)
    }

    companion object {
        /**
         * Retrieves key/value pairs from static fields of a class.
         *
         * @param someClass the class to be inspected.
         */
        @Throws(JSONException::class)
        private fun collectConstants(someClass: Class<*>, container: JSONObject) {
            val fields = someClass.fields
            for (field in fields) {
                try {
                    val value = field[null]
                    if (value != null) {
                        if (field.type.isArray) {
                            @Suppress("UNCHECKED_CAST")
                            container.put(field.name, JSONArray(listOf(*value as Array<Any?>)))
                        } else {
                            container.put(field.name, value)
                        }
                    }
                } catch (ignored: IllegalArgumentException) {
                    // NOOP
                } catch (ignored: IllegalAccessException) {
                    // NOOP
                }
            }
        }
    }
}