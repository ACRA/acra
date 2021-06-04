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
@file:JvmName("ConfigUtils")

package org.acra.config

import org.acra.log.debug

/**
 * Allows easy access to Plugin configurations from the main configuration
 *
 * @author F43nd1r
 * @since 01.06.2017
 */

inline fun <reified T : Configuration> CoreConfiguration.getPluginConfiguration() : T = getPluginConfiguration(T::class.java)

fun <T : Configuration> CoreConfiguration.getPluginConfiguration(c: Class<T>): T {
    return findPluginConfiguration(c) ?: throw IllegalArgumentException("${c.name} is no registered configuration")
}

inline fun <reified T : Configuration> CoreConfiguration.findPluginConfiguration() : T? = findPluginConfiguration(T::class.java)

fun <T : Configuration> CoreConfiguration.findPluginConfiguration(c: Class<T>): T? {
    debug { "Checking plugin Configurations : $pluginConfigurations for class : $c" }
    for (configuration in pluginConfigurations) {
        debug { "Checking plugin Configuration : $configuration against plugin class : $c" }
        if (c.isAssignableFrom(configuration.javaClass)) {
            @Suppress("UNCHECKED_CAST")
            return configuration as T
        }
    }
    return null
}