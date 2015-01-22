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

import static org.acra.ACRA.LOG_TAG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.annotation.ReportsCrashes;
import org.acra.util.BoundedLinkedList;

import android.util.Log;

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
     * @param bufferName
     *            The name of the buffer to be read: "main" (default), "radio"
     *            or "events".
     * @return A {@link String} containing the latest lines of the output.
     *         Default is 100 lines, use "-t", "300" in
     *         {@link ReportsCrashes#logcatArguments()} if you want 300 lines.
     *         You should be aware that increasing this value causes a longer
     *         report generation time and a bigger footprint on the device data
     *         plan consumption.
     */
    public static String collectLogCat(String bufferName) {
        final int myPid = android.os.Process.myPid();
        String myPidStr = null;
        if (ACRA.getConfig().logcatFilterByPid() && myPid > 0) {
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
        final List<String> logcatArgumentsList = new ArrayList<String>(
                Arrays.asList(ACRA.getConfig().logcatArguments()));

        final int tailIndex = logcatArgumentsList.indexOf("-t");
        if (tailIndex > -1 && tailIndex < logcatArgumentsList.size()) {
            tailCount = Integer.parseInt(logcatArgumentsList.get(tailIndex + 1));
            if (Compatibility.getAPILevel() < 8) {
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
        
        BufferedReader bufferedReader = null;

        try {
            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES);

            Log.d(LOG_TAG, "Retrieving logcat output...");

            // Dump stderr to null
            new Thread(new Runnable() {
                public void run() {
                    try {
                        InputStream stderr = process.getErrorStream();
                        byte[] dummy = new byte[ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES];
                        while (stderr.read(dummy) >= 0)
                            ;
                    } catch (IOException e) {
                    }
                }
            }).start();

            while (true) {
                final String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                if (myPidStr == null || line.contains(myPidStr)) {
                    logcatBuf.add(line + "\n");
                }
            }

        } catch (IOException e) {
            Log.e(ACRA.LOG_TAG, "LogCatCollector.collectLogCat could not retrieve data.", e);
        } finally {
            CollectorUtil.safeClose(bufferedReader);
        }

        return logcatBuf.toString();
    }
}
