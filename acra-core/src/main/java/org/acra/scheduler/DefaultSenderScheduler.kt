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
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import org.acra.config.CoreConfiguration
import org.acra.sender.JobSenderService
import org.acra.sender.LegacySenderService
import org.acra.sender.ReportSender
import org.acra.sender.SendingConductor
import org.acra.util.IOUtils
import org.acra.util.toPersistableBundle

/**
 * Simply schedules sending instantly
 *
 * @author F43nd1r
 * @since 18.04.18
 */
@Suppress("MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")
open class DefaultSenderScheduler(private val context: Context, private val config: CoreConfiguration) : SenderScheduler {
    override fun scheduleReportSending(onlySendSilentReports: Boolean) {
        val extras = Bundle()
        extras.putString(LegacySenderService.EXTRA_ACRA_CONFIG, IOUtils.serialize(config))
        extras.putBoolean(LegacySenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports)
        configureExtras(extras)
        if (ReportSender.hasBackgroundSenders(context, config)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val scheduler = (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler)
                val builder = JobInfo.Builder(0, ComponentName(context, JobSenderService::class.java)).setExtras(extras.toPersistableBundle())
                configureJob(builder)
                scheduler.schedule(builder.build())
            } else {
                val intent = Intent()
                intent.putExtras(extras)
                intent.component = ComponentName(context, LegacySenderService::class.java)
                context.startService(intent)
            }
        }
        if (ReportSender.hasForegroundSenders(context, config)) {
            SendingConductor(context, config).sendReports(true, extras)
        }
    }

    /**
     * allows to perform additional configuration in subclasses
     *
     * @param job the job builder
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected open fun configureJob(job: JobInfo.Builder) {
        job.setOverrideDeadline(0)
    }

    /**
     * allows to provide additional extras to senders
     *
     * @param extras the extras bundle
     */
    protected fun configureExtras(extras: Bundle) {}
}