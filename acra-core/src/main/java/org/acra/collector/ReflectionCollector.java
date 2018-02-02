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

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Collector retrieving key/value pairs from static fields and getters.
 * Reflection API usage allows to retrieve data without having to implement a class for each android version of each interesting class.
 * It can also help find hidden properties.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector.class)
public final class ReflectionCollector extends BaseReportFieldCollector {

    public ReflectionCollector() {
        super(ReportField.BUILD, ReportField.BUILD_CONFIG, ReportField.ENVIRONMENT);
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target)
            throws JSONException, ClassNotFoundException {
        final JSONObject result = new JSONObject();
        switch (reportField) {
            case BUILD:
                collectConstants(Build.class, result);
                final JSONObject version = new JSONObject();
                collectConstants(Build.VERSION.class, version);
                result.put("VERSION", version);
                break;
            case BUILD_CONFIG:
                collectConstants(getBuildConfigClass(context, config), result);
                break;
            case ENVIRONMENT:
                collectStaticGettersResults(Environment.class, result);
                break;
            default:
                //will not happen if used correctly
                throw new IllegalArgumentException();
        }
        target.put(reportField, result);
    }

    /**
     * Retrieves key/value pairs from static fields of a class.
     *
     * @param someClass the class to be inspected.
     */
    private static void collectConstants(@NonNull Class<?> someClass, @NonNull JSONObject container) throws JSONException {
        final Field[] fields = someClass.getFields();
        for (final Field field : fields) {
            try {
                final Object value = field.get(null);
                if (value != null) {
                    if (field.getType().isArray()) {
                        container.put(field.getName(), new JSONArray(Arrays.asList((Object[]) value)));
                    } else {
                        container.put(field.getName(), value);
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // NOOP
            } catch (IllegalAccessException ignored) {
                // NOOP
            }
        }
    }

    /**
     * Retrieves key/value pairs from static getters of a class (get*() or is*()).
     *
     * @param someClass the class to be inspected.
     */
    private void collectStaticGettersResults(@NonNull Class<?> someClass, @NonNull JSONObject container) throws JSONException {
        final Method[] methods = someClass.getMethods();
        for (final Method method : methods) {
            if (method.getParameterTypes().length == 0
                    && (method.getName().startsWith("get") || method.getName().startsWith("is"))
                    && !"getClass".equals(method.getName())) {
                try {
                    container.put(method.getName(), method.invoke(null, (Object[]) null));
                } catch (@NonNull IllegalArgumentException ignored) {
                    // NOOP
                } catch (@NonNull InvocationTargetException ignored) {
                    // NOOP
                } catch (@NonNull IllegalAccessException ignored) {
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
    @NonNull
    private Class<?> getBuildConfigClass(@NonNull Context context, @NonNull CoreConfiguration config) throws ClassNotFoundException {
        final Class configuredBuildConfig = config.buildConfigClass();
        if (!configuredBuildConfig.equals(Object.class)) {
            // If set via annotations or programmatically then it will have a real value,
            // otherwise it will be Object.class (default).
            return configuredBuildConfig;
        }

        final String className = context.getPackageName() + ".BuildConfig";
        return Class.forName(className);
    }
}
