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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.builder.ReportBuilder;
import org.acra.config.ACRAConfiguration;
import org.acra.util.IOUtils;
import org.acra.util.PackageManagerWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.acra.ACRA.LOG_TAG;


/**
 * Executes logcat commands and collects it's output.
 *
 * @author Kevin Gaudin
 */
final class LogCatCollector extends Collector {
    /**
     * Default number of latest lines kept from the logcat output.
     */
    private static final int DEFAULT_TAIL_COUNT = 100;

    private final ACRAConfiguration config;
    private final PackageManagerWrapper pm;

    LogCatCollector(ACRAConfiguration config, PackageManagerWrapper pm) {
        super(ReportField.LOGCAT, ReportField.EVENTSLOG, ReportField.RADIOLOG);
        this.config = config;
        this.pm = pm;
    }

    /**
     * Executes the logcat command with arguments taken from
     * {@link ReportsCrashes#logcatArguments()}
     *
     * @param bufferName The name of the buffer to be read: "main" (default), "radio" or "events".
     * @return A {@link String} containing the latest lines of the output.
     * Default is 100 lines, use "-t", "300" in
     * {@link ReportsCrashes#logcatArguments()} if you want 300 lines.
     * You should be aware that increasing this value causes a longer
     * report generation time and a bigger footprint on the device data
     * plan consumption.
     */
    private String collectLogCat(@Nullable String bufferName) {
        final int myPid = android.os.Process.myPid();
        final String myPidStr = config.logcatFilterByPid() && myPid > 0 ? Integer.toString(myPid) + "):" : null;

        final List<String> commandLine = new ArrayList<String>();
        commandLine.add("logcat");
        if (bufferName != null) {
            commandLine.add("-b");
            commandLine.add(bufferName);
        }

        // "-t n" argument has been introduced in FroYo (API level 8). For
        // devices with lower API level, we will have to emulate its job.
        final int tailCount;
        final List<String> logcatArgumentsList = config.logcatArguments();

        final int tailIndex = logcatArgumentsList.indexOf("-t");
        if (tailIndex > -1 && tailIndex < logcatArgumentsList.size()) {
            tailCount = Integer.parseInt(logcatArgumentsList.get(tailIndex + 1));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                logcatArgumentsList.remove(tailIndex + 1);
                logcatArgumentsList.remove(tailIndex);
                logcatArgumentsList.add("-d");
            }
        } else {
            tailCount = -1;
        }

        String logcat = "N/A";
        commandLine.addAll(logcatArgumentsList);

        try {
            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));

            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Retrieving logcat output...");

            // Dump stderr to null
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IOUtils.streamToString(process.getErrorStream());
                    } catch (IOException ignored) {
                    }
                }
            }).start();

            logcat = IOUtils.streamToString(process.getInputStream(), new Predicate<String>() {
                @Override
                public boolean apply(String s) {
                    return myPidStr == null || s.contains(myPidStr);
                }
            }, tailCount > 0 ? tailCount : DEFAULT_TAIL_COUNT);

        } catch (IOException e) {
            ACRA.log.e(LOG_TAG, "LogCatCollector.collectLogCat could not retrieve data.", e);
        }

        return logcat;
    }

    @Override
    boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return super.shouldCollect(crashReportFields, collect, reportBuilder) && (pm.hasPermission(Manifest.permission.READ_LOGS) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
    }

    @NonNull
    @Override
    String collect(ReportField reportField, ReportBuilder reportBuilder) {
        String bufferName = null;
        switch (reportField) {
            case LOGCAT:
                bufferName = null;
                break;
            case EVENTSLOG:
                bufferName = "events";
                break;
            case RADIOLOG:
                bufferName = "radio";
                break;
        }
        return collectLogCat(bufferName);
    }
}
