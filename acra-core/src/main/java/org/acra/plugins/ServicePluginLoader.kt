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
import org.acra.log.error
import java.util.*

/**
 * Utility to load [Plugin]s
 *
 * @author F43nd1r
 * @since 18.04.18
 */
class ServicePluginLoader : PluginLoader {
    override fun <T : Plugin> load(clazz: Class<T>): List<T> = loadInternal(clazz) { true }

    override fun <T : Plugin> loadEnabled(config: CoreConfiguration, clazz: Class<T>): List<T> = loadInternal(clazz) { it.enabled(config) }

    private fun <T : Plugin> loadInternal(clazz: Class<T>, shouldLoadPredicate: (T) -> Boolean): List<T> {
        val plugins: MutableList<T> = ArrayList()
        val serviceLoader = ServiceLoader.load(clazz, javaClass.classLoader)
        debug { "ServicePluginLoader loading services from ServiceLoader : $serviceLoader" }
        val iterator: Iterator<T> = serviceLoader.iterator()
        while (true) {
            try {
                if (!iterator.hasNext()) {
                    break
                }
            } catch (e: ServiceConfigurationError) {
                error(e) { "Broken ServiceLoader for ${clazz.simpleName}" }
                break
            }
            try {
                val plugin = iterator.next()
                if (shouldLoadPredicate.invoke(plugin)) {
                    debug { "Loaded ${clazz.simpleName} of type ${plugin.javaClass.name}" }
                    plugins.add(plugin)
                } else {
                    debug { "Ignoring disabled ${clazz.simpleName} of type ${plugin.javaClass.simpleName}" }
                }
            } catch (e: ServiceConfigurationError) {
                error(e) { "Unable to load ${clazz.simpleName}" }
            }
        }
        return plugins
    }
}