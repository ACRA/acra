/*
 * Copyright (c) 2018
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
package org.acra.scheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.google.auto.service.AutoService
import org.acra.builder.LastActivityManager
import org.acra.config.CoreConfiguration
import org.acra.config.ReportingAdministrator
import org.acra.config.SchedulerConfiguration
import org.acra.config.getPluginConfiguration
import org.acra.log.debug
import org.acra.log.info
import org.acra.log.warn
import org.acra.plugins.HasConfigPlugin

/**
 * @author F43nd1r
 * @since 07.05.18
 */
@AutoService(ReportingAdministrator::class)
class RestartingAdministrator : HasConfigPlugin(SchedulerConfiguration::class.java), ReportingAdministrator {
    override fun shouldFinishActivity(context: Context, config: CoreConfiguration, lastActivityManager: LastActivityManager): Boolean {
        debug { "RestartingAdministrator entry" }
        if (config.getPluginConfiguration<SchedulerConfiguration>().restartAfterCrash) {
            val activity = lastActivityManager.lastActivity
            if (activity != null) {
                debug { "Try to schedule last activity (" + activity.javaClass.name + ") for restart" }
                try {
                    val scheduler = (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler)
                    val extras = PersistableBundle()
                    extras.putString(EXTRA_LAST_ACTIVITY, activity.javaClass.name)
                    scheduler.schedule(JobInfo.Builder(1, ComponentName(context, RestartingService::class.java))
                            .setExtras(extras)
                            .setOverrideDeadline(100)
                            .build())
                    debug { "Successfully scheduled last activity (" + activity.javaClass.name + ") for restart" }
                } catch (e: Exception) {
                    warn(e) { "Failed to schedule last activity for restart" }
                }
            } else {
                info { "Activity restart is enabled but no activity was found. Nothing to do." }
            }
        }
        return true
    }

    companion object {
        const val EXTRA_LAST_ACTIVITY = "lastActivity"
        const val EXTRA_ACTIVITY_RESTART_AFTER_CRASH = "restartAfterCrash"
    }
}