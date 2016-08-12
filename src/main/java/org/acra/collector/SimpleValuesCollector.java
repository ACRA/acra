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
import android.os.Build;
import android.support.annotation.NonNull;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.util.Installation;
import org.acra.util.ReportUtils;

import java.util.Set;
import java.util.UUID;

/**
 * Created on 12.08.2016.
 *
 * @author F43nd1r
 */
class SimpleValuesCollector extends Collector {
    private final Context context;

    SimpleValuesCollector(Context context) {
        super(ReportField.IS_SILENT, ReportField.REPORT_ID, ReportField.INSTALLATION_ID,
                ReportField.PACKAGE_NAME, ReportField.PHONE_MODEL, ReportField.ANDROID_VERSION,
                ReportField.BRAND,ReportField.PRODUCT,ReportField.FILE_PATH, ReportField.USER_IP);
        this.context = context;
    }

    @Override
    boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return collect == ReportField.IS_SILENT || collect == ReportField.REPORT_ID || super.shouldCollect(crashReportFields, collect, reportBuilder);
    }

    @NonNull
    @Override
    String collect(ReportField reportField, ReportBuilder reportBuilder) {
        switch (reportField) {
            case IS_SILENT:
                return String.valueOf(reportBuilder.isSendSilently());
            case REPORT_ID:
                return UUID.randomUUID().toString();
            case INSTALLATION_ID:
                return Installation.id(context);
            case PACKAGE_NAME:
                return context.getPackageName();
            case PHONE_MODEL:
                return Build.MODEL;
            case ANDROID_VERSION:
                return Build.VERSION.RELEASE;
            case BRAND:
                return Build.BRAND;
            case PRODUCT:
                return Build.PRODUCT;
            case FILE_PATH:
                return ReportUtils.getApplicationFilePath(context);
            case USER_IP:
                return ReportUtils.getLocalIpAddress();
            default:
                //will never happen
                throw new IllegalArgumentException();
        }
    }
}
