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
package org.acra.util

import android.util.Base64
import org.acra.ACRAConstants
import org.acra.log.warn
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.io.Serializable

/**
 * @author William Ferguson &amp; F43nd1r
 * @since 4.6.0
 */
object IOUtils {
    /**
     * Closes a Closeable.
     *
     * @param closeable Closeable to close. If closeable is null then method just returns.
     */
    @JvmStatic
    fun safeClose(closeable: Closeable?) {
        if (closeable == null) return
        try {
            closeable.close()
        } catch (ignored: IOException) {
            // We made out best effort to release this resource. Nothing more we can do.
        }
    }

    @JvmStatic
    fun deleteFile(file: File) {
        val deleted = file.delete()
        if (!deleted) {
            warn { "Could not delete file: $file" }
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun writeStringToFile(file: File, content: String) {
        val writer = OutputStreamWriter(FileOutputStream(file), ACRAConstants.UTF8)
        try {
            writer.write(content)
            writer.flush()
        } finally {
            safeClose(writer)
        }
    }

    fun serialize(serializable: Serializable): String? {
        val out = ByteArrayOutputStream()
        try {
            ObjectOutputStream(out).use { outputStream ->
                outputStream.writeObject(serializable)
                return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun <T : Serializable> deserialize(clazz: Class<T>, s: String?): T? {
        if (s != null) {
            try {
                ObjectInputStream(ByteArrayInputStream(Base64.decode(s, Base64.DEFAULT))).use { inputStream ->
                    val o = inputStream.readObject()
                    if (clazz.isInstance(o)) {
                        return clazz.cast(o)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        return null
    }
}