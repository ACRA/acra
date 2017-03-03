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

import org.acra.config.ACRAConfiguration;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Default SSLSetup.
 *
 * @author Karol Szklarski
 */
public class DefaultSSLSetup implements SSLSetup {

    private final HttpsURLConnection httpsUrlConnection;
    private final Context context;
    private final ACRAConfiguration config;

    public DefaultSSLSetup(@NonNull HttpsURLConnection httpsUrlConnection, @NonNull Context context, @NonNull ACRAConfiguration config) {
        this.httpsUrlConnection = httpsUrlConnection;
        this.context = context;
        this.config = config;
    }

    @Override
    public void setup() throws GeneralSecurityException {
        final String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        final KeyStore keyStore = KeyStoreHelper.getKeyStore(context, config);

        tmf.init(keyStore);

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        httpsUrlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
    }
}
