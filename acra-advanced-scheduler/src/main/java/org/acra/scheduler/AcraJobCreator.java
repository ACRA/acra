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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.sender.SenderService;

/**
 * @author F43nd1r
 * @since 07.05.18
 */
class AcraJobCreator implements JobCreator {
    static final String REPORT_TAG = "org.acra.report.Job";
    static final String RESTART_TAG = "org.acra.restart.Job";
    private final CoreConfiguration config;

    public AcraJobCreator(CoreConfiguration config) {
        this.config = config;
    }

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case REPORT_TAG:
                return new Job() {
                    @NonNull
                    @Override
                    protected Result onRunJob(@NonNull Params params) {
                        boolean sendOnlySilentReports = params.getExtras().getBoolean(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, false);
                        new DefaultSenderScheduler(getContext(), config).scheduleReportSending(sendOnlySilentReports);
                        return Result.SUCCESS;
                    }
                };
            case RESTART_TAG:
                return new Job() {
                    @NonNull
                    @Override
                    protected Result onRunJob(@NonNull Params params) {
                        String className = params.getExtras().getString(RestartingAdministrator.EXTRA_LAST_ACTIVITY, null);
                        if (className != null) {
                            try {
                                //noinspection unchecked
                                Class<? extends Activity> activityClass = (Class<? extends Activity>) Class.forName(className);
                                final Intent intent = new Intent(getContext(), activityClass);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(intent);
                            } catch (ClassNotFoundException e) {
                                ACRA.log.w(ACRA.LOG_TAG, "Unable to find activity class" + className);
                            }
                        }
                        return Result.SUCCESS;
                    }
                };
            default:
                return null;
        }
    }
}
