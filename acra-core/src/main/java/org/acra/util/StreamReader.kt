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
package org.acra.util

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.acra.ACRAConstants
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import kotlin.math.min

/**
 * @author F43nd1r
 * @since 30.11.2017
 */
class StreamReader(private val inputStream: InputStream, var limit: Int = NO_LIMIT, var timeout: Int = INDEFINITE, var filter: ((String) -> Boolean)? = null) {

    constructor(filename: String) : this(File(filename))
    constructor(file: File) : this(FileInputStream(file))

    fun setLimit(limit: Int): StreamReader {
        this.limit = limit
        return this
    }

    fun setTimeout(timeout: Int): StreamReader {
        this.timeout = timeout
        return this
    }

    fun setFilter(filter: ((String) -> Boolean)?): StreamReader {
        this.filter = filter
        return this
    }

    @Throws(IOException::class)
    fun read(): String {
        val text = if (timeout == INDEFINITE) readFully() else readWithTimeout()
        return filter?.let {
            text.split("\\r?\\n").filter(it).run { if (limit == NO_LIMIT) this else takeLast(limit) }.joinToString("\n")
        } ?: if (limit == NO_LIMIT) {
            text
        } else {
            text.split("\\r?\\n").takeLast(limit).joinToString("\n")
        }
    }

    @Throws(IOException::class)
    private fun readFully(): String = inputStream.bufferedReader().use(BufferedReader::readText)

    @Throws(IOException::class)
    private fun readWithTimeout(): String {
        val until = System.currentTimeMillis() + timeout
        return inputStream.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES)
            var count: Int
            while (input.readUntil(buffer, until).also { count = it } != -1) {
                output.write(buffer, 0, count)
            }
            output.toString()
        }
    }

    @Throws(IOException::class)
    private fun InputStream.readUntil(buffer: ByteArray, until: Long): Int {
        var bufferOffset = 0
        while (System.currentTimeMillis() < until && bufferOffset < buffer.size) {
            val readResult = read(buffer, bufferOffset, min(inputStream.available(), buffer.size - bufferOffset))
            if (readResult == -1) break
            bufferOffset += readResult
        }
        return bufferOffset
    }

    companion object {
        private const val NO_LIMIT = -1
        private const val INDEFINITE = -1
    }
}