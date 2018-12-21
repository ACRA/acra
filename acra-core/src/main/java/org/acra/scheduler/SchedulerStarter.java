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
import android.support.annotation.Nullable;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.file.ReportLocator;

import java.io.File;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
public class SchedulerStarter {

    private final ReportLocator locator;
    private final SenderScheduler senderScheduler;

    public SchedulerStarter(@NonNull Context context, @NonNull CoreConfiguration config) {
        locator = new ReportLocator(context);
        List<SenderSchedulerFactory> schedulerFactories = config.pluginLoader().loadEnabled(config, SenderSchedulerFactory.class);
        if (schedulerFactories.isEmpty()) {
            senderScheduler = new DefaultSenderScheduler(context, config);
        } else {
            senderScheduler = schedulerFactories.get(0).create(context, config);
            if (schedulerFactories.size() > 1) ACRA.log.w(ACRA.LOG_TAG, "More than one SenderScheduler found. Will use only " + senderScheduler.getClass().getSimpleName());
        }
    }

    /**
     * Starts a process to start sending outstanding error reports.
     *
     * @param report                If not null, this report will be approved before scheduling.
     * @param onlySendSilentReports If true then only send silent reports.
     */
    public void scheduleReports(@Nullable File report, boolean onlySendSilentReports) {
        if (report != null) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Mark " + report.getName() + " as approved.");
            final File approvedReport = new File(locator.getApprovedFolder(), report.getName());
            if (!report.renameTo(approvedReport)) {
                ACRA.log.w(LOG_TAG, "Could not rename approved report from " + report + " to " + approvedReport);
            }
        }
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Schedule report sending");
        senderScheduler.scheduleReportSending(onlySendSilentReports);
    }

    public SenderScheduler getSenderScheduler() {
        return senderScheduler;
    }
}
