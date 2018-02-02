/*
 * Copyright (c) 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.legacy;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.data.CrashReportData;
import org.acra.file.CrashReportPersister;
import org.acra.file.ReportLocator;
import org.acra.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Converts acras old file format to json
 *
 * @author F43nd1r
 * @since 12.10.2016
 */

class ReportConverter {
    private static final int NONE = 0, SLASH = 1, UNICODE = 2, CONTINUE = 3, KEY_DONE = 4, IGNORE = 5;
    private final Context context;

    ReportConverter(@NonNull Context context) {
        this.context = context;
    }

    void convert() {
        ACRA.log.i(LOG_TAG, "Converting unsent ACRA reports to json");
        final ReportLocator locator = new ReportLocator(context);
        final CrashReportPersister persister = new CrashReportPersister();
        final List<File> reportFiles = new ArrayList<>();
        reportFiles.addAll(Arrays.asList(locator.getUnapprovedReports()));
        reportFiles.addAll(Arrays.asList(locator.getApprovedReports()));
        int converted = 0;
        for (File report : reportFiles) {
            InputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(report), ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES);
                final CrashReportData data = legacyLoad(new InputStreamReader(in, "ISO8859-1")); //$NON-NLS-1$
                if (data.containsKey(ReportField.REPORT_ID) && data.containsKey(ReportField.USER_CRASH_DATE)) {
                    persister.store(data, report);
                    converted++;
                } else {
                    //reports without these keys are probably invalid
                    IOUtils.deleteFile(report);
                }
            } catch (Throwable e) {
                try {
                    //If this succeeds the report has already been converted, happens e.g. on preference clear.
                    persister.load(report);
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Tried to convert already converted report file " + report.getPath() + ". Ignoring");
                } catch (Throwable t) {
                    //File matches neither of the known formats, remove it.
                    ACRA.log.w(LOG_TAG, "Unable to read report file " + report.getPath() + ". Deleting", e);
                    IOUtils.deleteFile(report);
                }
            } finally {
                IOUtils.safeClose(in);
            }
        }
        ACRA.log.i(LOG_TAG, "Converted " + converted + " unsent reports");
    }


    /**
     * Loads properties from the specified InputStream. The properties are of
     * the form <code>key=value</code>, one property per line. It may be not
     * encode as 'ISO-8859-1'.The {@code Properties} file is interpreted
     * according to the following rules:
     * <ul>
     * <li>Empty lines are ignored.</li>
     * <li>Lines starting with either a "#" or a "!" are comment lines and are
     * ignored.</li>
     * <li>A backslash at the end of the line escapes the following newline
     * character ("\r", "\n", "\r\n"). If there's a whitespace after the
     * backslash it will just escape that whitespace instead of concatenating
     * the lines. This does not apply to comment lines.</li>
     * <li>A property line consists of the key, the space between the key and
     * the value, and the value. The key goes up to the first whitespace, "=" or
     * ":" that is not escaped. The space between the key and the value contains
     * either one whitespace, one "=" or one ":" and any number of additional
     * whitespaces before and after that character. The value starts with the
     * first character after the space between the key and the value.</li>
     * <li>Following escape sequences are recognized: "\ ", "\\", "\r", "\n",
     * "\!", "\#", "\t", "\b", "\f", and "&#92;uXXXX" (unicode character).</li>
     * </ul>
     *
     * @param reader Reader from which to read the properties of this CrashReportData.
     * @return CrashReportData read from the supplied Reader.
     * @throws java.io.IOException if the properties could not be read.
     * @since 1.6
     */
    @NonNull
    private synchronized CrashReportData legacyLoad(@NonNull Reader reader) throws IOException {
        int mode = NONE, unicode = 0, count = 0;
        char nextChar;
        char[] buf = new char[40];
        int offset = 0, keyLength = -1, intVal;
        boolean firstChar = true;

        final CrashReportData crashData = new CrashReportData();
        final BufferedReader br = new BufferedReader(reader, ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES);
        try {
            while (true) {
                intVal = br.read();
                if (intVal == -1) {
                    break;
                }
                nextChar = (char) intVal;

                if (offset == buf.length) {
                    final char[] newBuf = new char[buf.length * 2];
                    System.arraycopy(buf, 0, newBuf, 0, offset);
                    buf = newBuf;
                }
                if (mode == UNICODE) {
                    final int digit = Character.digit(nextChar, 16);
                    if (digit >= 0) {
                        unicode = (unicode << 4) + digit;
                        if (++count < 4) {
                            continue;
                        }
                    } else if (count <= 4) {
                        // luni.09=Invalid Unicode sequence: illegal character
                        throw new IllegalArgumentException("luni.09");
                    }
                    mode = NONE;
                    buf[offset++] = (char) unicode;
                    if (nextChar != '\n' && nextChar != '\u0085') {
                        continue;
                    }
                }
                if (mode == SLASH) {
                    mode = NONE;
                    switch (nextChar) {
                        case '\r':
                            mode = CONTINUE; // Look for a following \n
                            continue;
                        case '\u0085':
                        case '\n':
                            mode = IGNORE; // Ignore whitespace on the next line
                            continue;
                        case 'b':
                            nextChar = '\b';
                            break;
                        case 'f':
                            nextChar = '\f';
                            break;
                        case 'n':
                            nextChar = '\n';
                            break;
                        case 'r':
                            nextChar = '\r';
                            break;
                        case 't':
                            nextChar = '\t';
                            break;
                        case 'u':
                            mode = UNICODE;
                            unicode = count = 0;
                            continue;
                    }
                } else {
                    switch (nextChar) {
                        case '#':
                        case '!':
                            if (firstChar) {
                                while (true) {
                                    intVal = br.read();
                                    if (intVal == -1) {
                                        break;
                                    }
                                    nextChar = (char) intVal; // & 0xff
                                    // not
                                    // required
                                    if (nextChar == '\r' || nextChar == '\n' || nextChar == '\u0085') {
                                        break;
                                    }
                                }
                                continue;
                            }
                            break;
                        case '\n':
                            if (mode == CONTINUE) { // Part of a \r\n sequence
                                mode = IGNORE; // Ignore whitespace on the next line
                                continue;
                            }
                            // fall into the next case
                        case '\u0085':
                        case '\r':
                            mode = NONE;
                            firstChar = true;
                            if (offset > 0 || (offset == 0 && keyLength == 0)) {
                                if (keyLength == -1) {
                                    keyLength = offset;
                                }
                                final String temp = new String(buf, 0, offset);
                                putKeyValue(crashData, temp.substring(0, keyLength), temp.substring(keyLength));
                            }
                            keyLength = -1;
                            offset = 0;
                            continue;
                        case '\\':
                            if (mode == KEY_DONE) {
                                keyLength = offset;
                            }
                            mode = SLASH;
                            continue;
                        case ':':
                        case '=':
                            if (keyLength == -1) { // if parsing the key
                                mode = NONE;
                                keyLength = offset;
                                continue;
                            }
                            break;
                    }
                    if (Character.isWhitespace(nextChar)) {
                        if (mode == CONTINUE) {
                            mode = IGNORE;
                        }
                        // if key length == 0 or value length == 0
                        if (offset == 0 || offset == keyLength || mode == IGNORE) {
                            continue;
                        }
                        if (keyLength == -1) { // if parsing the key
                            mode = KEY_DONE;
                            continue;
                        }
                    }
                    if (mode == IGNORE || mode == CONTINUE) {
                        mode = NONE;
                    }
                }
                firstChar = false;
                if (mode == KEY_DONE) {
                    keyLength = offset;
                    mode = NONE;
                }
                buf[offset++] = nextChar;
            }
            if (mode == UNICODE && count <= 4) {
                // luni.08=Invalid Unicode sequence: expected format \\uxxxx
                throw new IllegalArgumentException("luni.08");
            }
            if (keyLength == -1 && offset > 0) {
                keyLength = offset;
            }
            if (keyLength >= 0) {
                final String temp = new String(buf, 0, offset);
                String value = temp.substring(keyLength);
                if (mode == SLASH) {
                    value += "\u0000";
                }
                putKeyValue(crashData, temp.substring(0, keyLength), value);
            }

            IOUtils.safeClose(reader);

            return crashData;
        } finally {
            IOUtils.safeClose(br);
        }
    }

    private void putKeyValue(@NonNull CrashReportData crashData, @NonNull String key, @NonNull String value){
        try {
            crashData.put(key, new JSONObject(value));
        } catch (JSONException e1) {
            try {
                crashData.put(key, Double.valueOf(value));
            } catch (NumberFormatException e2) {
                switch (value) {
                    case "true":
                        crashData.put(key, true);
                        break;
                    case "false":
                        crashData.put(key, false);
                        break;
                    default:
                        crashData.put(key, value);
                        break;
                }
            }
        }
    }
}
