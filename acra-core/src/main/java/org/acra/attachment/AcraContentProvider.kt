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
package org.acra.attachment

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.text.TextUtils
import android.webkit.MimeTypeMap
import org.acra.ACRA
import org.acra.file.Directory
import org.acra.log.debug
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.regex.Pattern

/**
 * Provides access to attachments for senders
 * For uri schema, see [AcraCore.attachmentUris]
 *
 * @author F43nd1r
 * @since 13.03.2017
 */
class AcraContentProvider : ContentProvider() {
    private var authority: String? = null
    override fun onCreate(): Boolean {
        authority = getAuthority(context!!)
        if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Registered content provider for authority $authority")
        return true
    }

    /**
     * Provides file metadata
     *
     * @param uri           the file uri
     * @param projection    any combination of [OpenableColumns.DISPLAY_NAME] and [OpenableColumns.SIZE]
     * @param selection     ignored
     * @param selectionArgs ignored
     * @param sortOrder     ignored
     * @return file metadata in a cursor with a single row
     */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var proj = projection
        if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Query: $uri")
        val file = getFileForUri(uri) ?: return null
        if (proj == null) {
            proj = COLUMNS
        }
        val columnValueMap: MutableMap<String, Any?> = LinkedHashMap()
        for (column in proj) {
            if (column == OpenableColumns.DISPLAY_NAME) {
                columnValueMap[OpenableColumns.DISPLAY_NAME] = file.name
            } else if (column == OpenableColumns.SIZE) {
                columnValueMap[OpenableColumns.SIZE] = file.length()
            }
        }
        val cursor = MatrixCursor(columnValueMap.keys.toTypedArray(), 1)
        cursor.addRow(columnValueMap.values)
        return cursor
    }

    /**
     * @param uri the file uri
     * @return file represented by uri, or null if it can't be resolved
     */
    private fun getFileForUri(uri: Uri): File? {
        if (ContentResolver.SCHEME_CONTENT != uri.scheme || authority != uri.authority) {
            return null
        }
        val segments = uri.pathSegments.toMutableList()
        if (segments.size < 2) return null
        val dir: String = segments.removeAt(0).toUpperCase(Locale.ROOT)
        return try {
            val directory = Directory.valueOf(dir)
            directory.getFile(context!!, TextUtils.join(File.separator, segments))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Provides file mimeType
     *
     * @param uri the file uri
     * @return mimeType, default is [.MIME_TYPE_OCTET_STREAM]
     * @see .guessMimeType
     */
    override fun getType(uri: Uri): String = guessMimeType(uri)

    /**
     * @param uri    ignored
     * @param values ignored
     * @throws UnsupportedOperationException always
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException("No insert supported")

    /**
     * @param uri           ignored
     * @param selection     ignored
     * @param selectionArgs ignored
     * @throws UnsupportedOperationException always
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = throw UnsupportedOperationException("No delete supported")

    /**
     * @param uri           ignored
     * @param values        ignored
     * @param selection     ignored
     * @param selectionArgs ignored
     * @throws UnsupportedOperationException always
     */
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = throw UnsupportedOperationException("No update supported")

    /**
     * Open a file for read
     *
     * @param uri  the file uri
     * @param mode ignored
     * @return a [ParcelFileDescriptor] for the File
     * @throws FileNotFoundException if the file cannot be resolved
     */
    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val file = getFileForUri(uri)?.takeIf { it.exists() } ?: throw FileNotFoundException("File represented by uri $uri could not be found")
        debug {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                "$callingPackage opened ${file.path}"
            } else {
                "${file.path} was opened by an application"
            }
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    companion object {
        private val COLUMNS = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        private const val MIME_TYPE_OCTET_STREAM = "application/octet-stream"

        /**
         * @param context a a context
         * @return authority of this provider
         */
        private fun getAuthority(context: Context): String {
            return context.packageName + ".acra"
        }

        /**
         * Get an uri for this content provider for the given file
         *
         * @param context a context
         * @param file    the file
         * @return the uri
         */
        fun getUriForFile(context: Context, file: File): Uri {
            return getUriForFile(context, Directory.ROOT, file.path)
        }

        /**
         * Get an uri for this content provider for the given file
         *
         * @param context      a context
         * @param directory    the directory, to with the path is relative
         * @param relativePath the file path
         * @return the uri
         */
        fun getUriForFile(context: Context, directory: Directory, relativePath: String): Uri {
            val builder = Uri.Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(getAuthority(context))
                    .appendPath(directory.name.toLowerCase(Locale.ROOT))
            for (segment in relativePath.split(Pattern.quote(File.separator)).toTypedArray()) {
                if (segment.isNotEmpty()) {
                    builder.appendPath(segment)
                }
            }
            return builder.build()
        }

        /**
         * Tries to guess the mime type from uri extension
         *
         * @param uri the uri
         * @return the mime type of the uri, with fallback [.MIME_TYPE_OCTET_STREAM]
         */
        @JvmStatic
        fun guessMimeType(uri: Uri): String {
            var type: String? = null
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString())
            if (fileExtension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase(Locale.ROOT))
                if (type == null && "json" == fileExtension) {
                    type = "application/json"
                }
            }
            return type ?: MIME_TYPE_OCTET_STREAM
        }
    }
}