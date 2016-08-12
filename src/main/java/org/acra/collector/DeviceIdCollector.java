package org.acra.collector;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.util.PackageManagerWrapper;
import org.acra.util.ReportUtils;

import java.util.Set;

/**
 * Created by Lukas on 12.08.2016.
 */
public class DeviceIdCollector extends Collector {
    private final Context context;
    private final PackageManagerWrapper pm;
    private final SharedPreferences prefs;

    public DeviceIdCollector(Context context, PackageManagerWrapper pm, SharedPreferences prefs) {
        super(ReportField.DEVICE_ID);
        this.context = context;
        this.pm = pm;
        this.prefs = prefs;
    }

    @Override
    public boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return super.shouldCollect(crashReportFields, collect, reportBuilder) && prefs.getBoolean(ACRA.PREF_ENABLE_DEVICE_ID, true)
                && pm.hasPermission(Manifest.permission.READ_PHONE_STATE);
    }

    @NonNull
    @Override
    public String collect(ReportField reportField, ReportBuilder reportBuilder) {
        return ReportUtils.getDeviceId(context);
    }
}
