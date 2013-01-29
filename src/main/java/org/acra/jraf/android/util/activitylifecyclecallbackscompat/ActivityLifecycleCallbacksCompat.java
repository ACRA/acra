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
 * Copyright (C) 2006 The Android Open Source Project
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

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

/**
 * Equivalent of {@link ActivityLifecycleCallbacks} to be used with
 * {@link ApplicationHelper#registerActivityLifecycleCallbacks(Application, ActivityLifecycleCallbacksCompat)} and
 * {@link ApplicationHelper#unregisterActivityLifecycleCallbacks(Application, ActivityLifecycleCallbacksCompat)}.
 */
public interface ActivityLifecycleCallbacksCompat {
    void onActivityCreated(Activity activity, Bundle savedInstanceState);

    void onActivityStarted(Activity activity);

    void onActivityResumed(Activity activity);

    void onActivityPaused(Activity activity);

    void onActivityStopped(Activity activity);

    void onActivitySaveInstanceState(Activity activity, Bundle outState);

    void onActivityDestroyed(Activity activity);
}
