/*
 * Copyright (c) 2017 the ACRA team
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

package org.acra.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.internal.util.Predicate;

import org.acra.ACRAConstants;
import org.acra.collections.BoundedLinkedList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author F43nd1r
 * @since 30.11.2017
 */

public class StreamReader {
    private static final int NO_LIMIT = -1;
    private static final int INDEFINITE = -1;
    private final InputStream inputStream;
    private int limit = NO_LIMIT;
    private int timeout = INDEFINITE;
    private Predicate<String> filter = null;

    public StreamReader(@NonNull String filename) throws FileNotFoundException {
        this(new File(filename));
    }

    public StreamReader(@NonNull File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    public StreamReader(@NonNull InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @NonNull
    public StreamReader setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @NonNull
    public StreamReader setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @NonNull
    public StreamReader setFilter(Predicate<String> filter) {
        this.filter = filter;
        return this;
    }

    @NonNull
    public String read() throws IOException {
        final String text = timeout == INDEFINITE ? readFully() : readWithTimeout();
        if (filter == null) {
            if (limit == NO_LIMIT) {
                return text;
            }
            final String[] lines = text.split("\\r?\\n");
            if(lines.length <= limit){
                return text;
            }
            return TextUtils.join("\n", Arrays.copyOfRange(lines, lines.length - limit, lines.length));
        }
        final String[] lines = text.split("\\r?\\n");
        final List<String> buffer = limit == NO_LIMIT ? new LinkedList<>() : new BoundedLinkedList<>(limit);
        for (String line : lines) {
            if (filter.apply(line)) {
                buffer.add(line);
            }
        }
        return TextUtils.join("\n", buffer);
    }

    @NonNull
    private String readFully() throws IOException {
        final Reader input = new InputStreamReader(inputStream);
        try {
            final StringWriter output = new StringWriter();
            final char[] buffer = new char[ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return output.toString();
        } finally {
            IOUtils.safeClose(input);
        }
    }

    @NonNull
    private String readWithTimeout() throws IOException {
        final long until = System.currentTimeMillis() + timeout;
        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final byte[] buffer = new byte[ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES];
            int count;
            while ((count = fillBufferUntil(buffer, until)) != -1) {
                output.write(buffer, 0, count);
            }
            return output.toString();
        } finally {
            IOUtils.safeClose(inputStream);
        }
    }

    private int fillBufferUntil(@NonNull byte[] buffer, long until) throws IOException {
        int bufferOffset = 0;
        while (System.currentTimeMillis() < until && bufferOffset < buffer.length) {
            final int readResult = inputStream.read(buffer, bufferOffset, Math.min(inputStream.available(), buffer.length - bufferOffset));
            if (readResult == -1) break;
            bufferOffset += readResult;
        }
        return bufferOffset;
    }
}
