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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.sender.SenderService;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
public class DefaultSenderScheduler implements SenderScheduler {
    @Override
    public void scheduleReportSending(@NonNull Context context, @NonNull CoreConfiguration config, boolean onlySendSilentReports) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start SenderService");
        final Intent intent = new Intent();
        intent.putExtra(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
        intent.putExtra(SenderService.EXTRA_ACRA_CONFIG, config);
        JobIntentService.enqueueWork(context, SenderService.class, 0, intent);
    }
}
