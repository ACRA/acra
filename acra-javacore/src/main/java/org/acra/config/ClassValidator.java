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

package org.acra.config;

import java.lang.reflect.Modifier;

/**
 * @author F43nd1r
 * @since 07.06.2017
 */

public final class ClassValidator {
    private ClassValidator() {
    }

    public static void check(Class<?>... classes) throws ACRAConfigurationException {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                throw new ACRAConfigurationException("Expected class, but found interface " + clazz.getName() + ".");
            } else if (Modifier.isAbstract(clazz.getModifiers())) {
                throw new ACRAConfigurationException("Class " + clazz.getName() + " cannot be abstract.");
            } else if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) {
                throw new ACRAConfigurationException("Class " + clazz.getName() + " has to be static.");
            }
            try {
                clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new ACRAConfigurationException("Class " + clazz.getName() + " is missing a no-args Constructor.", e);
            }
        }
    }
}
