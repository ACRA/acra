/*
 * Copyright (c) 2021
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

package org.acra.config

import android.app.job.JobInfo
import com.faendir.kotlin.autodsl.AutoDsl
import org.acra.annotation.AcraDsl
import org.acra.ktx.plus

/**
 * @author F43nd1r
 * @since 18.04.18
 */
@AutoDsl(dslMarker = AcraDsl::class)
class SchedulerConfiguration(
    /**
     * enables this plugin
     */
    val enabled: Boolean = true,
    /**
     * Network constraint for report sending
     * @since 5.2.0
     */
    val requiresNetworkType: Int = JobInfo.NETWORK_TYPE_NONE,

    /**
     * Charging constraint for report sending
     * @since 5.2.0
     */
    val requiresCharging: Boolean = false,

    /**
     * Idle constraint for report sending
     * @since 5.2.0
     */
    val requiresDeviceIdle: Boolean = false,

    /**
     * Battery constraint for report sending
     * @since 5.2.0
     */
    val requiresBatteryNotLow: Boolean = false,

    /**
     * Restarts the last activity immediately after a crash.
     * If an activity is restarted, the [org.acra.scheduler.RestartingAdministrator.EXTRA_ACTIVITY_RESTART_AFTER_CRASH] extra will contain a boolean true.
     * Note that this might interact badly with the crash dialog.
     * @since 5.2.0
     */
    val restartAfterCrash: Boolean = false,
) : Configuration {
    override fun enabled(): Boolean = enabled
}

fun CoreConfigurationBuilder.scheduler(initializer: SchedulerConfigurationBuilder.() -> Unit) {
    pluginConfigurations += SchedulerConfigurationBuilder().apply(initializer).build()
}
