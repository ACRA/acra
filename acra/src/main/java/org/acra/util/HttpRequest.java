/*
 * This class was copied from this Stackoverflow Q&A:
 * http://stackoverflow.com/questions/2253061/secure-http-post-in-android/2253280#2253280
 * Thanks go to MattC!  
 */
package org.acra.util;

import java.io.IOException;

import org.acra.ACRA;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpRequest {

    private final DefaultHttpClient httpClient;
    private final HttpContext localContext;

    private UsernamePasswordCredentials creds;

    public HttpRequest(String login, String password) {
        if (login != null || password != null) {
            creds = new UsernamePasswordCredentials(login, password);
        }

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, ACRA.getConfig().socketTimeout());
        HttpConnectionParams.setSoTimeout(httpParams, ACRA.getConfig().socketTimeout());
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", new PlainSocketFactory(), 80));
        registry.register(new Scheme("https", (new FakeSocketFactory()), 443));

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, registry), httpParams);
        localContext = new BasicHttpContext();
    }

    /**
     * Posts to a URL.
     *
     * @param url   URL to which to post.
     * @param data  String containing the data to post to the URL.
     * @throws IOException if the data cannot be posted.
     */
    public void sendPost(String url, String data) throws IOException {

        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);

        final HttpPost httpPost = new HttpPost(url);

        Log.d(ACRA.LOG_TAG, "Setting httpPost headers");
        if (creds != null) {
            httpPost.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));
        }
        httpPost.setHeader("User-Agent", "Android");
        httpPost.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        final StringEntity tmp = new StringEntity(data, "UTF-8");
        httpPost.setEntity(tmp);

        Log.d(ACRA.LOG_TAG, "Sending request to " + url);

        // TODO Consider using a RequestRetryHandler and if its a SocketTimeoutException to up the SocketTimeout and try again.
        // See http://stackoverflow.com/questions/693997/how-to-set-httpresponse-timeout-for-android-in-java
        // I think SocketTimeOut while waiting for response may be the cause of the multiple crash reports () - I
        final HttpResponse response = httpClient.execute(httpPost, localContext);
        if (response != null) {
            final StatusLine statusLine = response.getStatusLine();
            if (statusLine != null) {
                final String statusCode = Integer.toString(response.getStatusLine().getStatusCode());
                if (statusCode.startsWith("4") || statusCode.startsWith("5")) {
                    throw new IOException("Host returned error code " + statusCode);
                }
            }
            final String ret = EntityUtils.toString(response.getEntity());
            if (ACRA.DEV_LOGGING) Log.d(ACRA.LOG_TAG,
                    "HTTP " + (statusLine != null ? statusLine.getStatusCode() : "NoStatusLine#noCode") + " - Returning value:"
                            + ret.substring(0, Math.min(ret.length(), 200)));
        } else {
            if (ACRA.DEV_LOGGING) Log.d(ACRA.LOG_TAG, "HTTP no Response!!");
        }
    }
}