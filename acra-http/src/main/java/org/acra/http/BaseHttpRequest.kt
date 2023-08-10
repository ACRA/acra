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
import android.util.Base64
import android.util.Log
import org.acra.ACRA
import org.acra.ACRAConstants
import org.acra.BuildConfig
import org.acra.config.CoreConfiguration
import org.acra.config.HttpSenderConfiguration
import org.acra.config.getPluginConfiguration
import org.acra.log.debug
import org.acra.log.error
import org.acra.log.info
import org.acra.log.warn
import org.acra.security.KeyStoreHelper
import org.acra.security.ProtocolSocketFactoryWrapper
import org.acra.sender.HttpSender
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.security.GeneralSecurityException
import java.util.zip.GZIPOutputStream
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

/**
 * @author F43nd1r
 * @since 03.03.2017
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseHttpRequest<T>(private val config: CoreConfiguration, private val context: Context, private val method: HttpSender.Method,
                                  private val login: String?, private val password: String?, private val connectionTimeOut: Int, private val socketTimeOut: Int,
                                  private val headers: Map<String, String>?) : HttpRequest<T> {
    private val senderConfiguration: HttpSenderConfiguration = config.getPluginConfiguration()

    /**
     * Sends to a URL.
     *
     * @param url     URL to which to send.
     * @param content content to send.
     * @throws IOException if the data cannot be sent.
     */
    @Throws(IOException::class)
    override fun send(url: URL, content: T) {
        val urlConnection = createConnection(url)
        if (urlConnection is HttpsURLConnection) {
            try {
                configureHttps(urlConnection)
            } catch (e: GeneralSecurityException) {
                error(e) { "Could not configure SSL for ACRA request to $url" }
            }
        }
        configureTimeouts(urlConnection, connectionTimeOut, socketTimeOut)
        configureHeaders(urlConnection, login, password, headers, content)
        debug { "Sending request to $url" }
        debug { "Http ${method.name} content : " }
        debug { content.toString() }
        try {
            writeContent(urlConnection, method, content)
            handleResponse(urlConnection.responseCode, urlConnection.responseMessage)
            urlConnection.disconnect()
        } catch (e: SocketTimeoutException) {
            if (senderConfiguration.dropReportsOnTimeout) {
                Log.w(ACRA.LOG_TAG, "Dropped report due to timeout")
            } else {
                throw e
            }
        }
    }

    @Throws(IOException::class)
    protected fun createConnection(url: URL): HttpURLConnection {
        return url.openConnection() as HttpURLConnection
    }

    @Throws(GeneralSecurityException::class)
    protected fun configureHttps(connection: HttpsURLConnection) {
        // Configure SSL
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val keyStore = KeyStoreHelper.getKeyStore(context, config)
        tmf.init(keyStore)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)
        connection.sslSocketFactory = ProtocolSocketFactoryWrapper(sslContext.socketFactory, senderConfiguration.tlsProtocols)
    }

    protected fun configureTimeouts(connection: HttpURLConnection, connectionTimeOut: Int, socketTimeOut: Int) {
        connection.connectTimeout = connectionTimeOut
        connection.readTimeout = socketTimeOut
    }

    @Throws(IOException::class)
    protected fun configureHeaders(connection: HttpURLConnection, login: String?, password: String?, customHeaders: Map<String, String>?, t: T) {
        // Set Headers
        connection.setRequestProperty("User-Agent", String.format("Android ACRA %1\$s", BuildConfig.VERSION_NAME)) //sent ACRA version to server
        connection.setRequestProperty("Accept", "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5")
        connection.setRequestProperty("Content-Type", getContentType(context, t))

        // Set Credentials
        if (login != null && password != null) {
            val encoded = String(Base64.encode("$login:$password".toByteArray(Charsets.UTF_8), Base64.NO_WRAP), Charsets.UTF_8)
            connection.setRequestProperty("Authorization", "Basic $encoded")
        }
        if (senderConfiguration.compress) {
            connection.setRequestProperty("Content-Encoding", "gzip")
        }
        customHeaders?.forEach { (key, value) -> connection.setRequestProperty(key, value) }
    }

    protected abstract fun getContentType(context: Context, t: T): String

    @Throws(IOException::class)
    protected fun writeContent(connection: HttpURLConnection, method: HttpSender.Method, content: T) {
        // write output - see http://developer.android.com/reference/java/net/HttpURLConnection.html
        connection.requestMethod = method.name
        connection.doOutput = true
        connection.setChunkedStreamingMode(ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES)

        // Disable ConnectionPooling because otherwise OkHttp ConnectionPool will try to start a Thread on #connect
        System.setProperty("http.keepAlive", "false")
        connection.connect()
        (if (senderConfiguration.compress) GZIPOutputStream(connection.outputStream, ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES)
        else BufferedOutputStream(connection.outputStream)).use {
            write(it, content)
            it.flush()
        }
    }

    @Throws(IOException::class)
    protected abstract fun write(outputStream: OutputStream, content: T)

    @Throws(IOException::class)
    protected fun handleResponse(responseCode: Int, responseMessage: String) {
        debug {  "Request response : $responseCode : $responseMessage" }
        if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
            // All is good
            info {  "Request received by server" }
        } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT || responseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            //timeout or server error. Repeat the request later.
            warn {  "Could not send ACRA Post responseCode=$responseCode message=$responseMessage" }
            throw IOException("Host returned error code $responseCode")
        } else if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            // Client error. The request must not be repeated. Discard it.
            warn {  "$responseCode: Client error - request will be discarded" }
        } else {
            warn {  "Could not send ACRA Post - request will be discarded. responseCode=$responseCode message=$responseMessage" }
        }
    }

}