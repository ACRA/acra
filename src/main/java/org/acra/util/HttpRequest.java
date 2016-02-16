/*
 * This class was copied from this Stackoverflow Q&A:
 * http://stackoverflow.com/questions/2253061/secure-http-post-in-android/2253280#2253280
 * Thanks go to MattC!  
 */
package org.acra.util;

import android.support.annotation.NonNull;
import android.util.Base64;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
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
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static org.acra.ACRA.LOG_TAG;

public final class HttpRequest {

    private static final int PRIMARY_DIGIT_DIVIDER = 100;

    private static final int HTTP_PRIMARY_SUCCESS = 2;
    private static final int HTTP_PRIMARY_CLIENT_ERROR = 4;
    private static final int HTTP_PRIMARY_SERVER_ERROR = 5;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_CONFLICT = 409;

    private final ACRAConfiguration config;
    private String login;
    private String password;
    private int connectionTimeOut = 3000;
    private int socketTimeOut = 3000;
    private Map<String, String> headers;

    public HttpRequest(ACRAConfiguration config) {
        this.config = config;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }


    /**
     * Posts to a URL.
     *
     * @param url     URL to which to post.
     * @param content Map of parameters to post to a URL.
     * @throws IOException if the data cannot be posted.
     */
    public void send(@NonNull URL url, @NonNull Method method, @NonNull String content, @NonNull Type type) throws IOException {

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        // Configure SSL
        if (urlConnection instanceof HttpsURLConnection) {
            try {
                final HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection;

                final String algorithm = TrustManagerFactory.getDefaultAlgorithm();
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);

                tmf.init(config.keyStore());

                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

                httpsUrlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            } catch (GeneralSecurityException e) {
                ACRA.log.e(LOG_TAG, "Could not configure SSL for ACRA request to " + url, e);
            }
        }

        // Set Credentials
        if ((login != null) && (password != null)) {
            final String credentials = login + ":" + password;
            final String encoded = new String(Base64.encode(credentials.getBytes("UTF-8"), Base64.NO_WRAP), "UTF-8");
            urlConnection.setRequestProperty("Authorization", "Basic " + encoded);
        }

        urlConnection.setConnectTimeout(connectionTimeOut);
        urlConnection.setReadTimeout(socketTimeOut);

        // Set Headers
        urlConnection.setRequestProperty("User-Agent", "Android");
        urlConnection.setRequestProperty("Accept",
                "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        urlConnection.setRequestProperty("Content-Type", type.getContentType());

        if (headers != null) {
            for (final Map.Entry<String, String> header : headers.entrySet()) {
                urlConnection.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        final byte[] contentAsBytes = content.getBytes("UTF-8");

        // write output - see http://developer.android.com/reference/java/net/HttpURLConnection.html
        urlConnection.setRequestMethod(method.name());
        urlConnection.setDoOutput(true);
        urlConnection.setFixedLengthStreamingMode(contentAsBytes.length);

        // Disable ConnectionPooling because otherwise OkHttp ConnectionPool will try to start a Thread on #connect
        System.setProperty("http.keepAlive", "false");

        urlConnection.connect();

        final OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
        outputStream.write(contentAsBytes);
        outputStream.flush();
        outputStream.close();

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Sending request to " + url);
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Http " + method.name() + " content : ");
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, content);

        final int responseCode = urlConnection.getResponseCode();
        if (ACRA.DEV_LOGGING)
            ACRA.log.d(LOG_TAG, "Request response : " + responseCode + " : " + urlConnection.getResponseMessage());
        int primaryCode = responseCode / PRIMARY_DIGIT_DIVIDER;
        outer: switch (primaryCode) {
            case HTTP_PRIMARY_SUCCESS:
                // All is good
                ACRA.log.i(LOG_TAG, "Request received by server");
                break;
            case HTTP_PRIMARY_CLIENT_ERROR:
                switch (responseCode) {
                    case HTTP_FORBIDDEN:
                        // 403 is an explicit data validation refusal from the server. The request must not be repeated. Discard it.
                        ACRA.log.w(LOG_TAG, "Data validation error on server - request will be discarded");
                        break outer;
                    case HTTP_CONFLICT:
                        // 409 means that the report has been received already. So we can discard it.
                        ACRA.log.w(LOG_TAG, "Server has already received this post - request will be discarded");
                        break outer;
                }
                //if none of the special cases, continue to next block
            case HTTP_PRIMARY_SERVER_ERROR:
                ACRA.log.w(LOG_TAG, "Could not send ACRA Post responseCode=" + responseCode + " message=" + urlConnection.getResponseMessage());
                throw new IOException("Host returned error code " + responseCode);
            default:
                ACRA.log.w(LOG_TAG, "Could not send ACRA Post - request will be discarded responseCode=" + responseCode + " message=" + urlConnection.getResponseMessage());
                break;
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
    public static String getParamsAsFormString(@NonNull Map<?, ?> parameters) throws UnsupportedEncodingException {

        final StringBuilder dataBfr = new StringBuilder();
        for (final Map.Entry<?, ?> entry : parameters.entrySet()) {
            if (dataBfr.length() != 0) {
                dataBfr.append('&');
            }
            final Object preliminaryValue = entry.getValue();
            final Object value = (preliminaryValue == null) ? "" : preliminaryValue;
            dataBfr.append(URLEncoder.encode(entry.getKey().toString(), "UTF-8"));
            dataBfr.append('=');
            dataBfr.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }

        return dataBfr.toString();
    }
}
