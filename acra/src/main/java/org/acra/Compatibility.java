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

import android.content.Context;
import android.os.Build;

/**
 * Utility class containing methods enabling backward compatibility.
 * 
 * @author Normal
 * 
 */
public class Compatibility {

    /**
     * Retrieves Android SDK API level using the best possible method.
     * 
     * @return The Android SDK API int level.
     */
    static int getAPILevel() {
        int apiLevel;
        try {
            // This field has been added in Android 1.6
            final Field SDK_INT = Build.VERSION.class.getField("SDK_INT");
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

    /**
     * Retrieve the DropBoxManager service name using reflection API.
     */
    static String getDropBoxServiceName() throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        final Field serviceName = Context.class.getField("DROPBOX_SERVICE");
        if (serviceName != null) {
            return (String) serviceName.get(null);
        }
        return null;
    }
}
