/*
 * Copyright (c) 2021
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

package org.acra.config

import androidx.annotation.RawRes
import com.faendir.kotlin.autodsl.AutoDsl
import org.acra.annotation.AcraDsl
import org.acra.ktx.plus
import org.acra.security.KeyStoreFactory
import org.acra.security.NoKeyStoreFactory
import org.acra.security.TLS
import org.acra.sender.HttpSender

/**
 * Http sender configuration
 *
 * @author F43nd1r
 * @since 01.06.2017
 */
@AutoDsl(dslMarker = AcraDsl::class)
class HttpSenderConfiguration(
    /**
     * enables this plugin
     */
    val enabled: Boolean = true,
    /**
     * The Uri of your own server-side script that will receive reports.
     *
     * @since 5.0.0
     */
    val uri: String,

    /**
     * you can set here and in [basicAuthPassword] the credentials for a BASIC HTTP authentication.
     *
     * @since 5.0.0
     */
    val basicAuthLogin: String? = null,

    /**
     * you can set here and in [basicAuthLogin] the credentials for a BASIC HTTP authentication.
     *
     * @since 5.0.0
     */
    val basicAuthPassword: String? = null,

    /**
     * The [HttpSender.Method] to be used when posting with [uri].
     *
     * @since 5.0.0
     */
    val httpMethod: HttpSender.Method = HttpSender.Method.POST,

    /**
     * timeout for server connection in milliseconds
     *
     * @see java.net.HttpURLConnection.setConnectTimeout
     * @since 5.0.0
     */
    val connectionTimeout: Int = 5000,

    /**
     * timeout for socket connection in milliseconds
     *
     * @see java.net.HttpURLConnection.setReadTimeout
     * @since 5.0.0
     */
    val socketTimeout: Int = 20000,

    /**
     * allows to prevent resending of timed out reports, possibly relieving server stress, but also reducing received report counts
     *
     * @since 5.0.0
     */
    val dropReportsOnTimeout: Boolean = false,

    /**
     * A custom class supplying a [java.security.KeyStore], which will be used for ssl authentication.
     * A base implementation is available: [org.acra.security.BaseKeyStoreFactory]
     *
     * @since 5.0.0
     */
    val keyStoreFactoryClass: Class<out KeyStoreFactory> = NoKeyStoreFactory::class.java,

    /**
     * a certificate used for ssl authentication
     *
     * Must start with "asset://" if the file is in the assets folder
     *
     * @since 5.0.0
     */
    val certificatePath: String? = null,

    /**
     * a certificate used for ssl authentication
     *
     * @since 5.0.0
     */
    @RawRes
    val resCertificate: Int? = null,

    /**
     * type of the certificate used for ssl authentication, set in either [certificatePath] or [resCertificate]
     *
     * @since 5.0.0
     */
    val certificateType: String = "X.509",

    /**
     * if the server request should be compressed using gzip
     *
     * @since 5.2.0
     */
    val compress: Boolean = false,

    /**
     * if the request should be sent in chunks.
     * Set to true when using cronet.
     *
     * @since 5.11.3
     */
    val chunked: Boolean = false,

    /**
     * TLS versions supported by the server.
     *
     * This array has to contain at least one option supported on all android versions this runs on!
     * ACRA will automatically remove unsupported versions on older devices.
     *
     * Note: Older Android versions do not support all tls versions.
     *
     * @since 5.7.0
     * @see javax.net.ssl.SSLContext
     */
    val tlsProtocols: List<TLS> = listOf(TLS.V1_3, TLS.V1_2, TLS.V1_1, TLS.V1),

    /**
     * custom HTTP headers to be sent by the provided [org.acra.sender.HttpSender]
     * This should be used also by third party senders.
     */
    val httpHeaders: Map<String, String> = emptyMap(),
) : Configuration {
    override fun enabled(): Boolean = enabled
}

fun CoreConfigurationBuilder.httpSender(initializer: HttpSenderConfigurationBuilder.() -> Unit) {
    pluginConfigurations += HttpSenderConfigurationBuilder().apply(initializer).build()
}
