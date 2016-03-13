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
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Wraps an {@link ActivityLifecycleCallbacksCompat} into an {@link ActivityLifecycleCallbacks}.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class ActivityLifecycleCallbacksWrapper implements ActivityLifecycleCallbacks {

    private final ActivityLifecycleCallbacksCompat mCallback;

    public ActivityLifecycleCallbacksWrapper(@NonNull ActivityLifecycleCallbacksCompat callback) {
        mCallback = callback;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mCallback.onActivityCreated(activity, savedInstanceState);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        mCallback.onActivityStarted(activity);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        mCallback.onActivityResumed(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        mCallback.onActivityPaused(activity);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        mCallback.onActivityStopped(activity);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, Bundle outState) {
        mCallback.onActivitySaveInstanceState(activity, outState);
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        mCallback.onActivityDestroyed(activity);
    }

    /**
     * Compare the current wrapped callback with another object wrapped callback
     */
    @Override
    public boolean equals(Object object) {
        if( !(object instanceof ActivityLifecycleCallbacksWrapper) ) {
            return false;
        }
        final ActivityLifecycleCallbacksWrapper that = (ActivityLifecycleCallbacksWrapper) object;
        return mCallback.equals( that.mCallback );
    }

    /**
     *
     * return wrapped callback object hashCode
     */
    @Override
    public int hashCode() {
        return mCallback.hashCode();
    }
}
