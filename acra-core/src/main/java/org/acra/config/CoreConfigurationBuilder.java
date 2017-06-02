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
import org.acra.annotation.ConfigurationBuilder;
import org.acra.annotation.NoPropagation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ACRAConstants.*;

/**
 * Builder responsible for programmatic construction of an immutable {@link ACRAConfiguration}.
 *
 * @since 4.8.1
 */
@SuppressWarnings("unused")
@ConfigurationBuilder(configurationName = "ACRAConfiguration", createFactory = false)
public final class CoreConfigurationBuilder extends BaseCoreConfigurationBuilder<CoreConfigurationBuilder> {

    private final Map<ReportField, Boolean> reportContentChanges;
    private final List<PluginConfigurationBuilder> pluginConfigurationBuilders;
    private List<PluginConfiguration> pluginConfigurations;

    /**
     * Constructs a ConfigurationBuilder that is prepopulated with any
     * '@ReportCrashes' annotation declared on the Application class.
     *
     * @param app Current Application, from which any annotated config will be gleaned.
     */
    public CoreConfigurationBuilder(@NonNull Application app) {
        super(app);
        reportContentChanges = new EnumMap<>(ReportField.class);
        pluginConfigurationBuilders = new ArrayList<>();
        for (PluginConfigurationBuilderFactory factory : ServiceLoader.load(PluginConfigurationBuilderFactory.class)) {
            pluginConfigurationBuilders.add(factory.create(app));
        }
    }

    /**
     * Builds the {@link ACRAConfiguration} which will be used to configure ACRA.
     * <p>
     * You can pass this {@link CoreConfigurationBuilder} to {@link ACRA#init(Application, CoreConfigurationBuilder)} and
     * {@link ACRA#init(Application, CoreConfigurationBuilder)} will handle any Exception.
     * </p>
     *
     * @return new ACRAConfiguration containing all the properties configured on this builder.
     * @throws ACRAConfigurationException if the required values for the configured notification mode have not been provided.
     */
    @NoPropagation
    @NonNull
    public ACRAConfiguration build() throws ACRAConfigurationException {
        if (reportSenderFactoryClasses().length == 0) {
            throw new ACRAConfigurationException("Report sender factories: using no report senders will make ACRA useless. Configure at least one ReportSenderFactory.");
        }
        ConfigUtils.checkValidity((Class[]) reportSenderFactoryClasses());
        ConfigUtils.checkValidity(reportPrimerClass(), retryPolicyClass());
        pluginConfigurations = new ArrayList<>();
        for (PluginConfigurationBuilder builder : pluginConfigurationBuilders) {
            pluginConfigurations.add(builder.build());
        }
        return new ACRAConfiguration(this);
    }

    /**
     * Use this if you want to keep the default configuration of reportContent, but set some fields explicitly.
     *
     * @param field  the field to set
     * @param enable if this field should be reported
     * @return this instance
     */
    @NonNull
    public CoreConfigurationBuilder setReportField(@NonNull ReportField field, boolean enable) {
        this.reportContentChanges.put(field, enable);
        return this;
    }

    @NoPropagation
    @NonNull
    @Override
    ReportField[] customReportContent() {
        return super.customReportContent();
    }

    @NonNull
    Set<ReportField> reportContent() {
        final Set<ReportField> reportContent = new LinkedHashSet<ReportField>();
        if (customReportContent().length != 0) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using custom Report Fields");
            reportContent.addAll(Arrays.asList(customReportContent()));
        } else {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using default Mail Report Fields");
            reportContent.addAll(Arrays.asList(DEFAULT_MAIL_REPORT_FIELDS));
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

    @NonNull
    List<PluginConfiguration> pluginConfigurations() {
        return pluginConfigurations;
    }

    @NoPropagation
    public <T extends PluginConfigurationBuilder> T getSenderConfigurationBuilder(Class<T> c) {
        for (PluginConfigurationBuilder builder : pluginConfigurationBuilders) {
            if (c.isAssignableFrom(builder.getClass())) {
                //noinspection unchecked
                return (T) builder;
            }
        }
        return null;
    }
}
