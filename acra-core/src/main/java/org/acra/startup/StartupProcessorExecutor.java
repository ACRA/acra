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

package org.acra.startup;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;
import org.acra.interaction.ReportInteractionExecutor;
import org.acra.scheduler.SchedulerStarter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author lukas
 * @since 15.09.18
 */
public class StartupProcessorExecutor {
    private final Context context;
    private final CoreConfiguration config;
    private final ReportLocator reportLocator;
    private final SchedulerStarter schedulerStarter;
    private final CrashReportFileNameParser fileNameParser;

    public StartupProcessorExecutor(@NonNull final Context context, @NonNull final CoreConfiguration config, @NonNull SchedulerStarter schedulerStarter) {
        this.context = context;
        this.config = config;
        this.reportLocator = new ReportLocator(context);
        this.schedulerStarter = schedulerStarter;
        this.fileNameParser = new CrashReportFileNameParser();
    }

    public void processReports(boolean isAcraEnabled) {
        final Calendar now = Calendar.getInstance();
        //application is not ready in onAttachBaseContext, so delay this. also run it on a background thread because we're doing disk I/O
        new Handler(context.getMainLooper()).post(() -> new Thread(() -> {
            final List<Report> reports = new ArrayList<>();
            for (File r : reportLocator.getUnapprovedReports()) {
                reports.add(new Report(r, false));
            }
            for (File r : reportLocator.getApprovedReports()) {
                reports.add(new Report(r, true));
            }
            final List<StartupProcessor> startupProcessors = config.pluginLoader().loadEnabled(config, StartupProcessor.class);
            for (StartupProcessor processor : startupProcessors) {
                processor.processReports(context, config, reports);
            }
            boolean send = false;
            for (Report report : reports) {
                // ignore reports that were just created for now, they might be handled in another thread
                if (fileNameParser.getTimestamp(report.getFile().getName()).before(now)) {
                    if (report.isDelete()) {
                        if (!report.getFile().delete()) {
                            ACRA.log.w(LOG_TAG, "Could not delete report " + report.getFile());
                        }
                    } else if (report.isApproved()) {
                        send = true;
                    } else if (report.isApprove() && isAcraEnabled) {
                        new ReportInteractionExecutor(context, config).performInteractions(report.getFile());
                    }
                }
            }
            if(send && isAcraEnabled) {
                schedulerStarter.scheduleReports(null, false);
            }
        }).start());

    }
}
