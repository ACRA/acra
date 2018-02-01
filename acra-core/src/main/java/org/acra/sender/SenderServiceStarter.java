/*
 * Copyright (c) 2017 the ACRA team
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

package org.acra.sender;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import org.acra.ACRA;
import org.acra.config.CoreConfiguration;

import static org.acra.ACRA.LOG_TAG;

/**
 * Starts the Service(Intent)Service to process and send pending reports.
 */
public class SenderServiceStarter {

    private final Context context;
    private final CoreConfiguration config;

    public SenderServiceStarter(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Starts a process to start sending outstanding error reports.
     *
     * @param onlySendSilentReports If true then only send silent reports.
     * @param approveReportsFirst   If true then approve unapproved reports first.
     */
    public void startService(boolean onlySendSilentReports, boolean approveReportsFirst) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start SenderService");
        final Intent intent = new Intent();
        intent.putExtra(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
        intent.putExtra(SenderService.EXTRA_APPROVE_REPORTS_FIRST, approveReportsFirst);
        intent.putExtra(SenderService.EXTRA_ACRA_CONFIG, config);
        JobIntentService.enqueueWork(context, SenderService.class, 0, intent);
    }
}
