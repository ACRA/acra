/*
 *  Copyright 2016
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
package org.acra.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.internal.util.Predicate;

import org.acra.ACRAConstants;
import org.acra.collections.BoundedLinkedList;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * @author William Ferguson, F43nd1r
 * @since 4.6.0
 */
public final class IOUtils {

    private static final Predicate<String> DEFAULT_FILTER = new Predicate<String>() {
        @Override
        public boolean apply(String s) {
            return true;
        }
    };
    private static final int NO_LIMIT = -1;

    private IOUtils() {
    }


    /**
     * Closes a Closeable.
     *
     * @param closeable Closeable to close. If closeable is null then method just returns.
     */
    public static void safeClose(@Nullable Closeable closeable) {
        if (closeable == null) return;

        try {
            closeable.close();
        } catch (IOException ignored) {
            // We made out best effort to release this resource. Nothing more we can do.
        }
    }

    /**
     * Reads an InputStream into a string
     *
     * @param input the stream
     * @return the read string
     * @throws IOException
     */
    @NonNull
    public static String streamToString(@NonNull InputStream input) throws IOException {
        return streamToString(input, DEFAULT_FILTER, NO_LIMIT);
    }

    /**
     * Reads an InputStream into a string
     *
     * @param input  the stream
     * @param filter should return false for lines which should be excluded
     * @return the read string
     * @throws IOException
     */
    @NonNull
    public static String streamToString(@NonNull InputStream input, Predicate<String> filter) throws IOException {
        return streamToString(input, filter, NO_LIMIT);
    }

    /**
     * Reads an InputStream into a string
     *
     * @param input the stream
     * @param limit the maximum number of lines to read (the last x lines are kept)
     * @return the read string
     * @throws IOException
     */
    @NonNull
    public static String streamToString(@NonNull InputStream input, int limit) throws IOException {
        return streamToString(input, DEFAULT_FILTER, limit);
    }

    /**
     * Reads an InputStream into a string
     *
     * @param input  the stream
     * @param filter should return false for lines which should be excluded
     * @param limit the maximum number of lines to read (the last x lines are kept)
     * @return the read string
     * @throws IOException
     */
    @NonNull
    public static String streamToString(@NonNull InputStream input, Predicate<String> filter, int limit) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input), ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES);
        try {
            String line;
            final List<String> buffer = limit == NO_LIMIT ? new LinkedList<String>() : new BoundedLinkedList<String>(limit);
            while ((line = reader.readLine()) != null) {
                if (filter.apply(line)) {
                    buffer.add(line);
                }
            }
            return TextUtils.join("\n", buffer);
        } finally {
            safeClose(reader);
        }
    }
}
