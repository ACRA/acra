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

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.util.ReportUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

/**
 * Created on 12.08.2016.
 *
 * @author F43nd1r
 */
class TimeCollector extends Collector {
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
                //will never happen
                throw new IllegalArgumentException();
        }
        return ReportUtils.getTimeString(time);
    }
}
