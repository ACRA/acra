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
import android.support.annotation.Size;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;

import java.util.Set;

/**
 * Created on 11.08.2016.
 *
 * @author F43nd1r
 */
abstract class Collector implements Comparable<Collector> {
    private final ReportField[] reportFields;

    /**
     * create a new Collector that is able to collect these reportFields
     * (Note: @Size is currently not working for varargs, it is still here as a hint to developers)
     *
     * @param reportFields the supported reportFields
     */
    Collector(@Size(min = 1) @NonNull ReportField... reportFields) {
        this.reportFields = reportFields;
    }

    /**
     * @return all fields this collector can collect
     */
    @NonNull
    final ReportField[] canCollect() {
        return reportFields;
    }

    /**
     * Should be:
     * > 0 if this class should be called as early as possible
     * = 0 if the relative position isn't important
     * < 0 if this class performs dodgy operations
     *
     * @return priority of this collector
     */
    int getPriority() {
        return 0;
    }

    @Override
    public final int compareTo(@NonNull Collector another) {
        return getPriority() - another.getPriority();
    }

    /**
     * this should check if the set contains the field, but may add additional checks like permissions etc.
     *
     * @param crashReportFields configured fields
     * @param collect           the filed to collect
     * @param reportBuilder     the current reportBuilder
     * @return if this field should be collected now
     */
    boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
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
    abstract String collect(ReportField reportField, ReportBuilder reportBuilder);
}
