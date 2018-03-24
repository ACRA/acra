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

package org.acra.data;

import android.content.Context;
import android.support.annotation.NonNull;
import org.acra.ACRA;
import org.acra.builder.ReportBuilder;
import org.acra.collector.ApplicationStartupCollector;
import org.acra.collector.Collector;
import org.acra.collector.CollectorException;
import org.acra.config.CoreConfiguration;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for collecting the CrashReportData for an Exception.
 *
 * @author F43nd1r
 * @since 4.3.0
 */
public final class CrashReportDataFactory {

    private final Context context;
    private final CoreConfiguration config;
    private final List<Collector> collectors;

    public CrashReportDataFactory(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
        collectors = new ArrayList<>();
        //noinspection ForLoopReplaceableByForEach need to catch exception in iterator.next()
        for (final Iterator<Collector> iterator = ServiceLoader.load(Collector.class, getClass().getClassLoader()).iterator(); iterator.hasNext(); ) {
            try {
                final Collector collector = iterator.next();
                if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Loaded collector of class " + collector.getClass().getName());
                collectors.add(collector);
            }catch (ServiceConfigurationError e){
                ACRA.log.e(LOG_TAG, "Unable to load collector", e);
            }
        }
        Collections.sort(collectors, (c1, c2) -> {
            Collector.Order o1;
            Collector.Order o2;
            try {
                o1 = c1.getOrder();
            } catch (Throwable t) {
                o1 = Collector.Order.NORMAL;
            }
            try {
                o2 = c2.getOrder();
            } catch (Throwable t) {
                o2 = Collector.Order.NORMAL;
            }
            return o1.ordinal() - o2.ordinal();
        });
    }

    /**
     * Collects crash data.
     *
     * @param builder ReportBuilder for whom to crete the crash report.
     * @return CrashReportData identifying the current crash.
     */
    @NonNull
    public CrashReportData createCrashData(@NonNull final ReportBuilder builder) {
        final ExecutorService executorService = config.parallel() ? Executors.newCachedThreadPool() : Executors.newSingleThreadExecutor();
        final CrashReportData crashReportData = new CrashReportData();
        final List<Future<?>> futures = new ArrayList<>();
        for (final Collector collector : collectors) {
            futures.add(executorService.submit(() -> {
                //catch absolutely everything possible here so no collector obstructs the others
                try {
                    if(ACRA.DEV_LOGGING)ACRA.log.d(LOG_TAG, "Calling collector " + collector.getClass().getName());
                    collector.collect(context, config, builder, crashReportData);
                    if(ACRA.DEV_LOGGING)ACRA.log.d(LOG_TAG, "Collector " + collector.getClass().getName() + " completed");
                }catch (CollectorException e){
                    ACRA.log.w(LOG_TAG, e);
                }catch (Throwable t) {
                    ACRA.log.e(LOG_TAG, "Error in collector " + collector.getClass().getSimpleName(), t);
                }
            }));
        }
        for (Future<?> future : futures) {
            while (!future.isDone()) {
                try {
                    future.get();
                } catch (InterruptedException ignored) {
                } catch (ExecutionException e) {
                    break;
                }
            }
        }
        return crashReportData;
    }

    public void collectStartUp() {
        for (Collector collector : collectors) {
            if (collector instanceof ApplicationStartupCollector) {
                //catch absolutely everything possible here so no collector obstructs the others
                try {
                    ((ApplicationStartupCollector) collector).collectApplicationStartUp(context, config);
                } catch (Throwable t) {
                    ACRA.log.w(ACRA.LOG_TAG, collector.getClass().getSimpleName() + " failed to collect its startup data", t);
                }
            }
        }
    }
}