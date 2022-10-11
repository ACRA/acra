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
package org.acra.sender

import android.content.Context
import android.net.Uri
import org.acra.ACRA
import org.acra.ReportField
import org.acra.attachment.DefaultAttachmentProvider
import org.acra.config.CoreConfiguration
import org.acra.config.HttpSenderConfiguration
import org.acra.config.getPluginConfiguration
import org.acra.data.CrashReportData
import org.acra.data.StringFormat
import org.acra.http.BinaryHttpRequest
import org.acra.http.DefaultHttpRequest
import org.acra.http.MultipartHttpRequest
import org.acra.log.debug
import org.acra.sender.HttpSender.Method
import org.acra.util.InstanceCreator
import org.acra.util.UriUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

/**
 * The [ReportSender] used by ACRA for http sending
 *
 * Create a new HttpPostSender instance with a fixed destination provided as
 * a parameter. Configuration changes to the formUri are not applied.
 *
 *
 * @param config  AcraConfig declaring the
 * @param method  HTTP [Method] to be used to send data. Currently only [Method.POST] and [Method.PUT] are available.
 * If [Method.PUT] is used, the [ReportField.REPORT_ID] is appended to the formUri to be compliant with RESTful APIs.
 * @param type    [StringFormat] of encoding used to send the report body.
 * [StringFormat.KEY_VALUE_LIST] is a simple Key/Value pairs list as defined by the application/x-www-form-urlencoded mime type.
 * @param formUri The URL of your server-side crash report collection script.
 */
/**
 *
 *
 * Create a new HttpSender instance with its destination taken from the supplied config.
 *
 *
 * @param config AcraConfig declaring the
 * @param method HTTP [Method] to be used to send data. Currently only [Method.POST] and [Method.PUT] are available.
 * If [Method.PUT] is used, the [ReportField.REPORT_ID] is appended to the formUri to be compliant with RESTful APIs.
 * @param type   [StringFormat] of encoding used to send the report body.
 * [StringFormat.KEY_VALUE_LIST] is a simple Key/Value pairs list as defined by the application/x-www-form-urlencoded mime type.
 * @author F43nd1r &amp; Various
 */
@Suppress("unused")
class HttpSender @JvmOverloads constructor(private val config: CoreConfiguration, method: Method?, type: StringFormat?, formUri: String? = null) :
    ReportSender {
    private val httpConfig: HttpSenderConfiguration = config.getPluginConfiguration()
    private val mFormUri: Uri = Uri.parse(formUri ?: httpConfig.uri)
    private val mMethod: Method = method ?: httpConfig.httpMethod
    private val mType: StringFormat = type ?: config.reportFormat
    private var mUsername: String? = null
    private var mPassword: String? = null

    /**
     *
     *
     * Set credentials for this HttpSender that override (if present) the ones set globally.
     *
     *
     * @param username The username to set for HTTP Basic Auth.
     * @param password The password to set for HTTP Basic Auth.
     */
    fun setBasicAuth(username: String?, password: String?) {
        mUsername = username
        mPassword = password
    }

    @Throws(ReportSenderException::class)
    override fun send(context: Context, errorContent: CrashReportData) {
        try {
            val baseUrl = mFormUri.toString()
            debug { "Connect to $baseUrl" }
            val login: String? = when {
                mUsername != null -> mUsername
                !httpConfig.basicAuthLogin.isNullOrEmpty() -> httpConfig.basicAuthLogin
                else -> null
            }
            val password: String? = when {
                mPassword != null -> mPassword
                !httpConfig.basicAuthPassword.isNullOrEmpty() -> httpConfig.basicAuthPassword
                else -> null
            }
            val uris = InstanceCreator.create(config.attachmentUriProvider) { DefaultAttachmentProvider() }.getAttachments(context, config)

            // Generate report body depending on requested type
            val reportAsString = convertToString(errorContent, mType)

            // Adjust URL depending on method
            val reportUrl = mMethod.createURL(baseUrl, errorContent)
            sendHttpRequests(
                config, context, mMethod, mType.matchingHttpContentType, login, password, httpConfig.connectionTimeout,
                httpConfig.socketTimeout, httpConfig.httpHeaders, reportAsString, reportUrl, uris
            )
        } catch (e: Exception) {
            throw ReportSenderException("Error while sending " + config.reportFormat.toString() + " report via Http " + mMethod.name, e)
        }
    }

    @Throws(IOException::class)
    protected fun sendHttpRequests(
        configuration: CoreConfiguration, context: Context, method: Method, contentType: String,
        login: String?, password: String?, connectionTimeOut: Int, socketTimeOut: Int, headers: Map<String, String>?,
        content: String, url: URL, attachments: List<Uri>
    ) {
        when (method) {
            Method.POST -> if (attachments.isEmpty()) {
                sendWithoutAttachments(configuration, context, method, contentType, login, password, connectionTimeOut, socketTimeOut, headers, content, url)
            } else {
                postMultipart(configuration, context, contentType, login, password, connectionTimeOut, socketTimeOut, headers, content, url, attachments)
            }

            Method.PUT -> {
                sendWithoutAttachments(configuration, context, method, contentType, login, password, connectionTimeOut, socketTimeOut, headers, content, url)
                for (uri in attachments) {
                    putAttachment(configuration, context, login, password, connectionTimeOut, socketTimeOut, headers, url, uri)
                }
            }
        }
    }

    @Throws(IOException::class)
    protected fun sendWithoutAttachments(
        configuration: CoreConfiguration, context: Context, method: Method, contentType: String,
        login: String?, password: String?, connectionTimeOut: Int, socketTimeOut: Int, headers: Map<String, String>?,
        content: String, url: URL
    ) {
        DefaultHttpRequest(configuration, context, method, contentType, login, password, connectionTimeOut, socketTimeOut, headers).send(url, content)
    }

    @Throws(IOException::class)
    protected fun postMultipart(
        configuration: CoreConfiguration, context: Context, contentType: String,
        login: String?, password: String?, connectionTimeOut: Int, socketTimeOut: Int, headers: Map<String, String>?,
        content: String, url: URL, attachments: List<Uri>
    ) {
        MultipartHttpRequest(configuration, context, contentType, login, password, connectionTimeOut, socketTimeOut, headers).send(url, content to attachments)
    }

    @Throws(IOException::class)
    protected fun putAttachment(
        configuration: CoreConfiguration, context: Context,
        login: String?, password: String?, connectionTimeOut: Int, socketTimeOut: Int, headers: Map<String, String>?,
        url: URL, attachment: Uri
    ) {
        try {
            val attachmentUrl = URL(url.toString() + "-" + UriUtils.getFileNameFromUri(context, attachment))
            BinaryHttpRequest(configuration, context, login, password, connectionTimeOut, socketTimeOut, headers).send(attachmentUrl, attachment)
        } catch (e: FileNotFoundException) {
            ACRA.log.w("Not sending attachment", e)
        }
    }

    /**
     * Convert a report to string
     *
     * @param report the report to convert
     * @param format the format to convert to
     * @return a string representation of the report
     * @throws Exception if conversion failed
     */
    @Throws(Exception::class)
    protected fun convertToString(report: CrashReportData?, format: StringFormat): String {
        return format.toFormattedString(report!!, config.reportContent, "&", "\n", true)
    }

    /**
     * Available HTTP methods to send data. Only POST and PUT are currently
     * supported.
     */
    enum class Method {
        POST {
            @Throws(MalformedURLException::class)
            override fun createURL(baseUrl: String, report: CrashReportData): URL = URL(baseUrl)
        },
        PUT {
            @Throws(MalformedURLException::class)
            override fun createURL(baseUrl: String, report: CrashReportData): URL = URL(baseUrl + '/' + report.getString(ReportField.REPORT_ID))
        };

        @Throws(MalformedURLException::class)
        abstract fun createURL(baseUrl: String, report: CrashReportData): URL
    }
}