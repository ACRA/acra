package org.acra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;

import android.util.Log;

public class HttpUtils {
    private static final String LOG_TAG = CrashReportingApplication.LOG_TAG;
        public static void doPost(Properties parameters,
            URLConnection cnx) throws UnsupportedEncodingException,
            IOException {
        // Construct data
        StringBuilder dataBfr = new StringBuilder();
        Iterator<Object> iKeys = parameters.keySet().iterator();
        while (iKeys.hasNext()) {
            if (dataBfr.length() != 0) {
                dataBfr.append('&');
            }
            String key = (String)iKeys.next();
            dataBfr.append(URLEncoder.encode(key, "UTF-8")).append('=').append(
                    URLEncoder.encode((String)parameters.get(key), "UTF-8"));
        }
        // POST data
        cnx.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(cnx
                .getOutputStream());
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


}
