/*
 *  Copyright 2012 Kevin Gaudin
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
package org.acra;

import static org.acra.ACRA.LOG_TAG;

import java.io.File;
import java.io.FilenameFilter;

import android.content.Context;
import android.util.Log;

/**
 * Responsible for retrieving the location of Crash Report files.
 * <p/>
 * @author William Ferguson
 * @since 4.3.0
 */
final class CrashReportFinder {

    private final Context context;

    public CrashReportFinder(Context context) {
        this.context = context;
    }

    /**
     * Returns an array containing the names of pending crash report files.
     *
     * @return an array containing the names of pending crash report files.
     */
    public String[] getCrashReportFiles() {
        if (context == null) {
            Log.e(LOG_TAG, "Trying to get ACRA reports but ACRA is not initialized.");
            return new String[0];
        }

        final File dir = context.getFilesDir();
        if (dir == null) {
            Log.w(LOG_TAG, "Application files directory does not exist! The application may not be installed correctly. Please try reinstalling.");
            return new String[0];
        }

        Log.d(LOG_TAG, "Looking for error files in " + dir.getAbsolutePath());

        // Filter for ".stacktrace" files
        final FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(ACRAConstants.REPORTFILE_EXTENSION);
            }
        };
        final String[] result = dir.list(filter);
        return (result == null) ? new String[0] : result;
    }
}
