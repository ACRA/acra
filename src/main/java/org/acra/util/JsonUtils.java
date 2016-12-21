/*
 * Copyright (c) 2016
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

package org.acra.util;

import android.util.Log;

import org.acra.ReportField;
import org.acra.model.ComplexElement;
import org.acra.collector.CrashReportData;
import org.acra.model.Element;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 12.10.2016
 */

public final class JsonUtils {
    private JsonUtils() {
    }

    public static JSONObject toJson(CrashReportData data) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<ReportField, Element> entry : data.entrySet()) {
            Element element = entry.getValue();
            map.put(entry.getKey().name(), element.value());
        }
        return new JSONObject(map);
    }

    public static CrashReportData toCrashReportData(JSONObject json) {
        CrashReportData data = new CrashReportData();
        for (Iterator<String> iterator = json.keys(); iterator.hasNext(); ) {
            String key = iterator.next();
            try {
                ReportField field = ReportField.valueOf(key);
                Object value = json.get(key);
                if (value instanceof JSONObject) {
                    data.put(field, new ComplexElement((JSONObject) value));
                } else if (value instanceof Number) {
                    data.putNumber(field, (Number) value);
                } else if (value instanceof Boolean) {
                    data.putBoolean(field, (Boolean) value);
                } else {
                    data.putString(field, value.toString());
                }
            } catch (IllegalArgumentException e) {
                Log.w(LOG_TAG, "Unknown report key " + key, e);
            } catch (JSONException e) {
                Log.w(LOG_TAG, "Unable to read report field " + key, e);
            }
        }
        return data;
    }

    public static List<String> flatten(JSONObject json) throws JSONException {
        List<String> result = new ArrayList<String>();
        for (Iterator<String> iterator = json.keys(); iterator.hasNext(); ) {
            String key = iterator.next();
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                for (String s : flatten((JSONObject) value)) {
                    result.add(key + "." + s);
                }
            } else {
                result.add(key + "=" + value);
            }
        }
        return result;
    }
}
