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

package org.acra.scheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import org.acra.config.CoreConfiguration;
import org.acra.sender.JobSenderService;
import org.acra.sender.LegacySenderService;
import org.acra.sender.SendingConductor;
import org.acra.util.IOUtils;

/**
 * Simply schedules sending instantly
 *
 * @author F43nd1r
 * @since 18.04.18
 */
public class DefaultSenderScheduler implements SenderScheduler {
    private final Context context;
    private final CoreConfiguration config;

    public DefaultSenderScheduler(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public void scheduleReportSending(boolean onlySendSilentReports) {
        SendingConductor conductor = new SendingConductor(context, config);
        if(!conductor.getSenderInstances(false).isEmpty()) {
            final Intent intent = new Intent();
            intent.putExtra(LegacySenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
            intent.putExtra(LegacySenderService.EXTRA_ACRA_CONFIG, config);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                PersistableBundle extras = new PersistableBundle();
                extras.putString(LegacySenderService.EXTRA_ACRA_CONFIG, IOUtils.serialize(config));
                extras.putBoolean(LegacySenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
                assert scheduler != null;
                JobInfo.Builder builder = new JobInfo.Builder(0, new ComponentName(context, JobSenderService.class)).setOverrideDeadline(0L).setExtras(extras);
                configureJob(builder);
                scheduler.schedule(builder.build());
            } else {
                intent.setComponent(new ComponentName(context, LegacySenderService.class));
                context.startService(intent);
            }
        }
        if(!conductor.getSenderInstances(true).isEmpty()) {
            conductor.sendReports(onlySendSilentReports, true);
        }
    }

    protected void configureJob(JobInfo.Builder job) {
    }
}
