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

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;

/**
 * Base implementation of a collector.
 * Maintains information on which fields can be collected by this collector.
 * Validates constraints in which a field should (not) be collected.
 *
 * @author F43nd1r
 * @since 4.9.1
 */
abstract class BaseReportFieldCollector implements Collector {
    private final ReportField[] reportFields;

    /**
     * create a new Collector that is able to collect these reportFields
     *
     * @param firstField   the first supported field (split away to ensure each collector supports at least one field)
     * @param reportFields the supported reportFields
     */
    BaseReportFieldCollector(@NonNull ReportField firstField, @NonNull ReportField... reportFields) {
        this.reportFields = new ReportField[reportFields.length + 1];
        this.reportFields[0] = firstField;
        if (reportFields.length > 0) {
            System.arraycopy(reportFields, 0, this.reportFields, 1, reportFields.length);
        }
    }

    /**
     * this should check if the config contains the field, but may add additional checks like permissions etc.
     *
     * @param context       a context
     * @param config        current configuration
     * @param collect       the field to collect
     * @param reportBuilder the current reportBuilder
     * @return if this field should be collected now
     */
    boolean shouldCollect(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportField collect, @NonNull ReportBuilder reportBuilder) {
        return config.reportContent().contains(collect);
    }

    /**
     * Calls {@link #shouldCollect(Context, CoreConfiguration, ReportField, ReportBuilder)} for each ReportField
     * and then {@link #collect(ReportField, Context, CoreConfiguration, ReportBuilder, CrashReportData)} if it returned true
     */
    @Override
    public final void collect(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws CollectorException {
        for (ReportField field : reportFields) {
            try {
                if (shouldCollect(context, config, field, reportBuilder)) {
                    collect(field, context, config, reportBuilder, target);
                }
            } catch (Throwable t) {
                target.put(field, (String) null);
                throw new CollectorException("Error while retrieving " + field.name() + " data:" + t.getMessage(), t);
            }
        }
    }

    /**
     * Collect a ReportField
     *
     * @param reportField the reportField to collect
     * @param context a context
     * @param config current Configuration
     * @param reportBuilder current ReportBuilder
     * @param target put results here
     * @throws Exception if collection failed
     */
    abstract void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws Exception;
}
