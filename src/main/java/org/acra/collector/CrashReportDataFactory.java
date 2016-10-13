/*
 *  Copyright 2012 Kevin Gaudin
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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.ACRAConfiguration;
import org.acra.model.Element;
import org.acra.util.PackageManagerWrapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for creating the CrashReportData for an Exception.
 * <p>
 * Also responsible for holding the custom data to send with each report.
 * </p>
 *
 * @author William Ferguson
 * @since 4.3.0
 */
public final class CrashReportDataFactory {

    private final Context context;
    private final ACRAConfiguration config;
    private final SharedPreferences prefs;
    private final Map<String, String> customParameters = new LinkedHashMap<String, String>();
    private final Calendar appStartDate;
    private final Element initialConfiguration;

    public CrashReportDataFactory(@NonNull Context context, @NonNull ACRAConfiguration config,
                                  @NonNull SharedPreferences prefs, @NonNull Calendar appStartDate,
                                  @NonNull Element initialConfiguration) {
        this.context = context;
        this.config = config;
        this.prefs = prefs;
        this.appStartDate = appStartDate;
        this.initialConfiguration = initialConfiguration;
    }

    /**
     * <p>
     * Adds a custom key and value to be reported with the generated
     * CashReportData.
     * </p>
     * <p>
     * The key/value pairs will be stored in the "custom" column, as a text
     * containing one 'key = value' pair on each line.
     * </p>
     *
     * @param key   A key for your custom data.
     * @param value The value associated to your key.
     * @return The previous value for this key if there was one, or null.
     */
    public String putCustomData(@NonNull String key, String value) {
        return customParameters.put(key, value);
    }

    /**
     * Removes a key/value pair from the custom data field.
     *
     * @param key The key of the data to be removed.
     * @return The value for this key before removal.
     */
    public String removeCustomData(@NonNull String key) {
        return customParameters.remove(key);
    }

    /**
     * Removes all key/value pairs from the custom data field.
     */
    public void clearCustomData() {
        customParameters.clear();
    }

    /**
     * Gets the current value for a key in the custom data field.
     *
     * @param key The key of the data to be retrieved.
     * @return The value for this key.
     */
    public String getCustomData(@NonNull String key) {
        return customParameters.get(key);
    }

    /**
     * Collects crash data.
     *
     * @param builder ReportBuilder for whom to crete the crash report.
     * @return CrashReportData identifying the current crash.
     */
    @NonNull
    public CrashReportData createCrashData(@NonNull ReportBuilder builder) {
        final CrashReportData crashReportData = new CrashReportData();
        try {
            final Set<ReportField> crashReportFields = config.getReportFields();
            final List<Collector> collectors = getCollectorsOrdered();

            //this will iterate over all collectors in descending order of priority
            for (Collector collector : collectors) {
                //catch absolutely everything possible here so no collector obstructs the others
                try {
                    for (ReportField reportField : collector.canCollect()) {
                        try {
                            if (collector.shouldCollect(crashReportFields, reportField, builder)) {
                                crashReportData.put(reportField, collector.collect(reportField, builder));
                            }
                        } catch (RuntimeException e) {
                            ACRA.log.e(LOG_TAG, "Error while retrieving " + reportField.name() + " data", e);
                        }
                    }
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error in collector " + collector.getClass().getSimpleName(), e);
                }
            }

        } catch (RuntimeException e) {
            ACRA.log.e(LOG_TAG, "Error while retrieving crash data", e);
        }

        return crashReportData;
    }

    private List<Collector> getCollectorsOrdered() {
        List<Collector> collectors = new ArrayList<Collector>();
        PackageManagerWrapper pm = new PackageManagerWrapper(context);
        collectors.add(new LogCatCollector(config, pm));
        collectors.add(new DropBoxCollector(context, config, pm));
        collectors.add(new StacktraceCollector());
        collectors.add(new TimeCollector(appStartDate));
        collectors.add(new SimpleValuesCollector(context));
        collectors.add(new ConfigurationCollector(context, initialConfiguration));
        collectors.add(new MemoryInfoCollector());
        collectors.add(new ReflectionCollector(context, config));
        collectors.add(new DisplayManagerCollector(context));
        collectors.add(new CustomDataCollector(customParameters));
        collectors.add(new SharedPreferencesCollector(context, config, prefs));
        collectors.add(new DeviceFeaturesCollector(context));
        collectors.add(new SettingsCollector(context, config));
        collectors.add(new PackageManagerCollector(pm));
        collectors.add(new DeviceIdCollector(context, pm, prefs));
        collectors.add(new LogFileCollector(context, config));
        collectors.add(new MediaCodecListCollector());
        collectors.add(new ThreadCollector());
        return collectors;
    }
}