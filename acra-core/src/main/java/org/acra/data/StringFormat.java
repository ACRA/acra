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
import android.text.TextUtils;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collections.ImmutableSet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents possible report formats
 *
 * @author F43nd1r
 * @since 14.11.2017
 */
public enum StringFormat {
    JSON("application/json") {
        @NonNull
        @Override
        public String toFormattedString(@NonNull CrashReportData data, @NonNull ImmutableSet<ReportField> order, @NonNull String mainJoiner, @NonNull String subJoiner, boolean urlEncode) throws JSONException {
            final Map<String, Object> map = data.toMap();
            final JSONStringer stringer = new JSONStringer().object();
            for (ReportField field : order) {
                stringer.key(field.toString()).value(map.remove(field.toString()));
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                stringer.key(entry.getKey()).value(entry.getValue());
            }
            return stringer.endObject().toString();
        }
    },
    KEY_VALUE_LIST("application/x-www-form-urlencoded") {
        @NonNull
        @Override
        public String toFormattedString(@NonNull CrashReportData data, @NonNull ImmutableSet<ReportField> order, @NonNull String mainJoiner, @NonNull String subJoiner, boolean urlEncode) throws UnsupportedEncodingException {
            final Map<String, String> map = toStringMap(data.toMap(), subJoiner);
            final StringBuilder builder = new StringBuilder();
            for (ReportField field : order) {
                append(builder, field.toString(), map.remove(field.toString()), mainJoiner, urlEncode);
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                append(builder, entry.getKey(), entry.getValue(), mainJoiner, urlEncode);
            }
            return builder.toString();
        }

        private void append(@NonNull StringBuilder builder, @Nullable String key, @Nullable String value, @Nullable String joiner, boolean urlEncode) throws UnsupportedEncodingException {
            if (builder.length() > 0) {
                builder.append(joiner);
            }
            if (urlEncode) {
                key = key != null ? URLEncoder.encode(key, ACRAConstants.UTF8) : null;
                value = value != null ? URLEncoder.encode(value, ACRAConstants.UTF8) : null;
            }
            builder.append(key).append('=').append(value);
        }

        @NonNull
        private Map<String, String> toStringMap(@NonNull Map<String, Object> map, @NonNull String joiner) {
            final Map<String, String> stringMap = new HashMap<>(map.size());
            for (final Map.Entry<String, Object> entry : map.entrySet()) {
                stringMap.put(entry.getKey(), valueToString(joiner, entry.getValue()));
            }
            return stringMap;
        }

        private String valueToString(@NonNull String joiner, @Nullable Object value) {
            if (value instanceof JSONObject) {
                return TextUtils.join(joiner, flatten((JSONObject) value));
            } else {
                return String.valueOf(value);
            }
        }

        @NonNull
        private List<String> flatten(@NonNull JSONObject json) {
            final List<String> result = new ArrayList<>();
            for (final Iterator<String> iterator = json.keys(); iterator.hasNext(); ) {
                final String key = iterator.next();
                Object value;
                try {
                    value = json.get(key);
                } catch (JSONException e) {
                    value = null;
                }
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
    };

    private final String contentType;

    StringFormat(@NonNull String contentType) {
        this.contentType = contentType;
    }

    @NonNull
    public abstract String toFormattedString(@NonNull CrashReportData data, @NonNull ImmutableSet<ReportField> order, @NonNull String mainJoiner, @NonNull String subJoiner, boolean urlEncode) throws Exception;

    @NonNull
    public String getMatchingHttpContentType() {
        return contentType;
    }
}
