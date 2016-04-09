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

import android.support.annotation.NonNull;

import org.acra.ACRA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects results of the <code>dumpsys</code> command.
 * 
 * @author Kevin Gaudin
 * 
 */
final class DumpSysCollector {
    private DumpSysCollector(){}

    /**
     * Collect results of the <code>dumpsys meminfo</code> command restricted to
     * this application process.
     * 
     * @return The execution result.
     */
    @NonNull
    public static String collectMemInfo() {

        final StringBuilder meminfo = new StringBuilder();
        try {
            final List<String> commandLine = new ArrayList<String>();
            commandLine.add("dumpsys");
            commandLine.add("meminfo");
            commandLine.add(Integer.toString(android.os.Process.myPid()));

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            meminfo.append(CollectorUtil.streamToString(process.getInputStream()));

        } catch (IOException e) {
            ACRA.log.e(LOG_TAG, "DumpSysCollector.meminfo could not retrieve data", e);
		}

        return meminfo.toString();
    }
}