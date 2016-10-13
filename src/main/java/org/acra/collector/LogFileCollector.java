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
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.ACRAConfiguration;
import org.acra.file.Directory;
import org.acra.model.Element;
import org.acra.model.StringElement;
import org.acra.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects the N last lines of a text stream. Use this collector if your
 * application handles its own logging system.
 *
 * @author Kevin Gaudin & F43nd1r
 */
final class LogFileCollector extends Collector {
    private final Context context;
    private final ACRAConfiguration config;

    LogFileCollector(Context context, ACRAConfiguration config) {
        super(ReportField.APPLICATION_LOG);
        this.context = context;
        this.config = config;
    }

    /**
     * Reads the last lines of a custom log file. The file name is assumed as
     * located in the {@link Application#getFilesDir()} directory if it does not
     * contain any path separator.
     *
     * @return An Element containing all of the requested lines.
     */
    @NonNull
    @Override
    Element collect(ReportField reportField, ReportBuilder reportBuilder) {
        try {
            return new StringElement(IOUtils.streamToString(
                    getStream(config.applicationLogFileDir(), config.applicationLogFile()),
                    config.applicationLogFileLines()));
        } catch (IOException e) {
            return ACRAConstants.NOT_AVAILABLE;
        }
    }

    /**
     * get the application log file location and open it
     *
     * @param directory the base directory for the file path
     * @param fileName the name of the file
     * @return a stream to the file or an empty stream if the file was not found
     */
    @NonNull
    private InputStream getStream(@NonNull Directory directory, @NonNull String fileName) {
        if (directory == Directory.FILES_LEGACY) {
            directory = fileName.startsWith("/") ? Directory.ROOT : Directory.FILES;
        }
        final File dir;
        switch (directory) {
            case FILES:
                dir = context.getFilesDir();
                break;
            case EXTERNAL_FILES:
                dir = context.getExternalFilesDir(null);
                break;
            case CACHE:
                dir = context.getCacheDir();
                break;
            case EXTERNAL_CACHE:
                dir = context.getExternalCacheDir();
                break;
            case NO_BACKUP_FILES:
                dir = ContextCompat.getNoBackupFilesDir(context);
                break;
            case EXTERNAL_STORAGE:
                dir = Environment.getExternalStorageDirectory();
                break;
            case ROOT:
            default:
                dir = new File("/");
                break;
        }
        final File file = new File(dir, fileName);
        if (!file.exists()) {
            if (ACRA.DEV_LOGGING)
                ACRA.log.d(LOG_TAG, "Log file '" + file.getPath() + "' does not exist");
        } else if (file.isDirectory()) {
            ACRA.log.e(LOG_TAG, "Log file '" + file.getPath() + "' is a directory");
        } else if (!file.canRead()) {
            ACRA.log.e(LOG_TAG, "Log file '" + file.getPath() + "' can't be read");
        } else {
            try {
                return new FileInputStream(file);
            } catch (IOException e) {
                ACRA.log.e(LOG_TAG, "Could not open stream for log file '" + file.getPath() + "'");
            }
        }
        return new ByteArrayInputStream(new byte[0]);
    }
}
