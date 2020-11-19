/*
 *  Copyright 2011 Kevin Gaudin
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
package org.acra.config

import android.content.Context
import org.acra.ACRAConstants
import org.acra.ReportField
import org.acra.annotation.BuilderMethod
import org.acra.annotation.ConfigurationValue
import org.acra.annotation.PreBuild
import org.acra.annotation.Transform
import org.acra.log.debug
import org.acra.log.warn
import org.acra.plugins.PluginLoader
import org.acra.plugins.ServicePluginLoader
import org.acra.util.StubCreator.createStub
import java.lang.reflect.Method
import java.util.*

/**
 * Contains builder methods which can't be generated
 *
 * @author F43nd1r
 */
class BaseCoreConfigurationBuilder internal constructor(private val app: Context) {
    private val reportContentChanges: MutableMap<ReportField, Boolean> = EnumMap(ReportField::class.java)
    private lateinit var configBuilders: List<ConfigurationBuilder>
    private lateinit var configurations: List<Configuration>
    private var pluginLoader: PluginLoader = ServicePluginLoader()

    private fun configurationBuilders(): List<ConfigurationBuilder> {
        if (!this::configBuilders.isInitialized) {
            val factories = pluginLoader.load(ConfigurationBuilderFactory::class.java)
            debug { "Found ConfigurationBuilderFactories : $factories" }
            configBuilders = factories.map { it.create(app) }
        }
        return configBuilders
    }

    /**
     * Set a custom plugin loader. Note: Call this before any call to [.getPluginConfigurationBuilder]
     *
     * @param pluginLoader the custom implementation
     */
    @BuilderMethod
    fun setPluginLoader(pluginLoader: PluginLoader) {
        this.pluginLoader = pluginLoader
    }

    @ConfigurationValue
    fun pluginLoader(): PluginLoader {
        return pluginLoader
    }

    @PreBuild
    @Throws(ACRAConfigurationException::class)
    fun preBuild() {
        val builders = configurationBuilders()
        debug { "Found ConfigurationBuilders : $builders" }
        configurations = builders.map { it.build() }
    }

    @Transform(methodName = "reportContent")
    fun transformReportContent(reportFields: Array<ReportField>): List<ReportField> {
        val reportContent: MutableList<ReportField> = ArrayList()
        if (reportFields.isNotEmpty()) {
            debug { "Using custom Report Fields" }
            reportContent.addAll(reportFields)
        } else {
            debug { "Using default Report Fields" }
            reportContent.addAll(ACRAConstants.DEFAULT_REPORT_FIELDS)
        }

        // Add or remove any extra fields.
        for ((key, value) in reportContentChanges) {
            if (value) {
                reportContent.add(key)
            } else {
                reportContent.remove(key)
            }
        }
        return reportContent
    }

    /**
     * Use this if you want to keep the default configuration of reportContent, but set some fields explicitly.
     *
     * @param field  the field to set
     * @param enable if this field should be reported
     */
    @BuilderMethod
    fun setReportField(field: ReportField, enable: Boolean) {
        reportContentChanges[field] = enable
    }

    @ConfigurationValue
    fun pluginConfigurations(): List<Configuration> {
        return configurations
    }

    @BuilderMethod
    fun <R : ConfigurationBuilder> getPluginConfigurationBuilder(c: Class<R>): R {
        for (builder in configurationBuilders()) {
            if (c.isAssignableFrom(builder.javaClass)) {
                @Suppress("UNCHECKED_CAST")
                return builder as R
            }
        }
        if (c.isInterface) {
            warn { "Couldn't find ConfigurationBuilder ${c.simpleName}. ALL CALLS TO IT WILL BE IGNORED!" }
            return createStub(c) { proxy, _, _ -> proxy }
        }
        throw IllegalArgumentException("Class ${c.name} is not a registered ConfigurationBuilder")
    }
}