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
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.acra.ACRA;
import org.acra.file.Directory;
import org.acra.http.HttpUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author F43nd1r
 * @since 13.03.2017
 */

public class AcraContentProvider extends ContentProvider {
    private static final String[] COLUMNS = {
            OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
    private String authority;

    @Override
    public boolean onCreate() {
        authority = getContext().getPackageName() + ".acra";
        if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Registered content provider for authority " + authority);
        return true;
    }

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
        final Map<String, Object> columnValueMap = new LinkedHashMap<String, Object>();
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

    @Nullable
    private File getFileForUri(Uri uri) {
        if(!"content".equals(uri.getScheme()) || !authority.equals(uri.getAuthority())){
            return null;
        }
        final List<String> segments = new ArrayList<String>(uri.getPathSegments());
        if (segments.size() < 2) return null;
        final String dir = segments.remove(0).toUpperCase();
        try {
            final Directory directory = Directory.valueOf(dir);
            return directory.getFile(getContext(), TextUtils.join(File.separator, segments));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return HttpUtils.guessMimeType(uri);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("No insert supported");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No delete supported");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No update supported");
    }

    @Nullable
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
}
