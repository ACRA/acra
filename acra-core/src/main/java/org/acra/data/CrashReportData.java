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

package org.acra.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collections.ImmutableSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Stores a crash report data
 */
public final class CrashReportData {
    private final JSONObject content;

    public CrashReportData() {
        content = new JSONObject();
    }

    public CrashReportData(String json) throws JSONException {
        content = new JSONObject(json);
    }

    public synchronized void put(@NonNull String key, boolean value) {
        try {
            content.put(key, value);
        } catch (JSONException e) {
            ACRA.log.w(ACRA.LOG_TAG, "Failed to put value into CrashReportData: " + String.valueOf(value));
        }
    }

    public synchronized void put(@NonNull String key, double value) {
        try {
            content.put(key, value);
        } catch (JSONException e) {
            ACRA.log.w(ACRA.LOG_TAG, "Failed to put value into CrashReportData: " + String.valueOf(value));
        }
    }

    public synchronized void put(@NonNull String key, int value) {
        try {
            content.put(key, value);
        } catch (JSONException e) {
            ACRA.log.w(ACRA.LOG_TAG, "Failed to put value into CrashReportData: " + String.valueOf(value));
        }
    }

    public synchronized void put(@NonNull String key, long value) {
        try {
            content.put(key, value);
        } catch (JSONException e) {
            ACRA.log.w(ACRA.LOG_TAG, "Failed to put value into CrashReportData: " + String.valueOf(value));
        }
    }

    public synchronized void put(@NonNull String key, @Nullable String value) {
        if (value == null) {
            putNA(key);
            return;
        }
        try {
            content.put(key, value);
        } catch (JSONException e) {
            ACRA.log.w(ACRA.LOG_TAG, "Failed to put value into CrashReportData: " + value);
        }
    }

    public synchronized void put(@NonNull String key, @Nullable JSONObject value) {
        if (value == null) {
            putNA(key);
            return;
        }
        try {
            content.put(key, value);
        } catch (JSONException e) {
            ACRA.log.w(ACRA.LOG_TAG, "Failed to put value into CrashReportData: " + String.valueOf(value));
        }
    }

    public synchronized void put(@NonNull String key, @Nullable JSONArray value) {
        if (value == null) {
            putNA(key);
            return;
        }
        try {
            content.put(key, value);
        } catch (JSONException e) {
            ACRA.log.w(ACRA.LOG_TAG, "Failed to put value into CrashReportData: " + String.valueOf(value));
        }
    }

    public synchronized void put(@NonNull ReportField key, boolean value) {
        put(key.toString(), value);
    }

    public synchronized void put(@NonNull ReportField key, double value) {
        put(key.toString(), value);
    }

    public synchronized void put(@NonNull ReportField key, int value) {
        put(key.toString(), value);
    }

    public synchronized void put(@NonNull ReportField key, long value) {
        put(key.toString(), value);
    }

    public synchronized void put(@NonNull ReportField key, @Nullable String value) {
        put(key.toString(), value);
    }

    public synchronized void put(@NonNull ReportField key, @Nullable JSONObject value) {
        put(key.toString(), value);
    }

    public synchronized void put(@NonNull ReportField key, @Nullable JSONArray value) {
        put(key.toString(), value);
    }

    private void putNA(@NonNull String key) {
        try {
            content.put(key, ACRAConstants.NOT_AVAILABLE);
        } catch (JSONException ignored) {
        }
    }

    /**
     * Returns the property with the specified key.
     *
     * @param key the key of the property to find.
     * @return the keyd property value, or {@code null} if it can't be found.
     */
    public String getString(@NonNull ReportField key) {
        return content.optString(key.toString());
    }

    public Object get(@NonNull String key) {
        return content.opt(key);
    }

    public boolean containsKey(@NonNull String key) {
        return content.has(key);
    }

    public boolean containsKey(@NonNull ReportField key) {
        return containsKey(key.toString());
    }

    @NonNull
    public String toJSON() throws JSONException {
        try {
            return StringFormat.JSON.toFormattedString(this, ImmutableSet.empty(), "", "", false);
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONException(e.getMessage());
        }
    }

    @NonNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>(content.length());
        for (final Iterator<String> iterator = content.keys(); iterator.hasNext(); ) {
            final String key = iterator.next();
            map.put(key, get(key));
        }
        return map;
    }
}
