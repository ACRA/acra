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
package org.acra.http;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import org.acra.ACRAConstants;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author F43nd1r
 * @since 11.03.2017
 */

public final class HttpUtils {
    private HttpUtils() {
    }

    @NonNull
    public static byte[] uriToByteArray(@NonNull Context context, Uri uri) throws IOException {
        final InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new FileNotFoundException("Could not open " + uri.toString());
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    /**
     * Converts a Map of parameters into a URL encoded Sting.
     *
     * @param parameters Map of parameters to convert.
     * @return URL encoded String representing the parameters.
     * @throws UnsupportedEncodingException if one of the parameters couldn't be converted to UTF-8.
     */
    @NonNull
    public static String getParamsAsFormString(@NonNull Map<?, ?> parameters) throws UnsupportedEncodingException {

        final StringBuilder dataBfr = new StringBuilder();
        for (final Map.Entry<?, ?> entry : parameters.entrySet()) {
            if (dataBfr.length() != 0) {
                dataBfr.append('&');
            }
            final Object preliminaryValue = entry.getValue();
            final Object value = (preliminaryValue == null) ? "" : preliminaryValue;
            dataBfr.append(URLEncoder.encode(entry.getKey().toString(), ACRAConstants.UTF8));
            dataBfr.append('=');
            dataBfr.append(URLEncoder.encode(value.toString(), ACRAConstants.UTF8));
        }

        return dataBfr.toString();
    }

    public static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            final Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            final int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String getMimeType(Context context, Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final ContentResolver contentResolver = context.getContentResolver();
            return contentResolver.getType(uri);
        }
        return guessMimeType(uri);
    }

    public static String guessMimeType(Uri uri){
        String type = null;
        final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                .toString());
        if (fileExtension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        if (type == null) {
            type = "application/octet-stream";
        }
        return type;
    }
}
