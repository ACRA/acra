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
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import com.google.auto.service.AutoService;
import org.acra.builder.LastActivityManager;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.ReportingAdministrator;
import org.acra.config.SchedulerConfiguration;
import org.acra.plugins.HasConfigPlugin;

/**
 * @author F43nd1r
 * @since 07.05.18
 */
@AutoService(ReportingAdministrator.class)
public class RestartingAdministrator extends HasConfigPlugin implements ReportingAdministrator {
    public static final String EXTRA_LAST_ACTIVITY = "lastActivity";

    public RestartingAdministrator() {
        super(SchedulerConfiguration.class);
    }

    @Override
    public boolean shouldFinishActivity(@NonNull Context context, @NonNull CoreConfiguration config, LastActivityManager lastActivityManager) {
        if (ConfigUtils.getPluginConfiguration(config, SchedulerConfiguration.class).restartAfterCrash() && lastActivityManager.getLastActivity() != null) {
            Thread thread = new Thread(() -> {
                try {
                    JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    assert scheduler != null;
                    PersistableBundle extras = new PersistableBundle();
                    extras.putString(RestartingAdministrator.EXTRA_LAST_ACTIVITY, lastActivityManager.getLastActivity().getClass().getName());
                    scheduler.schedule(new JobInfo.Builder(0, new ComponentName(context, RestartingService.class)).setExtras(extras).build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
        return true;
    }
}
