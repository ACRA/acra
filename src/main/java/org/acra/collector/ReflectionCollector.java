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

package org.acra.collector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Tools to retrieve key/value pairs from static fields and getters of any
 * class. Reflection API usage allows to retrieve data without having to
 * implement a class for each android version of each interesting class.
 * It can also help find hidden properties.
 * 
 * @author Kevin Gaudin
 * 
 */
final class ReflectionCollector {

    /**
     * Retrieves key/value pairs from static fields of a class.
     *
     * @param someClass the class to be inspected.
     * 
     * @return A human readable string with a key=value pair on each line.
     */
    public static String collectConstants(Class<?> someClass, String prefix) {

        final StringBuilder result = new StringBuilder();

        final Field[] fields = someClass.getFields();
        for (final Field field : fields) {
            if (prefix != null && prefix.length() > 0) {
                result.append(prefix).append('.');
            }
            result.append(field.getName()).append("=");
            try {
                final Object value = field.get(null);
                if (value != null) {
                    result.append(value.toString());
                }
            } catch (IllegalArgumentException e) {
                result.append("N/A");
            } catch (IllegalAccessException e) {
                result.append("N/A");
            }
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * Retrieves key/value pairs from static getters of a class (get*() or is*()).
     *
     * @param someClass the class to be inspected.
     * @return A human readable string with a key=value pair on each line.
     */
    public static String collectStaticGettersResults(Class<?> someClass) {
        final StringBuilder result = new StringBuilder();
        final Method[] methods = someClass.getMethods();
        for (final Method method : methods) {
            if (method.getParameterTypes().length == 0
                    && (method.getName().startsWith("get") || method.getName().startsWith("is"))
                    && !method.getName().equals("getClass")) {
                try {
                    result.append(method.getName());
                    result.append('=');
                    result.append(method.invoke(null, (Object[]) null));
                    result.append("\n");
                } catch (IllegalArgumentException e) {
                    // NOOP
                } catch (IllegalAccessException e) {
                    // NOOP
                } catch (InvocationTargetException e) {
                    // NOOP
                }
            }
        }

        return result.toString();
    }

    public static String collectConstants(Class<?> someClass) {
        return collectConstants(someClass, "");
    }
}
