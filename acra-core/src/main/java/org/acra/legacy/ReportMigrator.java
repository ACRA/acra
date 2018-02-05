/*
 * Copyright (c) 2017 the ACRA team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.legacy;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;

import java.io.File;
import java.io.FilenameFilter;

import static org.acra.ACRA.LOG_TAG;

/**
 * Migrates reports from the pre 4.8.0 location to the 4.8.0+ locations.
 */
final class ReportMigrator {

    private final Context context;
    private final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();
    private final ReportLocator reportLocator;

    ReportMigrator(@NonNull Context context) {
        this.context = context;
        this.reportLocator = new ReportLocator(context);
    }

    void migrate() {
        ACRA.log.i(LOG_TAG, "Migrating unsent ACRA reports to new file locations");

        final File[] reportFiles = getCrashReportFiles();

        for (final File file : reportFiles) {
            // Move it to unapproved or approved folders.
            final String fileName = file.getName();
            if (fileNameParser.isApproved(fileName)) {
                if (file.renameTo(new File(reportLocator.getApprovedFolder(), fileName))) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Cold not migrate unsent ACRA crash report : " + fileName);
                }
            } else {
                if (file.renameTo(new File(reportLocator.getUnapprovedFolder(), fileName))) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Cold not migrate unsent ACRA crash report : " + fileName);
                }
            }
        }
        ACRA.log.i(LOG_TAG, "Migrated " + reportFiles.length + " unsent reports");
    }

    /**
     * Returns an array containing the names of pending crash report files.
     *
     * @return an array containing the names of pending crash report files.
     */
    @NonNull
    private File[] getCrashReportFiles() {
        final File dir = context.getFilesDir();
        if (dir == null) {
            ACRA.log.w(LOG_TAG, "Application files directory does not exist! The application may not be installed correctly. Please try reinstalling.");
            return new File[0];
        }

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Looking for error files in " + dir.getAbsolutePath());

        // Filter for ".stacktrace" files
        final FilenameFilter filter = (dir1, name) -> name.endsWith(ACRAConstants.REPORTFILE_EXTENSION);
        final File[] result = dir.listFiles(filter);
        return (result == null) ? new File[0] : result;
    }

}
