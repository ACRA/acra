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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import androidx.work.*;
import com.google.auto.service.AutoService;
import org.acra.ACRA;
import org.acra.builder.LastActivityManager;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.ReportingAdministrator;
import org.acra.config.SchedulerConfiguration;
import org.acra.plugins.HasConfigPlugin;

import java.util.concurrent.TimeUnit;

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
                    WorkManager.getInstance().enqueue(new OneTimeWorkRequest.Builder(RestartJob.class)
                            .setInputData(new Data.Builder().putString(RestartingAdministrator.EXTRA_LAST_ACTIVITY, lastActivityManager.getLastActivity().getClass().getName()).build())
                            .setInitialDelay(1, TimeUnit.MILLISECONDS)
                            .build()).getResult().get();
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

    public static class RestartJob extends Worker {
        public RestartJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            String className = getInputData().getString(RestartingAdministrator.EXTRA_LAST_ACTIVITY);
            if (className != null) {
                try {
                    //noinspection unchecked
                    Class<? extends Activity> activityClass = (Class<? extends Activity>) Class.forName(className);
                    final Intent intent = new Intent(getApplicationContext(), activityClass);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                } catch (ClassNotFoundException e) {
                    ACRA.log.w(ACRA.LOG_TAG, "Unable to find activity class" + className);
                }
            }
            return Result.success();
        }
    }
}
