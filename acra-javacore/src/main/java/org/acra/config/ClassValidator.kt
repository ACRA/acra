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
package org.acra.config

import java.lang.reflect.Modifier

/**
 * @author F43nd1r
 * @since 07.06.2017
 */
object ClassValidator {
    @JvmStatic
    @Throws(ACRAConfigurationException::class)
    fun check(vararg classes: Class<*>) {
        for (clazz in classes) {
            if (clazz.isInterface) {
                throw ACRAConfigurationException("Expected class, but found interface ${clazz.name}.")
            } else if (Modifier.isAbstract(clazz.modifiers)) {
                throw ACRAConfigurationException("Class ${clazz.name} cannot be abstract.")
            } else if (clazz.enclosingClass != null && !Modifier.isStatic(clazz.modifiers)) {
                throw ACRAConfigurationException("Class ${clazz.name} has to be static.")
            }
            try {
                clazz.getConstructor()
            } catch (e: NoSuchMethodException) {
                throw ACRAConfigurationException("""Class ${clazz.name} is missing a no-args Constructor.""", e)
            }
        }
    }
}