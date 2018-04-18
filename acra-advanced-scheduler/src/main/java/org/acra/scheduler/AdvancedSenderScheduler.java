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

import android.content.Context;
import android.support.annotation.NonNull;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.auto.service.AutoService;
import org.acra.ACRA;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.SchedulerConfiguration;
import org.acra.plugins.ConfigBasedAllowsDisablePlugin;
import org.acra.sender.SenderService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
@AutoService(SenderScheduler.class)
public class AdvancedSenderScheduler extends ConfigBasedAllowsDisablePlugin implements SenderScheduler {

    public AdvancedSenderScheduler() {
        super(SchedulerConfiguration.class);
    }

    @Override
    public void scheduleReportSending(@NonNull Context context, @NonNull CoreConfiguration config, boolean onlySendSilentReports) {
        SchedulerConfiguration schedulerConfiguration = ConfigUtils.getPluginConfiguration(config, SchedulerConfiguration.class);
        PersistableBundleCompat extras = new PersistableBundleCompat();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try(ObjectOutputStream out = new ObjectOutputStream(bytes)){
            out.writeObject(config);
        } catch (IOException e) {
            ACRA.log.w("Failed to write config to string", e);
        }
        extras.putString(SenderService.EXTRA_ACRA_CONFIG, android.util.Base64.encodeToString(bytes.toByteArray(), android.util.Base64.DEFAULT));
        extras.putBoolean(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
        new JobRequest.Builder(ReportJob.TAG)
                .setExecutionWindow(1, TimeUnit.MINUTES.toMillis(1))
                .setExtras(extras)
                .setRequirementsEnforced(true)
                .setRequiredNetworkType(schedulerConfiguration.requiresNetworkType())
                .setRequiresCharging(schedulerConfiguration.requiresCharging())
                .setRequiresDeviceIdle(schedulerConfiguration.requiresDeviceIdle())
                .setRequiresBatteryNotLow(schedulerConfiguration.requiresBatteryNotLow())
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    @Override
    public void setUp(@NonNull Context context, @NonNull CoreConfiguration configuration) {
        JobManager.create(context).addJobCreator(new ReportJobCreator());
    }
}
