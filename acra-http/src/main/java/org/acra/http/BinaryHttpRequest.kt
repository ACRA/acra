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
package org.acra.http

import android.content.Context
import android.net.Uri
import org.acra.config.CoreConfiguration
import org.acra.sender.HttpSender
import org.acra.util.UriUtils
import java.io.IOException
import java.io.OutputStream

/**
 * @author F43nd1r
 * @since 10.03.2017
 */
class BinaryHttpRequest(config: CoreConfiguration, private val context: Context,
                        login: String?, password: String?, connectionTimeOut: Int, socketTimeOut: Int, headers: Map<String, String>?) :
        BaseHttpRequest<Uri>(config, context, HttpSender.Method.PUT, login, password, connectionTimeOut, socketTimeOut, headers) {
    override fun getContentType(context: Context, uri: Uri): String {
        return UriUtils.getMimeType(context, uri)
    }

    @Throws(IOException::class)
    override fun write(outputStream: OutputStream, content: Uri) {
        UriUtils.copyFromUri(context, outputStream, content)
    }
}