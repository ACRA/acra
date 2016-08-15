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

import android.support.annotation.NonNull;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;

/**
 * collects time information
 *
 * @author F43nd1r
 * @since 4.9.1
 */
final class TimeCollector extends Collector {
    private final Calendar appStartDate;

    TimeCollector(Calendar appStartDate) {
        super(ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE);
        this.appStartDate = appStartDate;
    }

    @Override
    boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return true;
    }

    @NonNull
    @Override
    String collect(ReportField reportField, ReportBuilder reportBuilder) {
        Calendar time;
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
        return getTimeString(time);
    }

    @NonNull
    private static String getTimeString(@NonNull Calendar time) {
        final SimpleDateFormat format = new SimpleDateFormat(ACRAConstants.DATE_TIME_FORMAT_STRING, Locale.ENGLISH);
        return format.format(time.getTimeInMillis());
    }
}
