/*
 * java.util.Properties.java modified by Kevin Gaudin to allow usage of enums as keys.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.acra.file;

import android.support.annotation.NonNull;

import org.acra.data.CrashReportData;
import org.acra.util.IOUtils;
import org.acra.util.StreamReader;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Handles persistence of {@link CrashReportData}
 */
public final class CrashReportPersister {

    /**
     * Loads properties from the specified {@code File}.
     *
     * @param file Report file from which to load the CrashData.
     * @return CrashReportData read from the supplied File.
     * @throws IOException   if error occurs during reading from the {@code File}.
     * @throws JSONException if the stream cannot be parsed as a JSON object.
     */
    @NonNull
    public CrashReportData load(@NonNull File file) throws IOException, JSONException {
        return new CrashReportData(new StreamReader(file).read());
    }

    /**
     * Stores the mappings in this Properties to the specified OutputStream,
     * putting the specified comment at the beginning. The output from this
     * method is suitable for being read by the load() method.
     *
     * @param crashData CrashReportData to save.
     * @param file      File into which to store the CrashReportData.
     * @throws IOException   if the CrashReportData could not be written to the OutputStream.
     * @throws JSONException if the crashData could not be converted to JSON.
     */
    public void store(@NonNull CrashReportData crashData, @NonNull File file) throws IOException, JSONException {
        IOUtils.writeStringToFile(file, crashData.toJSON());
    }
}
