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
package org.acra.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.acra.ACRAConstants
import org.acra.attachment.AcraContentProvider.Companion.guessMimeType
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream

/**
 * @author F43nd1r
 * @since 11.03.2017
 */
object UriUtils {
    @Throws(IOException::class)
    fun copyFromUri(context: Context, outputStream: OutputStream, uri: Uri) {
        context.contentResolver.openInputStream(uri)?.copyTo(outputStream, bufferSize = ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES) ?: throw FileNotFoundException(
                "Could not open $uri")
    }

    @Throws(FileNotFoundException::class)
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        throw FileNotFoundException("Could not resolve filename of $uri")
    }

    fun getMimeType(context: Context, uri: Uri): String {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)?.let { return it }
        }
        return guessMimeType(uri)
    }
}