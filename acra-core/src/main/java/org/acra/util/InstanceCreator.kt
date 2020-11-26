/*
 *  Copyright 2017
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
package org.acra.util

import androidx.annotation.VisibleForTesting

/**
 * @author F43nd1r
 * @since 09.03.2017
 */
object InstanceCreator {
    /**
     * Create an instance of clazz
     *
     * @param clazz    the clazz to create an instance of
     * @param fallback a value provider which provides a fallback in case of a failure
     * @param <T>      the return type
     * @return a new instance of clazz or fallback
    </T> */
    @JvmStatic
    fun <T> create(clazz: Class<out T>, fallback: () -> T): T = create(clazz) ?: fallback.invoke()

    @VisibleForTesting
    @JvmStatic
    fun <T> create(clazz: Class<out T?>): T? {
        try {
            return clazz.newInstance()
        } catch (e: InstantiationException) {
            org.acra.log.error(e) { "Failed to create instance of class ${clazz.name}" }
        } catch (e: IllegalAccessException) {
            org.acra.log.error(e) { "Failed to create instance of class ${clazz.name}" }
        }
        return null
    }

    /**
     * Create instances of the given classes
     *
     * @param classes the classes to create insatnces of
     * @param <T>     the return type
     * @return a list of successfully created instances, does not contain null
    </T> */
    @JvmStatic
    fun <T> create(classes: Collection<Class<out T>>): List<T> = classes.mapNotNull { create(it) }
}