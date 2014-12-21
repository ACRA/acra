/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acra.jraf.android.util.activitylifecyclecallbackscompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Build;

/**
 * Helper for accessing {@link Application#registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks)} and
 * {@link Application#unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks)} introduced in API level 14 in a
 * backwards compatible fashion.<br>
 * When running on API level 14 or above, the framework's implementations of these methods will be used.
 */
public class ApplicationHelper {
    public static final boolean PRE_ICS = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;

    /*
     * Register.
     */

    /**
     * Registers a callback to be called following the life cycle of the application's {@link Activity activities}.
     * 
     * @param application The application with which to register the callback.
     * @param callback The callback to register.
     */
    public static void registerActivityLifecycleCallbacks(Application application, ActivityLifecycleCallbacksCompat callback) {
        if (PRE_ICS) {
            preIcsRegisterActivityLifecycleCallbacks(callback);
        } else {
            postIcsRegisterActivityLifecycleCallbacks(application, callback);
        }
    }

    private static void preIcsRegisterActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback) {
        MainLifecycleDispatcher.get().registerActivityLifecycleCallbacks(callback);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void postIcsRegisterActivityLifecycleCallbacks(Application application, ActivityLifecycleCallbacksCompat callback) {
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksWrapper(callback));
    }


    /*
     * Unregister.
     */

    /**
     * Unregisters a previously registered callback.
     * 
     * @param application The application with which to unregister the callback.
     * @param callback The callback to unregister.
     */
    public void unregisterActivityLifecycleCallbacks(Application application, ActivityLifecycleCallbacksCompat callback) {
        if (PRE_ICS) {
            preIcsUnregisterActivityLifecycleCallbacks(callback);
        } else {
            postIcsUnregisterActivityLifecycleCallbacks(application, callback);
        }
    }

    private static void preIcsUnregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback) {
        MainLifecycleDispatcher.get().unregisterActivityLifecycleCallbacks(callback);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void postIcsUnregisterActivityLifecycleCallbacks(Application application, ActivityLifecycleCallbacksCompat callback) {
        application.unregisterActivityLifecycleCallbacks(new ActivityLifecycleCallbacksWrapper(callback));
    }

}
