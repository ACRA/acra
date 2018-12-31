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

import android.util.Base64;
import org.acra.ACRA;
import org.acra.ACRAConstants;

import java.io.*;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author William Ferguson &amp; F43nd1r
 * @since 4.6.0
 */
public final class IOUtils {

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

    public static void deleteFile(@NonNull File file) {
        final boolean deleted = file.delete();
        if (!deleted) {
            ACRA.log.w(LOG_TAG, "Could not delete file: " + file);
        }
    }

    public static void writeStringToFile(@NonNull File file, @NonNull String content) throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), ACRAConstants.UTF8);
        try {
            writer.write(content);
            writer.flush();
        } finally {
            safeClose(writer);
        }
    }

    @Nullable
    public static String serialize(@NonNull Serializable serializable) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream outputStream = new ObjectOutputStream(out)) {
            outputStream.writeObject(serializable);
            return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static <T extends Serializable> T deserialize(@NonNull Class<T> clazz, @Nullable String s) {
        if (s != null) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(s, Base64.DEFAULT)))) {
                Object o = inputStream.readObject();
                if (clazz.isInstance(o)) {
                    return clazz.cast(o);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
