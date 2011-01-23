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
package org.acra;

import static org.acra.ACRA.LOG_TAG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.acra.util.BoundedLinkedList;

import android.util.Log;

class LogCatCollector {

    private static final int DEFAULT_TAIL_COUNT = 100;

    protected static String collectLogCat(String bufferName) {
        BoundedLinkedList<String> logcatBuf = null;
        try {
            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("logcat");
            if (bufferName != null) {
                commandLine.add("-b");
                commandLine.add(bufferName);
            }
            // "-t n" argument has been introduced in FroYo (API level 8). For
            // devices with lower API level, we will have to emulate its job.
            int tailCount = -1;
            List<String> logcatArgumentsList = new ArrayList<String>(Arrays.asList(ACRA.getConfig().logcatArguments()));

            int tailIndex = logcatArgumentsList.indexOf("-t");
            if (tailIndex > -1 && tailIndex < logcatArgumentsList.size()) {
                tailCount = Integer.parseInt(logcatArgumentsList.get(tailIndex + 1));
                if (Compatibility.getAPILevel() < 8) {
                    logcatArgumentsList.remove(tailIndex + 1);
                    logcatArgumentsList.remove(tailIndex);
                    logcatArgumentsList.add("-d");
                }
            }
            logcatBuf = new BoundedLinkedList<String>(tailCount > 0 ? tailCount : DEFAULT_TAIL_COUNT);
            commandLine.addAll(logcatArgumentsList);

            Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            Log.d(LOG_TAG, "Retrieving logcat output...");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logcatBuf.add(line+"\n");
            }

        } catch (IOException e) {
            Log.e(ACRA.LOG_TAG, "LogCatCollector.collectLogcat could not retrieve data.", e);
        }

        return logcatBuf.toString();
    }
}