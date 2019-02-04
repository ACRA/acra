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
import org.acra.collections.WeakStack;

import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for tracking the last Activity that was created.
 *
 * @since 4.8.0
 */
public final class LastActivityManager {

    @NonNull
    private final WeakStack<Activity> activityStack = new WeakStack<>();

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
                activityStack.add(activity);
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
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, Bundle outState) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivitySaveInstanceState " + activity.getClass());
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityDestroyed " + activity.getClass());
                synchronized (activityStack) {
                    activityStack.remove(activity);
                    activityStack.notify();
                }
            }
        });
    }

    /**
     * @return last created activity, if any
     */
    @Nullable
    public Activity getLastActivity() {
        return activityStack.peek();
    }

    /**
     * @return a list of activities in the current process
     */
    @NonNull
    public List<Activity> getLastActivities() {
        return new ArrayList<>(activityStack);
    }

    /**
     * clear saved activities
     */
    public void clearLastActivities() {
        activityStack.clear();
    }

    /**
     * wait until the last activity is stopped
     *
     * @param timeOutInMillis timeout for wait
     */
    public void waitForAllActivitiesDestroy(int timeOutInMillis) {
        synchronized (activityStack) {
            long start = System.currentTimeMillis();
            long now = start;
            while (!activityStack.isEmpty() && start + timeOutInMillis > now) {
                try {
                    activityStack.wait(start - now + timeOutInMillis);
                } catch (InterruptedException ignored) {
                }
                now = System.currentTimeMillis();
            }
        }
    }
}
