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
package org.acra.builder;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.acra.ACRA;

import java.lang.ref.WeakReference;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for tracking the last Activity that was created.
 *
 * @since 4.8.0
 */
public final class LastActivityManager {

    @NonNull
    private WeakReference<Activity> lastActivityCreated = new WeakReference<>(null);

    /**
     * Create and register a new instance
     *
     * @param application the application to attach to
     */
    public LastActivityManager(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityCreated " + activity.getClass());
                lastActivityCreated = new WeakReference<>(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityStarted " + activity.getClass());
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityResumed " + activity.getClass());
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityPaused " + activity.getClass());
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityStopped " + activity.getClass());
                synchronized (this) {
                    notify();
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, Bundle outState) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivitySaveInstanceState " + activity.getClass());
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityDestroyed " + activity.getClass());
            }
        });
    }

    /**
     * @return last created activity, if any
     */
    @Nullable
    public Activity getLastActivity() {
        return lastActivityCreated.get();
    }

    /**
     * clear saved activity
     */
    public void clearLastActivity() {
        lastActivityCreated.clear();
    }

    /**
     * wait until the last activity is stopped
     *
     * @param timeOutInMillis timeout for wait
     */
    public void waitForActivityStop(int timeOutInMillis) {
        synchronized (this) {
            try {
                wait(timeOutInMillis);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
