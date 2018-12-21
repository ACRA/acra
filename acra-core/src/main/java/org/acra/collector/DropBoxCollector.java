/*
 *  Copyright 2010 Kevin Gaudin
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

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.DropBoxManager;
import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.util.PackageManagerWrapper;
import org.acra.util.SystemServices;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects data from the {@link DropBoxManager}. A set of DropBox tags have been identified in the Android source code.
 * We collect data associated to these tags with hope that some of them could help debugging applications.
 * Application specific tags can be provided by the app dev to track his own usage of the DropBoxManager.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector.class)
public final class DropBoxCollector extends BaseReportFieldCollector {

    private static final String[] SYSTEM_TAGS = {"system_app_anr", "system_app_wtf", "system_app_crash",
            "system_server_anr", "system_server_wtf", "system_server_crash", "BATTERY_DISCHARGE_INFO",
            "SYSTEM_RECOVERY_LOG", "SYSTEM_BOOT", "SYSTEM_LAST_KMSG", "APANIC_CONSOLE", "APANIC_THREADS",
            "SYSTEM_RESTART", "SYSTEM_TOMBSTONE", "data_app_strictmode"};

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault()); //iCal format (used for backwards compatibility)

    public DropBoxCollector() {
        super(ReportField.DROPBOX);
    }

    @NonNull
    @Override
    public Order getOrder() {
        return Order.FIRST;
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws Exception{
        final DropBoxManager dropbox = SystemServices.getDropBoxManager(context);

        final Calendar calendar = Calendar.getInstance();
        calendar.roll(Calendar.MINUTE, -config.dropboxCollectionMinutes());
        final long time = calendar.getTimeInMillis();
        dateFormat.format(calendar.getTime());

        final List<String> tags = new ArrayList<>();
        if (config.includeDropBoxSystemTags()) {
            tags.addAll(Arrays.asList(SYSTEM_TAGS));
        }
        final List<String> additionalTags = config.additionalDropBoxTags();
        if (!additionalTags.isEmpty()) {
            tags.addAll(additionalTags);
        }

        if (!tags.isEmpty()) {
            final JSONObject dropboxContent = new JSONObject();
            for (String tag : tags) {
                final StringBuilder builder = new StringBuilder();
                DropBoxManager.Entry entry = dropbox.getNextEntry(tag, time);
                if (entry == null) {
                    builder.append("Nothing.").append('\n');
                    continue;
                }
                while (entry != null) {
                    final long msec = entry.getTimeMillis();
                    calendar.setTimeInMillis(msec);
                    builder.append('@').append(dateFormat.format(calendar.getTime())).append('\n');
                    final String text = entry.getText(500);
                    if (text != null) {
                        builder.append("Text: ").append(text).append('\n');
                    } else {
                        builder.append("Not Text!").append('\n');
                    }
                    entry.close();
                    entry = dropbox.getNextEntry(tag, msec);
                }
                try {
                    dropboxContent.put(tag, builder.toString());
                } catch (JSONException e) {
                    ACRA.log.w(LOG_TAG, "Failed to collect data for tag " + tag, e);
                }
            }
            target.put(ReportField.DROPBOX, dropboxContent);
        }
    }

    @Override
    boolean shouldCollect(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportField collect, @NonNull ReportBuilder reportBuilder) {
        return super.shouldCollect(context, config, collect, reportBuilder) &&
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN || new PackageManagerWrapper(context).hasPermission(Manifest.permission.READ_LOGS))
                && new SharedPreferencesFactory(context, config).create().getBoolean(ACRA.PREF_ENABLE_SYSTEM_LOGS, true);
    }
}
