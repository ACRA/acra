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

import org.acra.config.CoreConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author F43nd1r
 * @since 10.10.2017
 */

public class ReportInteractionExecutor {
    private final List<ReportInteraction> reportInteractions;

    public ReportInteractionExecutor() {
        reportInteractions = new ArrayList<>();
        for (ReportInteraction interaction : ServiceLoader.load(ReportInteraction.class)) {
            reportInteractions.add(interaction);
        }
    }

    public boolean hasInteractions() {
        return reportInteractions.size() > 0;
    }

    public boolean performInteractions(@NonNull final Context context, @NonNull final CoreConfiguration config, @NonNull final File reportFile) {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final List<Future<Boolean>> futures = new ArrayList<>();
        for (final ReportInteraction reportInteraction : reportInteractions) {
            futures.add(executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return reportInteraction.performInteraction(context, config, reportFile);
                }
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
