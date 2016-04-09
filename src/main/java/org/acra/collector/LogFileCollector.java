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

package org.acra.collector;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.util.IOUtils;

import java.io.*;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects the N last lines of a text stream. Use this collector if your
 * application handles its own logging system.
 * 
 * @author Kevin Gaudin
 * 
 */
class LogFileCollector {

    /**
     * Reads the last lines of a custom log file. The file name is assumed as
     * located in the {@link Application#getFilesDir()} directory if it does not
     * contain any path separator.
     * 
     * @param context       Application context.
     * @param fileName      Log file to read. It can be an absolute path, or a relative path from the application
     *                      files folder, or a file within the application files folder.
     * @param numberOfLines Number of lines to retrieve.
     * @return A single String containing all of the requested lines.
     * @throws IOException
     */
    @NonNull
    public String collectLogFile(@NonNull Context context, @NonNull String fileName, int numberOfLines) throws IOException {
        return IOUtils.streamToString(getStream(context, fileName), numberOfLines);
    }

    @NonNull
    private static InputStream getStream(@NonNull Context context, @NonNull String fileName) {
        try {
            final FileInputStream inputStream;
            if (fileName.startsWith("/")) {
                // Absolute path
                inputStream = new FileInputStream(fileName);
            } else if (fileName.contains("/")) {
                // Relative path from the application files folder (ie a sub folder)
                inputStream = new FileInputStream(new File(context.getFilesDir(), fileName));
            } else {
                // A file directly contained within the application files folder.
                inputStream = context.openFileInput(fileName);
            }
            return inputStream;
        } catch (FileNotFoundException e) {
            ACRA.log.e(LOG_TAG, "Cannot find application log file : '" + fileName + "'");
            return new ByteArrayInputStream(new byte[0]);
        }
    }
}
