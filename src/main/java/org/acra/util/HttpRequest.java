/*
 * This class was copied from this Stackoverflow Q&A:
 * http://stackoverflow.com/questions/2253061/secure-http-post-in-android/2253280#2253280
 * Thanks go to MattC!  
 */
package org.acra.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.security.KeyStoreHelper;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ch.acra.acra.BuildConfig;

import static org.acra.ACRA.LOG_TAG;

public final class HttpRequest {

    private static final String UTF8 = "UTF-8";

    private final ACRAConfiguration config;
    private String login;
    private String password;
    private int connectionTimeOut = 3000;
    private int socketTimeOut = 3000;
    private Map<String, String> headers;

    public HttpRequest(@NonNull ACRAConfiguration config) {
        this.config = config;
    }

    public void setLogin(@Nullable String login) {
        this.login = login;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public void setHeaders(@Nullable Map<String, String> headers) {
        this.headers = headers;
    }


    /**
     * Posts to a URL.
     *
     * @param url     URL to which to post.
     * @param content Map of parameters to post to a URL.
     * @throws IOException if the data cannot be posted.
     */
    public void send(@NonNull Context context, @NonNull URL url, @NonNull Method method, @NonNull String content, @NonNull Type type) throws IOException {

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        // Configure SSL
        if (urlConnection instanceof HttpsURLConnection) {
            try {
                final HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection;

                final String algorithm = TrustManagerFactory.getDefaultAlgorithm();
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                final KeyStore keyStore = KeyStoreHelper.getKeyStore(context, config);

                tmf.init(keyStore);

                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

                httpsUrlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            } catch (GeneralSecurityException e) {
                ACRA.log.e(LOG_TAG, "Could not configure SSL for ACRA request to " + url, e);
            }
        }

        // Set Credentials
        if (login != null && password != null) {
            final String credentials = login + ':' + password;
            final String encoded = new String(Base64.encode(credentials.getBytes(UTF8), Base64.NO_WRAP), UTF8);
            urlConnection.setRequestProperty("Authorization", "Basic " + encoded);
        }

        urlConnection.setConnectTimeout(connectionTimeOut);
        urlConnection.setReadTimeout(socketTimeOut);

        // Set Headers
        urlConnection.setRequestProperty("User-Agent", String.format("Android ACRA %1$s", BuildConfig.VERSION_NAME)); //sent ACRA version to server
        urlConnection.setRequestProperty("Accept",
                "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        urlConnection.setRequestProperty("Content-Type", type.getContentType());

        if (headers != null) {
            for (final Map.Entry<String, String> header : headers.entrySet()) {
                urlConnection.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        final byte[] contentAsBytes = content.getBytes(UTF8);

        // write output - see http://developer.android.com/reference/java/net/HttpURLConnection.html
        urlConnection.setRequestMethod(method.name());
        urlConnection.setDoOutput(true);
        urlConnection.setFixedLengthStreamingMode(contentAsBytes.length);

        // Disable ConnectionPooling because otherwise OkHttp ConnectionPool will try to start a Thread on #connect
        System.setProperty("http.keepAlive", "false");

        urlConnection.connect();

        final OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
        try {
            outputStream.write(contentAsBytes);
            outputStream.flush();
        } finally {
            IOUtils.safeClose(outputStream);
        }

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Sending request to " + url);
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Http " + method.name() + " content : ");
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, content);

        final int responseCode = urlConnection.getResponseCode();
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Request response : " + responseCode + " : " + urlConnection.getResponseMessage());
        if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
            // All is good
            ACRA.log.i(LOG_TAG, "Request received by server");
        } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT || responseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            //timeout or server error. Repeat the request later.
            ACRA.log.w(LOG_TAG, "Could not send ACRA Post responseCode=" + responseCode + " message=" + urlConnection.getResponseMessage());
            throw new IOException("Host returned error code " + responseCode);
        } else if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST && responseCode < HttpURLConnection.HTTP_INTERNAL_ERROR) {
            // Client error. The request must not be repeated. Discard it.
            ACRA.log.w(LOG_TAG, responseCode+": Client error - request will be discarded");
        } else {
            ACRA.log.w(LOG_TAG, "Could not send ACRA Post - request will be discarded. responseCode=" + responseCode + " message=" + urlConnection.getResponseMessage());
        }

        urlConnection.disconnect();
    }

    /**
     * Converts a Map of parameters into a URL encoded Sting.
     *
     * @param parameters Map of parameters to convert.
     * @return URL encoded String representing the parameters.
     * @throws UnsupportedEncodingException if one of the parameters couldn't be converted to UTF-8.
     */
    @NonNull
    public static String getParamsAsFormString(@NonNull Map<?, ?> parameters) throws UnsupportedEncodingException {

        final StringBuilder dataBfr = new StringBuilder();
        for (final Map.Entry<?, ?> entry : parameters.entrySet()) {
            if (dataBfr.length() != 0) {
                dataBfr.append('&');
            }
            final Object preliminaryValue = entry.getValue();
            final Object value = (preliminaryValue == null) ? "" : preliminaryValue;
            dataBfr.append(URLEncoder.encode(entry.getKey().toString(), UTF8));
            dataBfr.append('=');
            dataBfr.append(URLEncoder.encode(value.toString(), UTF8));
        }

        return dataBfr.toString();
    }
}
