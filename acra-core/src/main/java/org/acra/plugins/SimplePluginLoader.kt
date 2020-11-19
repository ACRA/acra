/*
 * Copyright (c) 2018
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
package org.acra.plugins

import org.acra.config.CoreConfiguration
import org.acra.log.debug
import org.acra.log.warn

/**
 * @author lukas
 * @since 01.07.18
 */
class SimplePluginLoader @SafeVarargs constructor(private vararg val plugins: Class<out Plugin>) : PluginLoader {
    override fun <T : Plugin> load(clazz: Class<T>): List<T> {
        debug { "SimplePluginLoader loading services from plugin classes : $plugins" }
        return plugins.mapNotNull {
            if (clazz.isAssignableFrom(it)) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val instance = it.newInstance() as T
                    debug { "Loaded plugin from class : $it" }
                    return@mapNotNull instance
                } catch (e: Exception) {
                    warn(e) { "Could not load plugin from class : $it" }
                }
            }
            null
        }
    }

    override fun <T : Plugin> loadEnabled(config: CoreConfiguration, clazz: Class<T>): List<T> {
        return load(clazz).filter { it.enabled(config) }
    }
}