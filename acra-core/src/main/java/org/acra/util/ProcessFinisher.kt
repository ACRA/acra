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
package org.acra.util

import android.content.Context
import android.content.Intent
import android.os.Process
import org.acra.builder.LastActivityManager
import org.acra.config.CoreConfiguration
import org.acra.log.debug
import org.acra.log.error
import org.acra.sender.JobSenderService
import org.acra.sender.LegacySenderService
import org.acra.util.SystemServices.ServiceNotReachedException
import org.acra.util.SystemServices.getActivityManager
import kotlin.system.exitProcess

/**
 * Takes care of cleaning up a process and killing it.
 *
 * @author F43nd1r
 * @since 4.9.2
 */
class ProcessFinisher(private val context: Context, private val config: CoreConfiguration, private val lastActivityManager: LastActivityManager) {
    fun endApplication() {
        stopServices()
        killProcessAndExit()
    }

    fun finishLastActivity(uncaughtExceptionThread: Thread?) {
        debug { "Finishing activities prior to killing the Process" }
        var wait = false
        for (activity in lastActivityManager.lastActivities) {
            val finisher = Runnable {
                activity.finish()
                debug { "Finished ${activity.javaClass}" }
            }
            if (uncaughtExceptionThread === activity.mainLooper.thread) {
                finisher.run()
            } else {
                // A crashed activity won't continue its lifecycle. So we only wait if something else crashed
                wait = true
                activity.runOnUiThread(finisher)
            }
        }
        if (wait) {
            lastActivityManager.waitForAllActivitiesDestroy(100)
        }
        lastActivityManager.clearLastActivities()
    }

    @Suppress("DEPRECATION")
    private fun stopServices() {
        if (config.stopServicesOnCrash) {
            try {
                val activityManager = getActivityManager(context)
                val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
                val pid = Process.myPid()
                for (serviceInfo in runningServices) {
                    if (serviceInfo.pid == pid && LegacySenderService::class.java.name != serviceInfo.service.className
                            && JobSenderService::class.java.name != serviceInfo.service.className) {
                        try {
                            val intent = Intent()
                            intent.component = serviceInfo.service
                            context.stopService(intent)
                        } catch (e: SecurityException) {
                            debug { "Unable to stop Service ${serviceInfo.service.className}. Permission denied" }
                        }
                    }
                }
            } catch (e: ServiceNotReachedException) {
                error(e) { "Unable to stop services" }
            }
        }
    }

    private fun killProcessAndExit() {
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }
}