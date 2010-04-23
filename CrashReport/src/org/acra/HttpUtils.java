package org.acra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import android.util.Log;

public class HttpUtils {
    private static final String LOG_TAG = CrashReportingApplication.LOG_TAG;

    private static final TrustManager[] TRUST_MANAGER = { new NaiveTrustManager() };

    private static final AllowAllHostnameVerifier HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();

    public static void doPost(Properties parameters, URL url)
            throws UnsupportedEncodingException, IOException, KeyManagementException, NoSuchAlgorithmException {
        URLConnection cnx = getConnection(url);
        
        // Construct data
        StringBuilder dataBfr = new StringBuilder();
        Iterator<Object> iKeys = parameters.keySet().iterator();
        while (iKeys.hasNext()) {
            if (dataBfr.length() != 0) {
                dataBfr.append('&');
            }
            String key = (String) iKeys.next();
            dataBfr.append(URLEncoder.encode(key, "UTF-8")).append('=').append(
                    URLEncoder.encode((String) parameters.get(key), "UTF-8"));
        }
        // POST data
        cnx.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(cnx.getOutputStream());
        wr.write(dataBfr.toString());
        wr.flush();
        wr.close();

        BufferedReader rd = new BufferedReader(new InputStreamReader(cnx
                .getInputStream()));

        String line;
        while ((line = rd.readLine()) != null) {
            Log.d(LOG_TAG, line);
        }
        rd.close();
    }

    public static URLConnection getConnection(URL url) throws IOException,
            NoSuchAlgorithmException, KeyManagementException {
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            // Trust all certificates
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(new KeyManager[0], TRUST_MANAGER, new SecureRandom());
            SSLSocketFactory socketFactory = context.getSocketFactory();
            ((HttpsURLConnection) conn).setSSLSocketFactory(socketFactory);

            // Allow all hostnames
            ((HttpsURLConnection) conn)
                    .setHostnameVerifier(HOSTNAME_VERIFIER);

        }
        return conn;
    }

}
