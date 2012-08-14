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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.acra.util.BoundedLinkedList;

import android.app.Application;
import android.content.Context;

/**
 * Collects the N last lines of a text stream. Use this collector if your
 * application handles its own logging system.
 * 
 * @author Kevin Gaudin
 * 
 */
class LogFileCollector {

    /**
     * Private constructor to prevent instantiation.
     */
    private LogFileCollector() {
    };

    /**
     * Reads the last lines of a custom log file. The file name is assumed as
     * located in the {@link Application#getFilesDir()} directory if it does not
     * contain any path separator.
     * 
     * @param context
     * @param fileName
     * @param numberOfLines
     * @return
     * @throws IOException
     */
    public static String collectLogFile(Context context, String fileName, int numberOfLines) throws IOException {
        BoundedLinkedList<String> resultBuffer = new BoundedLinkedList<String>(numberOfLines);
        final BufferedReader reader;
        if (fileName.contains("/")) {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)), 1024);
        } else {
            reader = new BufferedReader(new InputStreamReader(context.openFileInput(fileName)), 1024);
        }
        String line = reader.readLine();
        while (line != null) {
            resultBuffer.add(line + "\n");
            line = reader.readLine();
        }
        return resultBuffer.toString();
    }
}
