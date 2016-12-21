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

package org.acra.model;

import android.support.annotation.NonNull;

import org.acra.util.JsonUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author F43nd1r
 * @since 12.10.2016
 */
public class ComplexElement extends JSONObject implements Element {
    public ComplexElement() {
    }

    public ComplexElement(String json) throws JSONException {
        super(json);
    }

    public ComplexElement(Map<String, ?> copyFrom) {
        super(copyFrom);
    }

    public ComplexElement(JSONObject copyFrom) throws JSONException {
        super(copyFrom, getNames(copyFrom));
    }

    @NonNull
    private static String[] getNames(JSONObject object) throws JSONException {
        JSONArray json = object.names();
        if(json != null) {
            String[] names = new String[json.length()];
            for (int i = 0; i < json.length(); i++) {
                names[i] = json.getString(i);
            }
            return names;
        }
        return new String[0];
    }

    @Override
    public Object value() {
        return this;
    }

    @Override
    public String[] flatten() {
        try {
            return JsonUtils.flatten(this).toArray(new String[0]);
        } catch (JSONException e) {
            return new String[0];
        }
    }
}
