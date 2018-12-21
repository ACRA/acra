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

import com.google.auto.service.AutoService;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.util.Installation;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

/**
 * Collects various simple values
 *
 * @author F43nd1r
 * @since 4.9.1
 */
@AutoService(Collector.class)
public final class SimpleValuesCollector extends BaseReportFieldCollector {

    public SimpleValuesCollector() {
        super(ReportField.IS_SILENT, ReportField.REPORT_ID, ReportField.INSTALLATION_ID,
                ReportField.PACKAGE_NAME, ReportField.PHONE_MODEL, ReportField.ANDROID_VERSION,
                ReportField.BRAND, ReportField.PRODUCT, ReportField.FILE_PATH, ReportField.USER_IP);
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws Exception {
        switch (reportField) {
            case IS_SILENT:
                target.put(ReportField.IS_SILENT, reportBuilder.isSendSilently());
                break;
            case REPORT_ID:
                target.put(ReportField.REPORT_ID, UUID.randomUUID().toString());
                break;
            case INSTALLATION_ID:
                target.put(ReportField.INSTALLATION_ID, Installation.id(context));
                break;
            case PACKAGE_NAME:
                target.put(ReportField.PACKAGE_NAME, context.getPackageName());
                break;
            case PHONE_MODEL:
                target.put(ReportField.PHONE_MODEL, Build.MODEL);
                break;
            case ANDROID_VERSION:
                target.put(ReportField.ANDROID_VERSION, Build.VERSION.RELEASE);
                break;
            case BRAND:
                target.put(ReportField.BRAND, Build.BRAND);
                break;
            case PRODUCT:
                target.put(ReportField.PRODUCT, Build.PRODUCT);
                break;
            case FILE_PATH:
                target.put(ReportField.FILE_PATH, getApplicationFilePath(context));
                break;
            case USER_IP:
                target.put(ReportField.USER_IP, getLocalIpAddress());
                break;
            default:
                //will not happen if used correctly
                throw new IllegalArgumentException();
        }
    }

    @Override
    boolean shouldCollect(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportField collect, @NonNull ReportBuilder reportBuilder) {
        return collect == ReportField.IS_SILENT || collect == ReportField.REPORT_ID || super.shouldCollect(context, config, collect, reportBuilder);
    }

    @NonNull
    private String getApplicationFilePath(@NonNull Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    @NonNull
    private static String getLocalIpAddress() throws SocketException {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (final Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            final NetworkInterface intf = en.nextElement();
            for (final Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
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
        return result.toString();
    }
}
