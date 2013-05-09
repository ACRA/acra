/*
 * This class was copied from this Stackoverflow Q&A:
 * http://stackoverflow.com/questions/2253061/secure-http-post-in-android/2253280#2253280
 * Thanks go to MattC!  
 */
package org.acra.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.acra.ACRA;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
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

public final class HttpRequest {

    private static class SocketTimeOutRetryHandler implements HttpRequestRetryHandler {

        private final HttpParams httpParams;
        private final int maxNrRetries;

        /**
         * @param httpParams
         *            HttpParams that will be used in the HttpRequest.
         * @param maxNrRetries
         *            Max number of times to retry Request on failure due to
         *            SocketTimeOutException.
         */
        private SocketTimeOutRetryHandler(HttpParams httpParams, int maxNrRetries) {
            this.httpParams = httpParams;
            this.maxNrRetries = maxNrRetries;
        }

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (exception instanceof SocketTimeoutException) {
                if (executionCount <= maxNrRetries) {

                    if (httpParams != null) {
                        final int newSocketTimeOut = HttpConnectionParams.getSoTimeout(httpParams) * 2;
                        HttpConnectionParams.setSoTimeout(httpParams, newSocketTimeOut);
                        ACRA.log.d(ACRA.LOG_TAG, "SocketTimeOut - increasing time out to " + newSocketTimeOut
                                + " millis and trying again");
                    } else {
                        ACRA.log.d(ACRA.LOG_TAG,
                                "SocketTimeOut - no HttpParams, cannot increase time out. Trying again with current settings");
                    }

                    return true;
                }

                ACRA.log.d(ACRA.LOG_TAG, "SocketTimeOut but exceeded max number of retries : " + maxNrRetries);
            }

            return false; // To change body of implemented methods use File |
                          // Settings | File Templates.
        }
    }

    private String login;
    private String password;
    private int connectionTimeOut = 3000;
    private int socketTimeOut = 3000;
    private int maxNrRetries = 3;
    private Map<String,String> headers;
    
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

    public void setHeaders(Map<String,String> headers) {
       this.headers = headers;
    }

    
    /**
     * The default number of retries is 3.
     * 
     * @param maxNrRetries
     *            Max number of times to retry Request on failure due to
     *            SocketTimeOutException.
     */
    public void setMaxNrRetries(int maxNrRetries) {
        this.maxNrRetries = maxNrRetries;
    }

    /**
     * Posts to a URL.
     * 
     * @param url
     *            URL to which to post.
     * @param content
     *            Map of parameters to post to a URL.
     * @throws IOException
     *             if the data cannot be posted.
     */
    public void send(URL url, Method method, String content, Type type) throws IOException {

        final HttpClient httpClient = getHttpClient();
        final HttpEntityEnclosingRequestBase httpRequest = getHttpRequest(url, method, content, type);

        ACRA.log.d(ACRA.LOG_TAG, "Sending request to " + url);
        if (ACRA.DEV_LOGGING)
            ACRA.log.d(ACRA.LOG_TAG, "Http " + method.name() + " content : ");
        if (ACRA.DEV_LOGGING)
            ACRA.log.d(ACRA.LOG_TAG, content);

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpRequest, new BasicHttpContext());
            if (response != null) {
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine != null) {
                    final String statusCode = Integer.toString(response.getStatusLine().getStatusCode());
    
                    if (!statusCode.equals("409") // 409 return code means that the
                                                  // report has been received
                                                  // already. So we can discard it.
                            && !statusCode.equals("403") // a 403 error code is an explicit data validation refusal
                                                         // from the server. The request must not be repeated.
                                                         // Discard it.
                            && (statusCode.startsWith("4") || statusCode.startsWith("5"))) {
                        if (ACRA.DEV_LOGGING) {
                            ACRA.log.d(ACRA.LOG_TAG, "Could not send HttpPost : " + httpRequest);
                            ACRA.log.d(ACRA.LOG_TAG, "HttpResponse Status : "
                                    + (statusLine != null ? statusLine.getStatusCode() : "NoStatusLine#noCode"));
                            final String respContent = EntityUtils.toString(response.getEntity());
                            ACRA.log.d(ACRA.LOG_TAG,
                                    "HttpResponse Content : " + respContent.substring(0, Math.min(respContent.length(), 200)));
                        }
                        throw new IOException("Host returned error code " + statusCode);
                    }
                }

                if (ACRA.DEV_LOGGING)
                    ACRA.log.d(ACRA.LOG_TAG, "HttpResponse Status : "
                            + (statusLine != null ? statusLine.getStatusCode() : "NoStatusLine#noCode"));
                final String respContent = EntityUtils.toString(response.getEntity());
                if (ACRA.DEV_LOGGING)
                    ACRA.log.d(ACRA.LOG_TAG,
                            "HttpResponse Content : " + respContent.substring(0, Math.min(respContent.length(), 200)));

            } else {
                if (ACRA.DEV_LOGGING)
                    ACRA.log.d(ACRA.LOG_TAG, "HTTP no Response!!");
            }
        } finally {
            if (response != null) {
				response.getEntity().consumeContent();
			}
        }
    }

    /**
     * @return HttpClient to use with this HttpRequest.
     */
    private HttpClient getHttpClient() {
        final HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
        HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeOut);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeOut);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", new PlainSocketFactory(), 80));
        if (ACRA.getConfig().disableSSLCertValidation()) {
            registry.register(new Scheme("https", (new FakeSocketFactory()), 443));
        } else {
            registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        }

        final ClientConnectionManager clientConnectionManager = new ThreadSafeClientConnManager(httpParams, registry);
        final DefaultHttpClient httpClient = new DefaultHttpClient(clientConnectionManager, httpParams);

        final HttpRequestRetryHandler retryHandler = new SocketTimeOutRetryHandler(httpParams, maxNrRetries);
        httpClient.setHttpRequestRetryHandler(retryHandler);

        return httpClient;
    }

    /**
     * @return Credentials to use with this HttpRequest or null if no
     *         credentials were supplied.
     */
    private UsernamePasswordCredentials getCredentials() {
        if (login != null || password != null) {
            return new UsernamePasswordCredentials(login, password);
        }

        return null;
    }

    private HttpEntityEnclosingRequestBase getHttpRequest(URL url, Method method, String content, Type type)
            throws UnsupportedEncodingException, UnsupportedOperationException {

        final HttpEntityEnclosingRequestBase httpRequest;
        switch (method) {
        case POST:
            httpRequest = new HttpPost(url.toString());
            break;
        case PUT:
            httpRequest = new HttpPut(url.toString());
            break;
        default:
            throw new UnsupportedOperationException("Unknown method: " + method.name());
        }

        final UsernamePasswordCredentials creds = getCredentials();
        if (creds != null) {
            httpRequest.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));
        }
        httpRequest.setHeader("User-Agent", "Android");
        httpRequest
                .setHeader("Accept",
                        "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        httpRequest.setHeader("Content-Type", type.getContentType());

        if(headers !=null) {
           Iterator<String> headerIt = headers.keySet().iterator();
           while(headerIt.hasNext()) {
              String header = headerIt.next();
              String value = headers.get(header);
              httpRequest.setHeader(header, value);
           }
        }
        
        httpRequest.setEntity(new StringEntity(content, "UTF-8"));

        return httpRequest;
    }

    /**
     * Converts a Map of parameters into a URL encoded Sting.
     * 
     * @param parameters
     *            Map of parameters to convert.
     * @return URL encoded String representing the parameters.
     * @throws UnsupportedEncodingException
     *             if one of the parameters couldn't be converted to UTF-8.
     */
    public static String getParamsAsFormString(Map<?, ?> parameters) throws UnsupportedEncodingException {

        final StringBuilder dataBfr = new StringBuilder();
        for (final Object key : parameters.keySet()) {
            if (dataBfr.length() != 0) {
                dataBfr.append('&');
            }
            final Object preliminaryValue = parameters.get(key);
            final Object value = (preliminaryValue == null) ? "" : preliminaryValue;
            dataBfr.append(URLEncoder.encode(key.toString(), "UTF-8"));
            dataBfr.append('=');
            dataBfr.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }

        return dataBfr.toString();
    }
}