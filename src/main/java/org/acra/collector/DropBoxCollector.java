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

import android.content.Context;
import android.os.DropBoxManager;
import android.support.annotation.NonNull;
import android.text.format.Time;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects data from the DropBoxManager introduced with Android API Level 8. A
 * set of DropBox tags have been identified in the Android source code. , we
 * collect data associated to these tags with hope that some of them could help
 * debugging applications. Application specific tags can be provided by the app
 * dev to track his own usage of the DropBoxManager.
 *
 * @author Kevin Gaudin
 */
final class DropBoxCollector {

    private static final String[] SYSTEM_TAGS = {"system_app_anr", "system_app_wtf", "system_app_crash",
            "system_server_anr", "system_server_wtf", "system_server_crash", "BATTERY_DISCHARGE_INFO",
            "SYSTEM_RECOVERY_LOG", "SYSTEM_BOOT", "SYSTEM_LAST_KMSG", "APANIC_CONSOLE", "APANIC_THREADS",
            "SYSTEM_RESTART", "SYSTEM_TOMBSTONE", "data_app_strictmode"};

    private static final String NO_RESULT = "N/A";

    /**
     * Read latest messages contained in the DropBox for system related tags and
     * optional developer-set tags.
     *
     * @param context The application context.
     * @param config  AcraConfig describe what to collect.
     * @return A readable formatted String listing messages retrieved.
     */
    @NonNull
    public String read(@NonNull Context context, @NonNull ACRAConfiguration config) {
        try {
            final DropBoxManager dropbox = (DropBoxManager) context.getSystemService(Context.DROPBOX_SERVICE);

            //TODO: replace Time with Calendar
            final Time timer = new Time();
            timer.setToNow();
            timer.minute -= config.dropboxCollectionMinutes();
            timer.normalize(false);
            final long time = timer.toMillis(false);

            final List<String> tags = new ArrayList<String>();
            if (config.includeDropBoxSystemTags()) {
                tags.addAll(Arrays.asList(SYSTEM_TAGS));
            }
            final String[] additionalTags = config.additionalDropBoxTags();
            if (additionalTags.length > 0) {
                tags.addAll(Arrays.asList(additionalTags));
            }

            if (tags.isEmpty()) {
                return "No tag configured for collection.";
            }

            final StringBuilder dropboxContent = new StringBuilder();
            for (String tag : tags) {
                dropboxContent.append("Tag: ").append(tag).append('\n');
                DropBoxManager.Entry entry = dropbox.getNextEntry(tag, time);
                if (entry == null) {
                    dropboxContent.append("Nothing.").append('\n');
                    continue;
                }
                while (entry != null) {
                    final long msec = entry.getTimeMillis();
                    timer.set(msec);
                    dropboxContent.append("@").append(timer.format2445()).append('\n');
                    final String text = entry.getText(500);
                    if (text != null) {
                        dropboxContent.append("Text: ").append(text).append('\n');
                    } else {
                        dropboxContent.append("Not Text!").append('\n');
                    }
                    entry.close();
                    entry = dropbox.getNextEntry(tag, msec);
                }
            }
            return dropboxContent.toString();

        } catch (Exception e) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "DropBoxManager not available.");
        }

        return NO_RESULT;
    }
}
