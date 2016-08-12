package org.acra.collector;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;

import java.util.Set;

/**
 * Created by Lukas on 11.08.2016.
 */
public abstract class Collector implements Comparable<Collector> {
    private final ReportField[] reportFields;

    protected Collector(@Size(min = 1) ReportField... reportFields) {
        this.reportFields = reportFields;
    }

    /**
     * @return all fields this collector can collect
     */
    @NonNull
    public final ReportField[] canCollect() {
        return reportFields;
    }

    /**
     * Should be:
     * > 0 if this should be called as early as possible
     * = 0 if the relative position isn't important
     * < 0 if this is dodgy
     *
     * @return priority of this collector
     */
    public int getPriority() {
        return 0;
    }

    @Override
    public final int compareTo(@NonNull Collector another) {
        return another.getPriority() - getPriority();
    }

    /**
     * this should check if the set contains the field, but may add additional checks like permissions etc.
     *
     * @param crashReportFields configured fields
     * @param collect           the filed to collect
     * @param reportBuilder     the current reportBuilder
     * @return if this field should be collected now
     */
    public boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return crashReportFields.contains(collect);
    }

    /**
     * will only be called if shouldCollect returned true for this ReportField
     *
     * @param reportField   the ReportField to collect
     * @param reportBuilder the current reportBuilder
     * @return String of what was collected
     */
    @NonNull
    public abstract String collect(ReportField reportField, ReportBuilder reportBuilder);
}
