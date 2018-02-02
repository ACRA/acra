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

package org.acra.attachment;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.file.Directory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Provides access to attachments for senders
 * For uri schema, see {@link AcraCore#attachmentUris()}
 *
 * @author F43nd1r
 * @since 13.03.2017
 */

public class AcraContentProvider extends ContentProvider {
    private static final String[] COLUMNS = {
            OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
    private static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";
    private String authority;

    @Override
    public boolean onCreate() {
        //noinspection ConstantConditions
        authority = getAuthority(getContext());
        if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Registered content provider for authority " + authority);
        return true;
    }

    /**
     * Provides file metadata
     *
     * @param uri           the file uri
     * @param projection    any combination of {@link OpenableColumns#DISPLAY_NAME} and {@link OpenableColumns#SIZE}
     * @param selection     ignored
     * @param selectionArgs ignored
     * @param sortOrder     ignored
     * @return file metadata in a cursor with a single row
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Query: " + uri);
        final File file = getFileForUri(uri);
        if (file == null) {
            return null;
        }
        if (projection == null) {
            projection = COLUMNS;
        }
        final Map<String, Object> columnValueMap = new LinkedHashMap<>();
        for (String column : projection) {
            if (column.equals(OpenableColumns.DISPLAY_NAME)) {
                columnValueMap.put(OpenableColumns.DISPLAY_NAME, file.getName());
            } else if (column.equals(OpenableColumns.SIZE)) {
                columnValueMap.put(OpenableColumns.SIZE, file.length());
            }
        }
        final MatrixCursor cursor = new MatrixCursor(columnValueMap.keySet().toArray(new String[columnValueMap.size()]), 1);
        cursor.addRow(columnValueMap.values());
        return cursor;
    }

    /**
     * @param uri the file uri
     * @return file represented by uri, or null if it can't be resolved
     */
    @Nullable
    private File getFileForUri(@NonNull Uri uri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) || !authority.equals(uri.getAuthority())) {
            return null;
        }
        final List<String> segments = new ArrayList<>(uri.getPathSegments());
        if (segments.size() < 2) return null;
        final String dir = segments.remove(0).toUpperCase();
        try {
            final Directory directory = Directory.valueOf(dir);
            //noinspection ConstantConditions
            return directory.getFile(getContext(), TextUtils.join(File.separator, segments));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Provides file mimeType
     *
     * @param uri the file uri
     * @return mimeType, default is {@link #MIME_TYPE_OCTET_STREAM}
     * @see #guessMimeType(Uri)
     */
    @NonNull
    @Override
    public String getType(@NonNull Uri uri) {
        return guessMimeType(uri);
    }

    /**
     * @param uri    ignored
     * @param values ignored
     * @throws UnsupportedOperationException always
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("No insert supported");
    }

    /**
     * @param uri           ignored
     * @param selection     ignored
     * @param selectionArgs ignored
     * @throws UnsupportedOperationException always
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No delete supported");
    }

    /**
     * @param uri           ignored
     * @param values        ignored
     * @param selection     ignored
     * @param selectionArgs ignored
     * @throws UnsupportedOperationException always
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No update supported");
    }

    /**
     * Open a file for read
     *
     * @param uri  the file uri
     * @param mode ignored
     * @return a {@link ParcelFileDescriptor} for the File
     * @throws FileNotFoundException if the file cannot be resolved
     */
    @NonNull
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        final File file = getFileForUri(uri);
        if (file == null || !file.exists()) throw new FileNotFoundException("File represented by uri " + uri + " could not be found");
        if (ACRA.DEV_LOGGING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ACRA.log.d(ACRA.LOG_TAG, getCallingPackage() + " opened " + file.getPath());
            } else {
                ACRA.log.d(ACRA.LOG_TAG, file.getPath() + " was opened by an application");
            }
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    /**
     * @param context a a context
     * @return authority of this provider
     */
    @NonNull
    private static String getAuthority(@NonNull Context context) {
        return context.getPackageName() + ".acra";
    }

    /**
     * Get an uri for this content provider for the given file
     *
     * @param context a context
     * @param file    the file
     * @return the uri
     */
    @NonNull
    public static Uri getUriForFile(@NonNull Context context, @NonNull File file) {
        return getUriForFile(context, Directory.ROOT, file.getPath());
    }

    /**
     * Get an uri for this content provider for the given file
     *
     * @param context      a context
     * @param directory    the directory, to with the path is relative
     * @param relativePath the file path
     * @return the uri
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public static Uri getUriForFile(@NonNull Context context, @NonNull Directory directory, @NonNull String relativePath) {
        final Uri.Builder builder = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(getAuthority(context))
                .appendPath(directory.name().toLowerCase());
        for (String segment : relativePath.split(Pattern.quote(File.separator))) {
            if (segment.length() > 0) {
                builder.appendPath(segment);
            }
        }
        return builder.build();
    }


    /**
     * Tries to guess the mime type from uri extension
     *
     * @param uri the uri
     * @return the mime type of the uri, with fallback {@link #MIME_TYPE_OCTET_STREAM}
     */
    @NonNull
    public static String guessMimeType(@NonNull Uri uri) {
        String type = null;
        final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                .toString());
        if (fileExtension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        if (type == null) {
            type = MIME_TYPE_OCTET_STREAM;
        }
        return type;
    }
}
