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

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.model.Element;
import org.acra.model.SimpleElement;
import org.acra.util.Installation;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects various simple values
 *
 * @author F43nd1r
 * @since 4.9.1
 */
final class SimpleValuesCollector extends Collector {
    private final Context context;

    SimpleValuesCollector(Context context) {
        super(ReportField.IS_SILENT, ReportField.REPORT_ID, ReportField.INSTALLATION_ID,
                ReportField.PACKAGE_NAME, ReportField.PHONE_MODEL, ReportField.ANDROID_VERSION,
                ReportField.BRAND, ReportField.PRODUCT, ReportField.FILE_PATH, ReportField.USER_IP);
        this.context = context;
    }

    @Override
    boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return collect == ReportField.IS_SILENT || collect == ReportField.REPORT_ID || super.shouldCollect(crashReportFields, collect, reportBuilder);
    }

    @NonNull
    @Override
    Element collect(ReportField reportField, ReportBuilder reportBuilder) {
        String value;
        switch (reportField) {
            case IS_SILENT:
                value = String.valueOf(reportBuilder.isSendSilently());
                break;
            case REPORT_ID:
                value = UUID.randomUUID().toString();
                break;
            case INSTALLATION_ID:
                value = Installation.id(context);
                break;
            case PACKAGE_NAME:
                value = context.getPackageName();
                break;
            case PHONE_MODEL:
                value = Build.MODEL;
                break;
            case ANDROID_VERSION:
                value = Build.VERSION.RELEASE;
                break;
            case BRAND:
                value = Build.BRAND;
                break;
            case PRODUCT:
                value = Build.PRODUCT;
                break;
            case FILE_PATH:
                value = getApplicationFilePath();
                break;
            case USER_IP:
                value = getLocalIpAddress();
                break;
            default:
                //will not happen if used correctly
                throw new IllegalArgumentException();
        }
        return new SimpleElement(value);
    }

    @NonNull
    private String getApplicationFilePath() {
        final File filesDir = context.getFilesDir();
        if (filesDir != null) {
            return filesDir.getAbsolutePath();
        }

        ACRA.log.w(LOG_TAG, "Couldn't retrieve ApplicationFilePath for : " + context.getPackageName());
        return "Couldn't retrieve ApplicationFilePath";
    }

    @NonNull
    private static String getLocalIpAddress() {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                final NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    final InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (!first) {
                            result.append('\n');
                        }
                        result.append(inetAddress.getHostAddress());
                        first = false;
                    }
                }
            }
        } catch (SocketException ex) {
            ACRA.log.w(LOG_TAG, ex.toString());
        }
        return result.toString();
    }
}
