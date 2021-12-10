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

import com.faendir.kotlin.autodsl.AutoDsl
import org.acra.annotation.AcraDsl
import org.acra.ktx.plus
import java.util.concurrent.TimeUnit

/**
 * Limiter configuration
 *
 * @author F43nd1r
 * @since 26.10.2017
 */
@AutoDsl(dslMarker = AcraDsl::class)
class LimiterConfiguration(
    /**
     * enables this plugin
     */
    val enabled: Boolean = true,
    /**
     * Unit of [period]
     *
     * @since 5.0.0
     */
    val periodUnit: TimeUnit = TimeUnit.DAYS,

    /**
     * number of [periodUnit]s in which to limit reports
     *
     * Reports which have been collected before this will not be considered for any limits except [failedReportLimit]
     *
     * @since 5.0.0
     */
    val period: Long = 7,

    /**
     * general limit of reports per period
     *
     * @since 5.0.0
     */
    val overallLimit: Int = 25,

    /**
     * limit for reports with the same stacktrace per period
     *
     * @since 5.0.0
     */
    val stacktraceLimit: Int = 3,

    /**
     * limit for reports with the same exception class per period
     *
     * @since 5.0.0
     */
    val exceptionClassLimit: Int = 10,

    /**
     * limit for unsent reports to keep
     *
     * @since 5.0.0
     */
    val failedReportLimit: Int = 5,

    /**
     * toast shown when a report was not collected or sent because a limit was exceeded
     *
     * @since 5.0.0
     */
    val ignoredCrashToast: String? = null,

    /**
     * This property can be used to determine whether old (out of date) reports should be sent or not.
     *
     * @since 5.3.0
     */
    val deleteReportsOnAppUpdate: Boolean = true,

    /**
     * Resetting limits after an app update allows you to see if a bug still exists.
     *
     * @since 5.3.0
     */
    val resetLimitsOnAppUpdate: Boolean = true,
) : Configuration {
    override fun enabled(): Boolean = enabled
}

fun CoreConfigurationBuilder.limiter(initializer: LimiterConfigurationBuilder.() -> Unit) {
    pluginConfigurations += LimiterConfigurationBuilder().apply(initializer).build()
}
