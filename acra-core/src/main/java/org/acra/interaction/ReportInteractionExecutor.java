/*
 * Copyright (c) 2017
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

package org.acra.interaction;

import android.content.Context;
import android.support.annotation.NonNull;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.acra.ACRA.LOG_TAG;

/**
 * Manages and executes all report interactions
 *
 * @author F43nd1r
 * @since 10.10.2017
 */

public class ReportInteractionExecutor {
    private final List<ReportInteraction> reportInteractions;
    private final Context context;
    private final CoreConfiguration config;

    public ReportInteractionExecutor(@NonNull final Context context, @NonNull final CoreConfiguration config) {
        this.context = context;
        this.config = config;
        reportInteractions = new ArrayList<>();
        //noinspection ForLoopReplaceableByForEach
        for (final Iterator<ReportInteraction> iterator = ServiceLoader.load(ReportInteraction.class, getClass().getClassLoader()).iterator(); iterator.hasNext(); ) {
            try {
                final ReportInteraction reportInteraction = iterator.next();
                if (reportInteraction.enabled(config)) {
                    reportInteractions.add(reportInteraction);
                }else if (ACRA.DEV_LOGGING) {
                    ACRA.log.d(LOG_TAG, "Ignoring disabled ReportInteraction of type " + reportInteraction.getClass().getSimpleName());
                }
            } catch (ServiceConfigurationError e) {
                ACRA.log.e(ACRA.LOG_TAG, "Unable to load ReportInteraction", e);
            }
        }
    }

    public boolean hasInteractions() {
        return reportInteractions.size() > 0;
    }

    public boolean performInteractions(@NonNull final File reportFile) {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final List<Future<Boolean>> futures = new ArrayList<>();
        for (final ReportInteraction reportInteraction : reportInteractions) {
            futures.add(executorService.submit(() -> {
                if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Calling ReportInteraction of class " + reportInteraction.getClass().getName());
                return reportInteraction.performInteraction(context, config, reportFile);
            }));
        }
        boolean sendReports = true;
        for (Future<Boolean> future : futures) {
            while (!future.isDone()) {
                try {
                    sendReports &= future.get();
                } catch (InterruptedException ignored) {
                } catch (ExecutionException e) {
                    //ReportInteraction crashed, so ignore it
                    break;
                }
            }
        }
        return sendReports;
    }
}
