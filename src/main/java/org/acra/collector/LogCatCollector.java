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

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;
import org.acra.collections.BoundedLinkedList;
import org.acra.util.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;


/**
 * Executes logcat commands and collects it's output.
 * 
 * @author Kevin Gaudin
 * 
 */
class LogCatCollector {

    /**
     * Default number of latest lines kept from the logcat output.
     */
    private static final int DEFAULT_TAIL_COUNT = 100;

    /**
     * Executes the logcat command with arguments taken from
     * {@link ReportsCrashes#logcatArguments()}
     *
     * @param config        AcraConfig to use when collecting logcat.
     * @param bufferName    The name of the buffer to be read: "main" (default), "radio" or "events".
     * @return A {@link String} containing the latest lines of the output.
     *         Default is 100 lines, use "-t", "300" in
     *         {@link ReportsCrashes#logcatArguments()} if you want 300 lines.
     *         You should be aware that increasing this value causes a longer
     *         report generation time and a bigger footprint on the device data
     *         plan consumption.
     */
    public String collectLogCat(@NonNull ACRAConfiguration config, @Nullable String bufferName) {
        final int myPid = android.os.Process.myPid();
        String myPidStr = null;
        if (config.logcatFilterByPid() && myPid > 0) {
            myPidStr = Integer.toString(myPid) +"):";
        }

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

        final LinkedList<String> logcatBuf = new BoundedLinkedList<String>(tailCount > 0 ? tailCount
                : DEFAULT_TAIL_COUNT);
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

            final String finalMyPidStr = myPidStr;
            logcatBuf.add(IOUtils.streamToString(process.getInputStream(), new Predicate<String>() {
                @Override
                public boolean apply(String s) {
                    return finalMyPidStr == null || s.contains(finalMyPidStr);
                }
            }));

        } catch (IOException e) {
            ACRA.log.e(LOG_TAG, "LogCatCollector.collectLogCat could not retrieve data.", e);
        }

        return logcatBuf.toString();
    }
}
