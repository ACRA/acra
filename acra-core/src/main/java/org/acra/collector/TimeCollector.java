/*
 *  Copyright 2016
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

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * collects time information
 *
 * @author F43nd1r
 * @since 4.9.1
 */
@AutoService(Collector.class)
public final class TimeCollector extends BaseReportFieldCollector implements ApplicationStartupCollector {
    private final SimpleDateFormat dateFormat;
    private Calendar appStartDate;

    public TimeCollector() {
        super(ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE);
        dateFormat = new SimpleDateFormat(ACRAConstants.DATE_TIME_FORMAT_STRING, Locale.ENGLISH);
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) {
        final Calendar time;
        switch (reportField) {
            case USER_APP_START_DATE:
                time = appStartDate;
                break;
            case USER_CRASH_DATE:
                time = new GregorianCalendar();
                break;
            default:
                //will not happen if used correctly
                throw new IllegalArgumentException();
        }
        target.put(reportField, getTimeString(time));
    }

    @Override
    public void collectApplicationStartUp(@NonNull Context context, @NonNull CoreConfiguration config) {
        if (config.reportContent().contains(ReportField.USER_APP_START_DATE)) {
            appStartDate = new GregorianCalendar();
        }
    }

    @Override
    boolean shouldCollect(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportField collect, @NonNull ReportBuilder reportBuilder) {
        return collect == ReportField.USER_CRASH_DATE || super.shouldCollect(context, config, collect, reportBuilder);
    }

    @NonNull
    private String getTimeString(@NonNull Calendar time) {
        return dateFormat.format(time.getTimeInMillis());
    }
}
