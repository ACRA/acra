package org.acra.collector;

import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.util.PackageManagerWrapper;

import java.util.Set;

/**
 * Created by Lukas on 12.08.2016.
 */
public class PackageManagerCollector extends Collector {
    private final PackageManagerWrapper pm;

    public PackageManagerCollector(PackageManagerWrapper pm) {
        super(ReportField.APP_VERSION_NAME, ReportField.APP_VERSION_CODE);
        this.pm = pm;
    }

    @NonNull
    @Override
    public String collect(ReportField reportField, ReportBuilder reportBuilder) {
        PackageInfo info = pm.getPackageInfo();
        if (info != null) {
            switch (reportField) {
                case APP_VERSION_NAME:
                    return info.versionName;
                case APP_VERSION_CODE:
                    return Integer.toString(info.versionCode);
            }
        }
        return "Package info unavailable";
    }
}
