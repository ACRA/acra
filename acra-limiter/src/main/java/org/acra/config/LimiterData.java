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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.data.CrashReportData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author F43nd1r
 * @since 26.10.2017
 */

class LimiterData {
    private final List<ReportMetadata> list;

    LimiterData(@Nullable String json) throws JSONException {
        list = new ArrayList<>();
        if (json != null && !json.isEmpty()) {
            final JSONArray array = new JSONArray(json);
            final int length = array.length();
            for (int i = 0; i < length; i++) {
                list.add(new ReportMetadata(array.optJSONObject(i)));
            }
        }
    }

    @NonNull
    public List<ReportMetadata> getReportMetadata() {
        return list;
    }

    public void purgeOldData(Calendar keepAfter) {
        for (final Iterator<ReportMetadata> iterator = list.iterator(); iterator.hasNext(); ) {
            if (keepAfter.after(iterator.next().getTimestamp())) {
                iterator.remove();
            }
        }
    }

    public String toJSON() {
        return new JSONArray(list).toString();
    }

    public static class ReportMetadata extends JSONObject {
        private static final String KEY_STACK_TRACE = "stacktrace";
        private static final String KEY_EXCEPTION_CLASS = "class";
        private static final String KEY_TIMESTAMP = "timestamp";

        ReportMetadata(@NonNull CrashReportData crashReportData) throws JSONException {
            final String stacktrace = crashReportData.getString(ReportField.STACK_TRACE);
            put(KEY_STACK_TRACE, stacktrace);
            final int index = stacktrace.indexOf('\n');
            final String firstLine = index == -1 ? stacktrace : stacktrace.substring(0, index);
            final int index2 = stacktrace.indexOf(':');
            final String className = index2 == -1 ? firstLine : firstLine.substring(0, index2);
            try {
                Class.forName(className);
                put(KEY_EXCEPTION_CLASS, className);
            } catch (ClassNotFoundException ignored) {
            }
            put(KEY_TIMESTAMP, crashReportData.getString(ReportField.USER_CRASH_DATE));


        }

        ReportMetadata(@NonNull JSONObject copyFrom) throws JSONException {
            super(copyFrom, jsonArrayToList(copyFrom.names()));
        }

        public String getStacktrace() {
            return optString(KEY_STACK_TRACE);
        }

        public String getExceptionClass() {
            return optString(KEY_EXCEPTION_CLASS);
        }

        @Nullable
        Calendar getTimestamp() {
            final String timestamp = optString(KEY_TIMESTAMP);
            if (timestamp != null) {
                try {
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new SimpleDateFormat(ACRAConstants.DATE_TIME_FORMAT_STRING, Locale.ENGLISH).parse(timestamp));
                    return calendar;
                } catch (ParseException ignored) {
                }
            }
            return null;
        }
    }

    @NonNull
    private static String[] jsonArrayToList(@Nullable JSONArray array) {
        final List<String> list = new ArrayList<>();
        if (array != null) {
            final int length = array.length();
            for (int i = 0; i < length; i++) {
                list.add(String.valueOf(array.opt(i)));
            }
        }
        return list.toArray(new String[list.size()]);
    }
}
