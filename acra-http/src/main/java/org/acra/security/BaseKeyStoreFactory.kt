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
package org.acra.security

import android.content.Context
import org.acra.ACRAConstants
import org.acra.log.error
import org.acra.util.IOUtils.safeClose
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory

/**
 * Provides base KeyStoreFactory implementation
 *
 * creates a new KeyStoreFactory with the specified certificate type
 *
 * @param certificateType the certificate type
 *
 * @author F43nd1r
 * @since 4.8.3
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseKeyStoreFactory
/**
 * creates a new KeyStoreFactory for the default certificate type [ACRAConstants.DEFAULT_CERTIFICATE_TYPE]
 */ @JvmOverloads constructor(private val certificateType: String = ACRAConstants.DEFAULT_CERTIFICATE_TYPE) : KeyStoreFactory {
    enum class Type {
        CERTIFICATE, KEYSTORE
    }

    protected abstract fun getInputStream(context: Context): InputStream?
    protected val keyStoreType: String
        get() = KeyStore.getDefaultType()
    protected val streamType: Type
        get() = Type.CERTIFICATE
    protected val password: CharArray?
        get() = null

    override fun create(context: Context): KeyStore? {
        return getInputStream(context)?.let { BufferedInputStream(it) }?.use {
            try {
                val keyStore = KeyStore.getInstance(keyStoreType)
                when (streamType) {
                    Type.CERTIFICATE -> {
                        val certificateFactory = CertificateFactory.getInstance(certificateType)
                        val certificate = certificateFactory.generateCertificate(it)
                        keyStore.load(null, null)
                        keyStore.setCertificateEntry("ca", certificate)
                    }
                    Type.KEYSTORE -> keyStore.load(it, password)
                }
                return@use keyStore
            } catch (e: CertificateException) {
                error(e) { "Could not load certificate" }
            } catch (e: KeyStoreException) {
                error(e) { "Could not load keystore" }
            } catch (e: NoSuchAlgorithmException) {
                error(e) { "Could not load keystore" }
            } catch (e: IOException) {
                error(e) { "Could not load keystore" }
            }
            return@use null
        }
    }
}