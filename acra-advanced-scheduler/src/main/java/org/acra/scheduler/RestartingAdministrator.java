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
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.faendir.asl.annotation.AutoService;
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
            PersistableBundleCompat extras = new PersistableBundleCompat();
            extras.putString(EXTRA_LAST_ACTIVITY, lastActivityManager.getLastActivity().getClass().getName());
            new JobRequest.Builder(AcraJobCreator.RESTART_TAG)
                    .setExact(TimeUnit.SECONDS.toMillis(1))
                    //.setExecutionWindow(TimeUnit.SECONDS.toMillis(10), TimeUnit.MINUTES.toMillis(1))
                    .setExtras(extras)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule();
        }
        return true;
    }
}
