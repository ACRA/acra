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
package org.acra.builder

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import org.acra.collections.WeakStack
import org.acra.log.debug
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Responsible for tracking the last Activity that was created.
 *
 * @since 4.8.0
 */
class LastActivityManager(application: Application) {
    private val activityStack = WeakStack<Activity>()
    private val lock = ReentrantLock()
    private val destroyedCondition = lock.newCondition()

    /**
     * @return last created activity, if any
     */
    val lastActivity: Activity?
        get() = activityStack.peek()

    /**
     * @return a list of activities in the current process
     */
    val lastActivities: List<Activity>
        get() = ArrayList(activityStack)

    /**
     * clear saved activities
     */
    fun clearLastActivities() {
        activityStack.clear()
    }

    /**
     * wait until the last activity is stopped
     *
     * @param timeOutInMillis timeout for wait
     */
    fun waitForAllActivitiesDestroy(timeOutInMillis: Int) {
        lock.withLock {
            val start = System.currentTimeMillis()
            var now = start
            while (!activityStack.isEmpty() && start + timeOutInMillis > now) {
                destroyedCondition.await(start - now + timeOutInMillis, TimeUnit.MILLISECONDS)
                now = System.currentTimeMillis()
            }
        }
    }

    /**
     * Create and register a new instance
     *
     * @param application the application to attach to
     */
    init {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                debug { "onActivityCreated ${activity.javaClass}" }
                activityStack.add(activity)
            }

            override fun onActivityStarted(activity: Activity) {
                debug { "onActivityStarted ${activity.javaClass}" }
            }

            override fun onActivityResumed(activity: Activity) {
                debug { "onActivityResumed ${activity.javaClass}" }
            }

            override fun onActivityPaused(activity: Activity) {
                debug { "onActivityPaused ${activity.javaClass}" }
            }

            override fun onActivityStopped(activity: Activity) {
                debug { "onActivityStopped ${activity.javaClass}" }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                debug { "onActivitySaveInstanceState ${activity.javaClass}" }
            }

            override fun onActivityDestroyed(activity: Activity) {
                debug { "onActivityDestroyed ${activity.javaClass}" }
                lock.withLock {
                    activityStack.remove(activity)
                    destroyedCondition.signalAll()
                }
            }
        })
    }
}