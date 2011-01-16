/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
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

import android.os.Build;

public class Compatibility {
    static int getAPILevel() {
        int apiLevel;
        try {
            Field SDK_INT = Build.VERSION.class.getField("SDK_INT");
            apiLevel = SDK_INT.getInt(null);
        } catch (SecurityException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (NoSuchFieldException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (IllegalArgumentException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (IllegalAccessException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        }

        return apiLevel;
    }
}
