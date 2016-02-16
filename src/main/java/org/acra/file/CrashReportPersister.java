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

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collector.CollectorUtil;
import org.acra.collector.CrashReportData;

import java.io.*;
import java.util.Map;

/**
 * Stores a crash reports data with {@link org.acra.ReportField} enum values as keys.
 * This is basically the source of {@link java.util.Properties} adapted to extend an
 * EnumMap instead of Hashtable and with a few tweaks to avoid losing crazy
 * amounts of android time in the generation of a date comment when storing to
 * file.
 */
public final class CrashReportPersister {

    private static final int NONE = 0, SLASH = 1, UNICODE = 2, CONTINUE = 3, KEY_DONE = 4, IGNORE = 5;
    private static final String LINE_SEPARATOR = "\n";

    /**
     * Loads properties from the specified {@code InputStream}. The encoding is ISO8859-1.
     *
     * @param file  Report file from which to load the CrashData.
     * @return CrashReportData read from the supplied InputStream.
     * @throws java.io.IOException if error occurs during reading from the {@code InputStream}.
     */
    @NonNull
    public CrashReportData load(@NonNull File file) throws IOException {

        final FileInputStream in = new FileInputStream(file);
        try {
            final BufferedInputStream bis = new BufferedInputStream(in, ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES);
            return load(new InputStreamReader(bis, "ISO8859-1")); //$NON-NLS-1$
        } finally {
            in.close();
        }
    }

    /**
     * Stores the mappings in this Properties to the specified OutputStream,
     * putting the specified comment at the beginning. The output from this
     * method is suitable for being read by the load() method.
     *
     * @param crashData     CrashReportData to save.
     * @param file          File into which to store the CrashReportData.
     * @throws java.io.IOException if the CrashReportData could not be written to the OutputStream.
     */
    public void store(@NonNull CrashReportData crashData, @NonNull File file) throws IOException {

        OutputStreamWriter writer = null;
        try {
            final StringBuilder buffer = new StringBuilder(200);
            writer = new OutputStreamWriter(new FileOutputStream(file), "ISO8859_1"); //$NON-NLS-1$

            for (final Map.Entry<ReportField, String> entry : crashData.entrySet()) {
                final String key = entry.getKey().toString();
                dumpString(buffer, key, true);
                buffer.append('=');
                dumpString(buffer, entry.getValue(), false);
                buffer.append(LINE_SEPARATOR);
                writer.write(buffer.toString());
                buffer.setLength(0);
            }
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
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
     * @param reader    Reader from which to read the properties of this CrashReportData.
     * @return CrashReportData read from the supplied Reader.
     * @throws java.io.IOException if the properties could not be read.
     * @since 1.6
     */
    @NonNull
    private synchronized CrashReportData load(@NonNull Reader reader) throws IOException {
        int mode = NONE, unicode = 0, count = 0;
        char nextChar, buf[] = new char[40]; //TODO: consider using a list instead of manually increasing the size when needed
        int offset = 0, keyLength = -1, intVal;
        boolean firstChar = true;

        final CrashReportData crashData = new CrashReportData();
        final BufferedReader br = new BufferedReader(reader, ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES);

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
                        crashData.put(Enum.valueOf(ReportField.class, temp.substring(0, keyLength)), temp.substring(keyLength));
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
            final ReportField key = Enum.valueOf(ReportField.class, temp.substring(0, keyLength));
            String value = temp.substring(keyLength);
            if (mode == SLASH) {
                value += "\u0000";
            }
            crashData.put(key, value);
        }

        CollectorUtil.safeClose(reader);

        return crashData;
    }

    /**
     * Constructs a new {@code Properties} object.
     *
     * @param buffer    StringBuilder to populate with the supplied property.
     * @param string    String to append to the buffer.
     * @param key       Whether the String is a key value or not.
     */
    private void dumpString(@NonNull StringBuilder buffer, @NonNull String string, boolean key) {
        int i = 0;
        if (!key && i < string.length() && string.charAt(i) == ' ') {
            buffer.append("\\ "); //$NON-NLS-1$
            i++;
        }

        for (; i < string.length(); i++) {
            char ch = string.charAt(i);
            switch (ch) {
            case '\t':
                buffer.append("\\t"); //$NON-NLS-1$
                break;
            case '\n':
                buffer.append("\\n"); //$NON-NLS-1$
                break;
            case '\f':
                buffer.append("\\f"); //$NON-NLS-1$
                break;
            case '\r':
                buffer.append("\\r"); //$NON-NLS-1$
                break;
            default:
                if ("\\#!=:".indexOf(ch) >= 0 || (key && ch == ' ')) {
                    buffer.append('\\');
                }
                if (ch >= ' ' && ch <= '~') {
                    buffer.append(ch);
                } else {
                    final String hex = Integer.toHexString(ch);
                    buffer.append("\\u"); //$NON-NLS-1$
                    for (int j = 0; j < 4 - hex.length(); j++) {
                        buffer.append("0"); //$NON-NLS-1$
                    }
                    buffer.append(hex);
                }
            }
        }
    }
}
