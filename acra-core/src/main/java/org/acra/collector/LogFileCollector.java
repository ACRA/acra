/*
 *  Copyright 2012 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.acra.collector;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.util.StreamReader;

import java.io.IOException;

/**
 * Collects the N last lines of a text stream. Use this collector if your application handles its own logging system.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector.class)
public final class LogFileCollector extends BaseReportFieldCollector {

    public LogFileCollector() {
        super(ReportField.APPLICATION_LOG);
    }

    @NonNull
    @Override
    public Order getOrder() {
        return Order.LATE;
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws IOException {
        target.put(ReportField.APPLICATION_LOG, new StreamReader(config.applicationLogFileDir().getFile(context, config.applicationLogFile()))
                .setLimit(config.applicationLogFileLines()).read());
    }
}
