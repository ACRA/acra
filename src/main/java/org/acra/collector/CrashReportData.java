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
import org.acra.model.BooleanElement;
import org.acra.model.Element;
import org.acra.model.NumberElement;
import org.acra.model.StringElement;
import org.acra.util.JsonUtils;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Properties;

/**
 * Stores a crash reports data with {@link org.acra.ReportField} enum values as keys.
 * <p>
 * This is basically the source of {@link Properties} adapted to extend an
 * EnumMap instead of Hashtable and with a few tweaks to avoid losing crazy
 * amounts of android time in the generation of a date comment when storing to file.
 */
public final class CrashReportData extends EnumMap<ReportField, Element> {

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
        return super.get(key).toString();
    }

    public void putString(@NonNull ReportField key, String value) {
        put(key, new StringElement(value));
    }

    public void putNumber(@NonNull ReportField key, Number value) {
        put(key, new NumberElement(value));
    }

    public void putBoolean(@NonNull ReportField key, boolean value) {
        put(key, new BooleanElement(value));
    }

    @NonNull
    public JSONObject toJSON() {
        return JsonUtils.toJson(this);
    }

}
