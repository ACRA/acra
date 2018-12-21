/*
 *  Copyright 2010 Kevin Gaudin
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
import org.json.JSONObject;

/**
 * Collects some data identifying a Thread
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector.class)
public final class ThreadCollector extends BaseReportFieldCollector {
    public ThreadCollector() {
        super(ReportField.THREAD_DETAILS);
    }

    @NonNull
    @Override
    public Order getOrder() {
        return Order.LATE;
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws Exception {
        final Thread t = reportBuilder.getUncaughtExceptionThread();
        if (t != null) {
            final JSONObject result = new JSONObject();
            result.put("id", t.getId());
            result.put("name", t.getName());
            result.put("priority", t.getPriority());
            if (t.getThreadGroup() != null) {
                result.put("groupName", t.getThreadGroup().getName());
            }
            target.put(ReportField.THREAD_DETAILS, result);
        } else {
            target.put(ReportField.THREAD_DETAILS, (String) null);
        }
    }
}
