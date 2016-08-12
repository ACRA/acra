package org.acra.collector;

import android.support.annotation.NonNull;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.util.ReportUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

/**
 * Created by Lukas on 12.08.2016.
 */
public class TimeCollector extends Collector {
    private final Calendar appStartDate;

    public TimeCollector(Calendar appStartDate) {
        super(ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE);
        this.appStartDate = appStartDate;
    }

    @Override
    public boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return true;
    }

    @NonNull
    @Override
    public String collect(ReportField reportField, ReportBuilder reportBuilder) {
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
