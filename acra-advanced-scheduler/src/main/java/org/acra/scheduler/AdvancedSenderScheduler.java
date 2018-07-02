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
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.SchedulerConfiguration;
import org.acra.file.ReportLocator;
import org.acra.plugins.HasConfigPlugin;
import org.acra.sender.SenderService;

import java.util.concurrent.TimeUnit;

/**
 * Utilizes evernotes android-job to delay report sending
 *
 * @author F43nd1r
 * @since 18.04.18
 */
public class AdvancedSenderScheduler implements SenderScheduler {
    private final Context context;
    private final CoreConfiguration config;

    private AdvancedSenderScheduler(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public void scheduleReportSending(boolean onlySendSilentReports) {
        if(new ReportLocator(context).getApprovedReports().length == 0) {
            return;
        }
        SchedulerConfiguration schedulerConfiguration = ConfigUtils.getPluginConfiguration(config, SchedulerConfiguration.class);
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putBoolean(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
        new JobRequest.Builder(AcraJobCreator.REPORT_TAG)
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

    @AutoService(SenderSchedulerFactory.class)
    public static class Factory extends HasConfigPlugin implements SenderSchedulerFactory {

        public Factory() {
            super(SchedulerConfiguration.class);
        }

        @NonNull
        @Override
        public SenderScheduler create(@NonNull Context context, @NonNull CoreConfiguration config) {
            JobManager.create(context).addJobCreator(new AcraJobCreator(config));
            return new AdvancedSenderScheduler(context, config);
        }

    }

}
