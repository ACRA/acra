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

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

/**
 * Keeps a list of {@link ActivityLifecycleCallbacksCompat}s that will be called following the life cycle of the application's {@link Activity activities}.
 * This class is used when the app is running on an older platform version that does not support
 * {@link Application#registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks)} and
 * {@link Application#unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks)}.
 */
public class MainLifecycleDispatcher implements ActivityLifecycleCallbacksCompat {
    private static final MainLifecycleDispatcher INSTANCE = new MainLifecycleDispatcher();

    public static MainLifecycleDispatcher get() {
        return INSTANCE;
    }

    private MainLifecycleDispatcher() {}

    private ArrayList<ActivityLifecycleCallbacksCompat> mActivityLifecycleCallbacks = new ArrayList<ActivityLifecycleCallbacksCompat>();

    /* package */void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback) {
        synchronized (mActivityLifecycleCallbacks) {
            mActivityLifecycleCallbacks.add(callback);
        }
    }

    /* package */void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback) {
        synchronized (mActivityLifecycleCallbacks) {
            mActivityLifecycleCallbacks.remove(callback);
        }
    }

    private Object[] collectActivityLifecycleCallbacks() {
        Object[] callbacks = null;
        synchronized (mActivityLifecycleCallbacks) {
            if (mActivityLifecycleCallbacks.size() > 0) {
                callbacks = mActivityLifecycleCallbacks.toArray();
            }
        }
        return callbacks;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((ActivityLifecycleCallbacksCompat) callback).onActivityCreated(activity, savedInstanceState);
            }
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((ActivityLifecycleCallbacksCompat) callback).onActivityStarted(activity);
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((ActivityLifecycleCallbacksCompat) callback).onActivityResumed(activity);
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((ActivityLifecycleCallbacksCompat) callback).onActivityPaused(activity);
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((ActivityLifecycleCallbacksCompat) callback).onActivityStopped(activity);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((ActivityLifecycleCallbacksCompat) callback).onActivitySaveInstanceState(activity, outState);
            }
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((ActivityLifecycleCallbacksCompat) callback).onActivityDestroyed(activity);
            }
        }
    }
}
