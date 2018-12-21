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

import android.content.Context;
import android.support.annotation.NonNull;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.BuilderMethod;
import org.acra.annotation.ConfigurationValue;
import org.acra.annotation.PreBuild;
import org.acra.annotation.Transform;
import org.acra.plugins.PluginLoader;
import org.acra.plugins.ServicePluginLoader;
import org.acra.util.StubCreator;

import java.util.*;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ACRAConstants.DEFAULT_REPORT_FIELDS;


/**
 * Contains builder methods which can't be generated
 *
 * @author F43nd1r
 */
@SuppressWarnings("WeakerAccess")
public final class BaseCoreConfigurationBuilder {

    private final Map<ReportField, Boolean> reportContentChanges;
    private final Context app;
    private List<ConfigurationBuilder> configurationBuilders;
    private List<Configuration> configurations;
    private PluginLoader pluginLoader;

    BaseCoreConfigurationBuilder(@NonNull Context app) {
        this.app = app;
        reportContentChanges = new EnumMap<>(ReportField.class);
        pluginLoader = new ServicePluginLoader();
    }

    private List<ConfigurationBuilder> configurationBuilders() {
        if (configurationBuilders == null) {
            List<ConfigurationBuilderFactory> factories = pluginLoader.load(ConfigurationBuilderFactory.class);
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Found ConfigurationBuilderFactories : " + factories);
            configurationBuilders = new ArrayList<>(factories.size());
            for (ConfigurationBuilderFactory factory : factories) {
                configurationBuilders.add(factory.create(app));
            }
        }
        return configurationBuilders;
    }

    /**
     * Set a custom plugin loader. Note: Call this before any call to {@link #getPluginConfigurationBuilder(Class)}
     *
     * @param pluginLoader the custom implementation
     */
    @BuilderMethod
    public void setPluginLoader(@NonNull PluginLoader pluginLoader) {
        this.pluginLoader = pluginLoader;
    }

    @NonNull
    @ConfigurationValue
    PluginLoader pluginLoader() {
        return pluginLoader;
    }

    @PreBuild
    void preBuild() throws ACRAConfigurationException {
        configurations = new ArrayList<>();
        final List<ConfigurationBuilder> builders = configurationBuilders();
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Found ConfigurationBuilders : " + builders);
        for (ConfigurationBuilder builder : builders) {
            configurations.add(builder.build());
        }
    }

    @NonNull
    @Transform(methodName = "reportContent")
    Set<ReportField> transformReportContent(@NonNull ReportField[] reportFields) {
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
     */
    @BuilderMethod
    public void setReportField(@NonNull ReportField field, boolean enable) {
        this.reportContentChanges.put(field, enable);
    }

    @ConfigurationValue
    @NonNull
    List<Configuration> pluginConfigurations() {
        return configurations;
    }

    @NonNull
    @BuilderMethod
    public <R extends ConfigurationBuilder> R getPluginConfigurationBuilder(@NonNull Class<R> c) {
        for (ConfigurationBuilder builder : configurationBuilders()) {
            if (c.isAssignableFrom(builder.getClass())) {
                //noinspection unchecked
                return (R) builder;
            }
        }
        if (c.isInterface()) {
            ACRA.log.w(LOG_TAG, "Couldn't find ConfigurationBuilder " + c.getSimpleName() + ". ALL CALLS TO IT WILL BE IGNORED!");
            return StubCreator.createStub(c, (proxy, method, args) -> proxy);
        }
        throw new IllegalArgumentException("Class " + c.getName() + " is not a registered ConfigurationBuilder");
    }
}
