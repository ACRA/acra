/*
 *  Copyright 2011 Kevin Gaudin
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
package org.acra.config;

import android.app.Application;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.NoPropagation;
import org.acra.annotation.PreBuild;
import org.acra.annotation.Transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ACRAConstants.DEFAULT_REPORT_FIELDS;

public abstract class BaseACRAConfigurationBuilder<T extends BaseACRAConfigurationBuilder> {

    private final Map<ReportField, Boolean> reportContentChanges;
    private final List<ConfigurationBuilder> configurationBuilders;
    private List<Configuration> configurations;

    BaseACRAConfigurationBuilder(@NonNull Application app) {
        reportContentChanges = new EnumMap<>(ReportField.class);
        configurationBuilders = new ArrayList<>();
        for (ConfigurationBuilderFactory factory : ServiceLoader.load(ConfigurationBuilderFactory.class)) {
            configurationBuilders.add(factory.create(app));
        }
    }

    @PreBuild
    void preBuild() throws ACRAConfigurationException {
        configurations = new ArrayList<>();
        for (ConfigurationBuilder builder : configurationBuilders) {
            configurations.add(builder.build());
        }
    }

    @Transform(methodName = "reportContent")
    Set<ReportField> transformReportContent(ReportField[] reportFields) {
        final Set<ReportField> reportContent = new LinkedHashSet<>();
        if (reportFields.length != 0) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using custom Report Fields");
            reportContent.addAll(Arrays.asList(reportFields));
        } else {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using default Report Fields");
            reportContent.addAll(Arrays.asList(DEFAULT_REPORT_FIELDS));
        }

        // Add or remove any extra fields.
        for (Map.Entry<ReportField, Boolean> entry : reportContentChanges.entrySet()) {
            if (entry.getValue()) {
                reportContent.add(entry.getKey());
            } else {
                reportContent.remove(entry.getKey());
            }
        }
        return reportContent;
    }

    /**
     * Use this if you want to keep the default configuration of reportContent, but set some fields explicitly.
     *
     * @param field  the field to set
     * @param enable if this field should be reported
     * @return this instance
     */
    @NonNull
    public T setReportField(@NonNull ReportField field, boolean enable) {
        this.reportContentChanges.put(field, enable);
        return (T) this;
    }

    @NonNull
    List<Configuration> pluginConfigurations() {
        return configurations;
    }

    @NoPropagation
    public <T extends ConfigurationBuilder> T getSenderConfigurationBuilder(Class<T> c) {
        for (ConfigurationBuilder builder : configurationBuilders) {
            if (c.isAssignableFrom(builder.getClass())) {
                //noinspection unchecked
                return (T) builder;
            }
        }
        return null;
    }
}
