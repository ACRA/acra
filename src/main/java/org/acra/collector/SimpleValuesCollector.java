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
 * Created by Lukas on 12.08.2016.
 */
public class SimpleValuesCollector extends Collector {
    private final Context context;

    public SimpleValuesCollector(Context context) {
        super(ReportField.IS_SILENT, ReportField.REPORT_ID, ReportField.INSTALLATION_ID,
                ReportField.PACKAGE_NAME, ReportField.PHONE_MODEL, ReportField.ANDROID_VERSION,
                ReportField.BRAND,ReportField.PRODUCT,ReportField.FILE_PATH, ReportField.USER_IP);
        this.context = context;
    }

    @Override
    public boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return collect == ReportField.IS_SILENT || collect == ReportField.REPORT_ID || super.shouldCollect(crashReportFields, collect, reportBuilder);
    }

    @NonNull
    @Override
    public String collect(ReportField reportField, ReportBuilder reportBuilder) {
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
