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
package org.acra.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static org.acra.ACRA.LOG_TAG;

/**
 * Provides base KeyStoreFactory implementation
 *
 * @author F43nd1r
 * @since 4.8.3
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseKeyStoreFactory implements KeyStoreFactory {

    public enum Type {
        CERTIFICATE,
        KEYSTORE
    }

    private final String certificateType;

    /**
     * creates a new KeyStoreFactory for the default certificate type {@link ACRAConstants#DEFAULT_CERTIFICATE_TYPE}
     */
    public BaseKeyStoreFactory() {
        this(ACRAConstants.DEFAULT_CERTIFICATE_TYPE);
    }

    /**
     * creates a new KeyStoreFactory with the specified certificate type
     *
     * @param certificateType the certificate type
     */
    public BaseKeyStoreFactory(String certificateType) {
        this.certificateType = certificateType;
    }

    @Nullable
    protected abstract InputStream getInputStream(@NonNull Context context);

    protected String getKeyStoreType() {
        return KeyStore.getDefaultType();
    }

    @NonNull
    protected Type getStreamType() {
        return Type.CERTIFICATE;
    }

    @Nullable
    protected char[] getPassword() {
        return null;
    }

    @Override
    @Nullable
    public final KeyStore create(@NonNull Context context) {
        final InputStream inputStream = getInputStream(context);
        if (inputStream != null) {
            final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            try {
                final KeyStore keyStore = KeyStore.getInstance(getKeyStoreType());
                switch (getStreamType()) {
                    case CERTIFICATE:
                        final CertificateFactory certificateFactory = CertificateFactory.getInstance(certificateType);
                        final Certificate certificate = certificateFactory.generateCertificate(bufferedInputStream);
                        keyStore.load(null, null);
                        keyStore.setCertificateEntry("ca", certificate);
                        break;
                    case KEYSTORE:
                        keyStore.load(bufferedInputStream, getPassword());
                }
                return keyStore;
            } catch (CertificateException e) {
                ACRA.log.e(LOG_TAG, "Could not load certificate", e);
            } catch (KeyStoreException e) {
                ACRA.log.e(LOG_TAG, "Could not load keystore", e);
            } catch (NoSuchAlgorithmException e) {
                ACRA.log.e(LOG_TAG, "Could not load keystore", e);
            } catch (IOException e) {
                ACRA.log.e(LOG_TAG, "Could not load keystore", e);
            } finally {
                IOUtils.safeClose(bufferedInputStream);
            }
        }
        return null;
    }
}
