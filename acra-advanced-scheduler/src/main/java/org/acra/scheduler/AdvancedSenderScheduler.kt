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
import android.content.Context
import android.os.Build
import com.google.auto.service.AutoService
import org.acra.config.ConfigUtils.getPluginConfiguration
import org.acra.config.CoreConfiguration
import org.acra.config.SchedulerConfiguration
import org.acra.plugins.HasConfigPlugin

/**
 * Utilizes jobservice to delay report sending
 *
 * @author F43nd1r
 * @since 18.04.18
 */
class AdvancedSenderScheduler private constructor(context: Context, config: CoreConfiguration) : DefaultSenderScheduler(context, config) {
    private val schedulerConfiguration: SchedulerConfiguration = getPluginConfiguration(config, SchedulerConfiguration::class.java)
    override fun configureJob(job: JobInfo.Builder) {
        job.setRequiredNetworkType(schedulerConfiguration.requiresNetworkType)
        job.setRequiresCharging(schedulerConfiguration.requiresCharging)
        job.setRequiresDeviceIdle(schedulerConfiguration.requiresDeviceIdle)
        var constrained = schedulerConfiguration.requiresNetworkType != JobInfo.NETWORK_TYPE_NONE || schedulerConfiguration.requiresCharging || schedulerConfiguration.requiresDeviceIdle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            job.setRequiresBatteryNotLow(schedulerConfiguration.requiresBatteryNotLow)
            constrained = constrained or schedulerConfiguration.requiresBatteryNotLow
        }
        if (!constrained) {
            job.setOverrideDeadline(0)
        }
    }

    @AutoService(SenderSchedulerFactory::class)
    class Factory : HasConfigPlugin(SchedulerConfiguration::class.java), SenderSchedulerFactory {
        override fun create(context: Context, config: CoreConfiguration): SenderScheduler {
            return AdvancedSenderScheduler(context, config)
        }
    }

}