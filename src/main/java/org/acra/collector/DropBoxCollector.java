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

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.ACRAConfiguration;
import org.acra.model.ComplexElement;
import org.acra.model.Element;
import org.acra.util.PackageManagerWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects data from the {@link DropBoxManager}. A
 * set of DropBox tags have been identified in the Android source code. , we
 * collect data associated to these tags with hope that some of them could help
 * debugging applications. Application specific tags can be provided by the app
 * dev to track his own usage of the DropBoxManager.
 *
 * @author Kevin Gaudin & F43nd1r
 */
final class DropBoxCollector extends Collector {

    private final Context context;
    private final ACRAConfiguration config;
    private final PackageManagerWrapper pm;

    DropBoxCollector(Context context, ACRAConfiguration config, PackageManagerWrapper pm){
        super(ReportField.DROPBOX);
        this.context = context;
        this.config = config;
        this.pm = pm;
    }

    private static final String[] SYSTEM_TAGS = {"system_app_anr", "system_app_wtf", "system_app_crash",
            "system_server_anr", "system_server_wtf", "system_server_crash", "BATTERY_DISCHARGE_INFO",
            "SYSTEM_RECOVERY_LOG", "SYSTEM_BOOT", "SYSTEM_LAST_KMSG", "APANIC_CONSOLE", "APANIC_THREADS",
            "SYSTEM_RESTART", "SYSTEM_TOMBSTONE", "data_app_strictmode"};

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault()); //iCal format (used for backwards compatibility)

    /**
     * Read latest messages contained in the DropBox for system related tags and
     * optional developer-set tags.
     *
     * @return An Element listing messages retrieved.
     */
    @NonNull
    @Override
    Element collect(ReportField reportField, ReportBuilder reportBuilder) {
        try {
            final DropBoxManager dropbox = (DropBoxManager) context.getSystemService(Context.DROPBOX_SERVICE);

            final Calendar calendar = Calendar.getInstance();
            calendar.roll(Calendar.MINUTE, -config.dropboxCollectionMinutes());
            final long time = calendar.getTimeInMillis();
            dateFormat.format(calendar.getTime());

            final List<String> tags = new ArrayList<String>();
            if (config.includeDropBoxSystemTags()) {
                tags.addAll(Arrays.asList(SYSTEM_TAGS));
            }
            final Set<String> additionalTags = config.additionalDropBoxTags();
            if (!additionalTags.isEmpty()) {
                tags.addAll(additionalTags);
            }

            if (tags.isEmpty()) {
                return ACRAConstants.NOT_AVAILABLE;
            }

            final ComplexElement dropboxContent = new ComplexElement();
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
                dropboxContent.put(tag, builder.toString());
            }
            return dropboxContent;

        } catch (Exception e) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "DropBoxManager not available.");
        }

        return ACRAConstants.NOT_AVAILABLE;
    }

    @Override
    boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return super.shouldCollect(crashReportFields, collect, reportBuilder) && (pm.hasPermission(Manifest.permission.READ_LOGS) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
    }
}
