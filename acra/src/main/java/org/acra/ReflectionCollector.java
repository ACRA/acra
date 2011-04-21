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

package org.acra;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;

public class ReflectionCollector {
    public static String collectConstants(Class<? extends Object> someClass) {
        StringBuilder result = new StringBuilder();

        Field[] fields = someClass.getFields();
        for (Field field : fields) {
            result.append(field.getName()).append("=");
            try {
                result.append(field.get(null).toString());
            } catch (IllegalArgumentException e) {
                result.append("N/A");
            } catch (IllegalAccessException e) {
                result.append("N/A");
            }
            result.append("\n");
        }

        return result.toString();
    }

    public static String collectStaticGettersResults(Class<? extends Object> someClass) {
        StringBuilder result = new StringBuilder();
        Method[] methods = someClass.getMethods();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 0
                    && (method.getName().startsWith("get") || method.getName().startsWith("is"))
                    && !method.getName().equals("getClass")) {
                try {
                    result.append(method.getName()).append('=').append(method.invoke(null, (Object[]) null))
                            .append("\n");
                } catch (IllegalArgumentException e) {
                    Log.w(ACRA.LOG_TAG, "Could not invoke method : ", e);
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    Log.w(ACRA.LOG_TAG, "Error : ", e);
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    Log.w(ACRA.LOG_TAG, "Error : ", e);
                }
            }
        }

        return result.toString();
    }

}
