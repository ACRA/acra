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
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.util.PackageManagerWrapper;

/**
 * Collects {@link PackageInfo} values
 *
 * @author F43nd1r
 * @since 4.9.1
 */
@AutoService(Collector.class)
public final class PackageManagerCollector extends BaseReportFieldCollector {

    public PackageManagerCollector() {
        super(ReportField.APP_VERSION_NAME, ReportField.APP_VERSION_CODE);
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws CollectorException {
        final PackageInfo info = new PackageManagerWrapper(context).getPackageInfo();
        if (info == null) {
            throw new CollectorException("Failed to get package info");
        } else {
            switch (reportField) {
                case APP_VERSION_NAME:
                    target.put(ReportField.APP_VERSION_NAME, info.versionName);
                    break;
                case APP_VERSION_CODE:
                    target.put(ReportField.APP_VERSION_CODE, info.versionCode);
                    break;
            }
        }
    }
}
