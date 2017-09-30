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

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.file.Directory;
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
final class LogFileCollector extends AbstractReportFieldCollector {

    LogFileCollector() {
        super(ReportField.APPLICATION_LOG);
    }

    @NonNull
    @Override
    public Order getOrder() {
        return Order.LATE;
    }

    @Override
    void collect(ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws IOException {
        target.put(ReportField.APPLICATION_LOG, IOUtils.streamToString(
                getStream(context, config.applicationLogFileDir(), config.applicationLogFile()),
                config.applicationLogFileLines()));
    }

    /**
     * get the application log file location and open it
     *
     * @param directory the base directory for the file path
     * @param fileName  the name of the file
     * @return a stream to the file or an empty stream if the file was not found
     */
    @NonNull
    private InputStream getStream(@NonNull Context context, @NonNull Directory directory, @NonNull String fileName) {
        final File file = directory.getFile(context, fileName);
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
