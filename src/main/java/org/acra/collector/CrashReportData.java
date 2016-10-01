/*
 *  Copyright 2012 Kevin Gaudin
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

import android.support.annotation.NonNull;

import org.acra.ReportField;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Stores a crash reports data with {@link org.acra.ReportField} enum values as keys.
 * <p>
 * This is basically the source of {@link Properties} adapted to extend an
 * EnumMap instead of Hashtable and with a few tweaks to avoid losing crazy
 * amounts of android time in the generation of a date comment when storing to file.
 */
public final class CrashReportData extends EnumMap<ReportField, CrashReportData.Element> {

    private static final long serialVersionUID = 5002578634500874842L;

    /**
     * Constructs a new {@code Properties} object.
     */
    public CrashReportData() {
        super(ReportField.class);
    }

    /**
     * Returns the property with the specified name.
     *
     * @param key the name of the property to find.
     * @return the named property value, or {@code null} if it can't be found.
     */
    public String getProperty(@NonNull ReportField key) {
        return super.get(key).asString();
    }

    public CrashReportData.Element putSimple(@NonNull ReportField key, String value) {
        return put(key, new SimpleElement(value));
    }

    @NonNull
    public JSONObject toJSON() {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Entry<ReportField, Element> entry : this.entrySet()){
            Element element = entry.getValue();
            Object value = element instanceof SimpleElement ? element.asString() : element;
            map.put(entry.getKey().name(), value);
        }
        return new JSONObject(map);
    }

    public interface Element {
        String asString();

        String[] flatten();
    }

    public static class SimpleElement implements Element {
        private final String content;

        public SimpleElement(String content) {
            this.content = content;
        }

        @Override
        public String asString() {
            return toString();
        }

        @Override
        public String[] flatten() {
            return new String[]{content};
        }

        @Override
        public String toString() {
            return content;
        }
    }

    public static class ComplexElement extends JSONObject implements Element {
        public ComplexElement() {
        }

        public ComplexElement(String json) throws JSONException {
            super(json);
        }

        public ComplexElement(Map<String, ?> copyFrom) {
            super(copyFrom);
        }

        @Override
        public String asString() {
            return toString();
        }

        @Override
        public String[] flatten() {
            try {
                return flatten(this).toArray(new String[0]);
            } catch (JSONException e) {
                return new String[0];
            }
        }

        private List<String> flatten(JSONObject jsonObject) throws JSONException {
            List<String> result = new ArrayList<String>();
            for (Iterator<String> iterator = jsonObject.keys(); iterator.hasNext(); ) {
                String key = iterator.next();
                Object value = jsonObject.get(key);
                if (value instanceof JSONObject) {
                    for (String s : flatten((JSONObject) value)) {
                        result.add(key + "." + s);
                    }
                } else {
                    result.add(key + "." + value);
                }
            }
            return result;
        }
    }
}
