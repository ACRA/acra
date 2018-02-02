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
package org.acra.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.BuildConfig;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.HttpSenderConfiguration;
import org.acra.security.KeyStoreHelper;
import org.acra.sender.HttpSender.Method;
import org.acra.util.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 03.03.2017
 */
@SuppressWarnings("WeakerAccess")
public abstract class BaseHttpRequest<T> implements HttpRequest<T> {

    private final CoreConfiguration config;
    private final Context context;
    private final Method method;
    private final String login;
    private final String password;
    private final int connectionTimeOut;
    private final int socketTimeOut;
    private final Map<String, String> headers;

    public BaseHttpRequest(@NonNull CoreConfiguration config, @NonNull Context context, @NonNull Method method,
                           @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers) {
        this.config = config;
        this.context = context;
        this.method = method;
        this.login = login;
        this.password = password;
        this.connectionTimeOut = connectionTimeOut;
        this.socketTimeOut = socketTimeOut;
        this.headers = headers;
    }


    /**
     * Sends to a URL.
     *
     * @param url     URL to which to send.
     * @param content content to send.
     * @throws IOException if the data cannot be sent.
     */
    @Override
    public void send(@NonNull URL url, @NonNull T content) throws IOException {

        final HttpURLConnection urlConnection = createConnection(url);
        if (urlConnection instanceof HttpsURLConnection) {
            try {
                configureHttps((HttpsURLConnection) urlConnection);
            } catch (GeneralSecurityException e) {
                ACRA.log.e(LOG_TAG, "Could not configure SSL for ACRA request to " + url, e);
            }
        }
        configureTimeouts(urlConnection, connectionTimeOut, socketTimeOut);
        configureHeaders(urlConnection, login, password, headers, content);
        if (ACRA.DEV_LOGGING) {
            ACRA.log.d(LOG_TAG, "Sending request to " + url);
            ACRA.log.d(LOG_TAG, "Http " + method.name() + " content : ");
            ACRA.log.d(LOG_TAG, content.toString());
        }
        try {
            writeContent(urlConnection, method, content);
            handleResponse(urlConnection.getResponseCode(), urlConnection.getResponseMessage());
            urlConnection.disconnect();
        } catch (SocketTimeoutException e) {
            if (ConfigUtils.getPluginConfiguration(config, HttpSenderConfiguration.class).dropReportsOnTimeout()) {
                Log.w(ACRA.LOG_TAG, "Dropped report due to timeout");
            } else {
                throw e;
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    protected HttpURLConnection createConnection(@NonNull URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    @SuppressWarnings("WeakerAccess")
    protected void configureHttps(@NonNull HttpsURLConnection connection) throws GeneralSecurityException {
        // Configure SSL
        final String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        final KeyStore keyStore = KeyStoreHelper.getKeyStore(context, config);

        tmf.init(keyStore);

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        connection.setSSLSocketFactory(sslContext.getSocketFactory());
    }

    @SuppressWarnings("WeakerAccess")
    protected void configureTimeouts(@NonNull HttpURLConnection connection, int connectionTimeOut, int socketTimeOut) {
        connection.setConnectTimeout(connectionTimeOut);
        connection.setReadTimeout(socketTimeOut);
    }

    @SuppressWarnings("WeakerAccess")
    protected void configureHeaders(@NonNull HttpURLConnection connection, @Nullable String login, @Nullable String password,
                                    @Nullable Map<String, String> customHeaders, @NonNull T t) throws IOException {
        // Set Headers
        connection.setRequestProperty("User-Agent", String.format("Android ACRA %1$s", BuildConfig.VERSION_NAME)); //sent ACRA version to server
        connection.setRequestProperty("Accept",
                "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        connection.setRequestProperty("Content-Type", getContentType(context, t));

        // Set Credentials
        if (login != null && password != null) {
            final String credentials = login + ':' + password;
            final String encoded = new String(Base64.encode(credentials.getBytes(ACRAConstants.UTF8), Base64.NO_WRAP), ACRAConstants.UTF8);
            connection.setRequestProperty("Authorization", "Basic " + encoded);
        }

        if (customHeaders != null) {
            for (final Map.Entry<String, String> header : customHeaders.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
    }

    @NonNull
    protected abstract String getContentType(@NonNull Context context, @NonNull T t);

    @SuppressWarnings("WeakerAccess")
    protected void writeContent(@NonNull HttpURLConnection connection, @NonNull Method method, @NonNull T content) throws IOException {
        final byte[] contentAsBytes = asBytes(content);
        // write output - see http://developer.android.com/reference/java/net/HttpURLConnection.html
        connection.setRequestMethod(method.name());
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(contentAsBytes.length);

        // Disable ConnectionPooling because otherwise OkHttp ConnectionPool will try to start a Thread on #connect
        System.setProperty("http.keepAlive", "false");

        connection.connect();

        final OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        try {
            outputStream.write(contentAsBytes);
            outputStream.flush();
        } finally {
            IOUtils.safeClose(outputStream);
        }
    }

    @NonNull
    protected abstract byte[] asBytes(@NonNull T content) throws IOException;

    @SuppressWarnings("WeakerAccess")
    protected void handleResponse(int responseCode, String responseMessage) throws IOException {
        if (ACRA.DEV_LOGGING)
            ACRA.log.d(LOG_TAG, "Request response : " + responseCode + " : " + responseMessage);
        if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
            // All is good
            ACRA.log.i(LOG_TAG, "Request received by server");
        } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT || responseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            //timeout or server error. Repeat the request later.
            ACRA.log.w(LOG_TAG, "Could not send ACRA Post responseCode=" + responseCode + " message=" + responseMessage);
            throw new IOException("Host returned error code " + responseCode);
        } else if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            // Client error. The request must not be repeated. Discard it.
            ACRA.log.w(LOG_TAG, responseCode + ": Client error - request will be discarded");
        } else {
            ACRA.log.w(LOG_TAG, "Could not send ACRA Post - request will be discarded. responseCode=" + responseCode + " message=" + responseMessage);
        }
    }
}
