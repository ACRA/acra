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
package org.acra.http

import android.content.Context
import android.net.Uri
import org.acra.ACRAConstants
import org.acra.config.CoreConfiguration
import org.acra.log.warn
import org.acra.sender.HttpSender
import org.acra.util.UriUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter

/**
 * Produces [RFC 7578](https://tools.ietf.org/html/rfc7578) compliant requests
 *
 * @author F43nd1r
 * @since 11.03.2017
 */
class MultipartHttpRequest(config: CoreConfiguration, private val context: Context, private val contentType: String, login: String?, password: String?,
                           connectionTimeOut: Int, socketTimeOut: Int, headers: Map<String, String>?) :
        BaseHttpRequest<Pair<String, List<Uri>>>(config, context, HttpSender.Method.POST, login, password, connectionTimeOut, socketTimeOut, headers) {
    override fun getContentType(context: Context, t: Pair<String, List<Uri>>): String = "multipart/form-data; boundary=$BOUNDARY"

    @Throws(IOException::class)
    override fun write(outputStream: OutputStream, content: Pair<String, List<Uri>>) {
        val writer = PrintWriter(OutputStreamWriter(outputStream, ACRAConstants.UTF8))
        writer.append(SECTION_START)
                .format(CONTENT_DISPOSITION, "ACRA_REPORT", "")
                .format(CONTENT_TYPE, contentType)
                .append(NEW_LINE)
                .append(content.first)
        for (uri in content.second) {
            try {
                val name = UriUtils.getFileNameFromUri(context, uri)
                writer.append(SECTION_START)
                        .format(CONTENT_DISPOSITION, "ACRA_ATTACHMENT", name)
                        .format(CONTENT_TYPE, UriUtils.getMimeType(context, uri))
                        .append(NEW_LINE)
                        .flush()
                UriUtils.copyFromUri(context, outputStream, uri)
            } catch (e: FileNotFoundException) {
                warn(e) { "Not sending attachment" }
            }
        }
        writer.append(MESSAGE_END).flush()
    }

    companion object {
        private const val BOUNDARY = "%&ACRA_REPORT_DIVIDER&%"
        private const val BOUNDARY_FIX = "--"
        private const val NEW_LINE = "\r\n"
        private const val SECTION_START = NEW_LINE + BOUNDARY_FIX + BOUNDARY + NEW_LINE
        private const val MESSAGE_END = NEW_LINE + BOUNDARY_FIX + BOUNDARY + BOUNDARY_FIX + NEW_LINE
        private const val CONTENT_DISPOSITION = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"$NEW_LINE"
        private const val CONTENT_TYPE = "Content-Type: %s$NEW_LINE"
    }
}