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

import android.util.Log;

/**
 * Collects results of the <code>dumpsys</code> command.
 * 
 * @author Kevin Gaudin
 * 
 */
class DumpSysCollector {

    /**
     * Collect results of the <code>dumpsys meminfo</code> command restricted to
     * this application process.
     * 
     * @return The execution result.
     */
    protected static String collectMemInfo() {
        StringBuilder meminfo = new StringBuilder();
        try {
            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("dumpsys");
            commandLine.add("meminfo");
            commandLine.add(Integer.toString(android.os.Process.myPid()));

            Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            Log.d(LOG_TAG, "Retrieving dumpsys meminfo output...");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                meminfo.append(line);
                meminfo.append("\n");
            }

        } catch (IOException e) {
            Log.e(ACRA.LOG_TAG, "DumpSysCollector.meminfo could not retrieve data", e);
        }

        return meminfo.toString();
    }

}