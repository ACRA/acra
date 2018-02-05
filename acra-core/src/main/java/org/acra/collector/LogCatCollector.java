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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;
import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.prefs.SharedPreferencesFactory;
import org.acra.util.PackageManagerWrapper;
import org.acra.util.StreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;


/**
 * Executes logcat commands and collects it's output.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector.class)
public final class LogCatCollector extends BaseReportFieldCollector {
    private static final int READ_TIMEOUT = 3000;

    public LogCatCollector() {
        super(ReportField.LOGCAT, ReportField.EVENTSLOG, ReportField.RADIOLOG);
    }

    @NonNull
    @Override
    public Order getOrder() {
        return Order.FIRST;
    }

    /**
     * Executes the logcat command with arguments taken from {@link org.acra.annotation.AcraCore#logcatArguments()}
     *
     * @param bufferName The name of the buffer to be read: "main" (default), "radio" or "events".
     * @return A string containing the latest lines of the output.
     * Default is 100 lines, use "-t", "300" in {@link org.acra.annotation.AcraCore#logcatArguments()} if you want 300 lines.
     * You should be aware that increasing this value causes a longer report generation time and a bigger footprint on the device data plan consumption.
     */
    private String collectLogCat(@NonNull CoreConfiguration config, @Nullable String bufferName) throws IOException {
        final int myPid = android.os.Process.myPid();
        // no need to filter on jellybean onwards, android does that anyway
        final String myPidStr = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && config.logcatFilterByPid() && myPid > 0 ? Integer.toString(myPid) + "):" : null;

        final List<String> commandLine = new ArrayList<>();
        commandLine.add("logcat");
        if (bufferName != null) {
            commandLine.add("-b");
            commandLine.add(bufferName);
        }

        final int tailCount;
        final List<String> logcatArgumentsList = config.logcatArguments();

        final int tailIndex = logcatArgumentsList.indexOf("-t");
        if (tailIndex > -1 && tailIndex < logcatArgumentsList.size()) {
            tailCount = Integer.parseInt(logcatArgumentsList.get(tailIndex + 1));
        } else {
            tailCount = -1;
        }

        commandLine.addAll(logcatArgumentsList);
        final Process process = new ProcessBuilder().command(commandLine).redirectErrorStream(true).start();
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Retrieving logcat output (buffer:" + (bufferName == null ? "default" : bufferName) + ")...");

        try {
            return streamToString(config, process.getInputStream(), myPidStr == null ? null : s -> s.contains(myPidStr), tailCount);
        } finally {
            process.destroy();
        }
    }

    @Override
    boolean shouldCollect(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportField collect, @NonNull ReportBuilder reportBuilder) {
        return super.shouldCollect(context, config, collect, reportBuilder) &&
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN || new PackageManagerWrapper(context).hasPermission(Manifest.permission.READ_LOGS))
                && new SharedPreferencesFactory(context, config).create().getBoolean(ACRA.PREF_ENABLE_SYSTEM_LOGS, true);
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) throws IOException {
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
        target.put(reportField, collectLogCat(config, bufferName));
    }

    /**
     * Reads an InputStream into a string respecting blocking settings.
     *
     * @param input  the stream
     * @param filter should return false for lines which should be excluded
     * @param limit  the maximum number of lines to read (the last x lines are kept)
     * @return the String that was read.
     * @throws IOException if the stream cannot be read.
     */
    @NonNull
    private String streamToString(@NonNull CoreConfiguration config, @NonNull InputStream input, @Nullable Predicate<String> filter, int limit) throws IOException {
        final StreamReader reader = new StreamReader(input).setFilter(filter).setLimit(limit);
        if (config.logcatReadNonBlocking()) {
            reader.setTimeout(READ_TIMEOUT);
        }
        return reader.read();
    }
}
