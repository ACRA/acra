/*
 * Copyright (c) 2017
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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import org.acra.ACRAConstants;
import org.acra.attachment.AcraContentProvider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author F43nd1r
 * @since 11.03.2017
 */

public final class UriUtils {
    private UriUtils() {
    }

    public static void copyFromUri(@NonNull Context context, @NonNull OutputStream outputStream, @NonNull Uri uri) throws IOException {
        try (final InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Could not open " + uri.toString());
            }
            final byte[] buffer = new byte[ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    @NonNull
    public static String getFileNameFromUri(@NonNull Context context, @NonNull Uri uri) throws FileNotFoundException {
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        throw new FileNotFoundException("Could not resolve filename of " + uri);
    }

    @NonNull
    public static String getMimeType(@NonNull Context context, @NonNull Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final ContentResolver contentResolver = context.getContentResolver();
            String type = contentResolver.getType(uri);
            if (type != null) return type;
        }
        return AcraContentProvider.guessMimeType(uri);
    }

}
