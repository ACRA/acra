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
import org.acra.model.Element;

import java.util.Set;

/**
 * Represents a collector.
 * Maintains information on which fields can be collected by this collector.
 * Validates constraints in which a field should (not) be collected.
 *
 * @author F43nd1r
 * @since 4.9.1
 */
abstract class Collector {
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
     * @return Element of what was collected
     */
    @NonNull
    abstract Element collect(ReportField reportField, ReportBuilder reportBuilder);
}
