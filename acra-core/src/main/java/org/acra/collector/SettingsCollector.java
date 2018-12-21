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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.acra.ACRA.LOG_TAG;

/**
 * collects data from {@link System}, {@link Global} and {@link Secure} Settings classes.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector.class)
public final class SettingsCollector extends BaseReportFieldCollector {

    private static final String ERROR = "Error: ";

    public SettingsCollector() {
        super(ReportField.SETTINGS_SYSTEM, ReportField.SETTINGS_SECURE, ReportField.SETTINGS_GLOBAL);
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws Exception {
        switch (reportField) {
            case SETTINGS_SYSTEM:
                target.put(ReportField.SETTINGS_SYSTEM, collectSettings(context, config, System.class));
                break;
            case SETTINGS_SECURE:
                target.put(ReportField.SETTINGS_SECURE, collectSettings(context, config, Secure.class));
                break;
            case SETTINGS_GLOBAL:
                target.put(ReportField.SETTINGS_GLOBAL, Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? collectSettings(context, config, Global.class) : null);
                break;
            default:
                //will not happen if used correctly
                throw new IllegalArgumentException();
        }
    }

    @NonNull
    private JSONObject collectSettings(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull Class<?> settings) throws NoSuchMethodException {
        final JSONObject result = new JSONObject();
        final Field[] keys = settings.getFields();
        final Method getString = settings.getMethod("getString", ContentResolver.class, String.class);
        for (final Field key : keys) {
            if (!key.isAnnotationPresent(Deprecated.class) && key.getType() == String.class && isAuthorized(config, key)) {
                try {
                    //noinspection JavaReflectionInvocation
                    final Object value = getString.invoke(null, context.getContentResolver(), key.get(null));
                    if (value != null) {
                        result.put(key.getName(), value);
                    }
                } catch (@NonNull Exception e) {
                    ACRA.log.w(LOG_TAG, ERROR, e);
                }
            }
        }
        return result;
    }

    private boolean isAuthorized(@NonNull CoreConfiguration config, @Nullable Field key) {
        if (key == null || key.getName().startsWith("WIFI_AP")) {
            return false;
        }
        for (String regex : config.excludeMatchingSettingsKeys()) {
            if (key.getName().matches(regex)) {
                return false;
            }
        }
        return true;
    }
}
