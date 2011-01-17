package org.acra.sender;

import static org.acra.ACRA.LOG_TAG;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;


import android.net.Uri;
import android.util.Log;

public class HttpPostSender implements ReportSender {
    Uri mFormUri = null;

    public HttpPostSender(String formUri) {
        mFormUri = Uri.parse(formUri);
    }

    @Override
    public void send(Properties report) throws ReportSenderException {

        try {
            URL reportUrl;
            reportUrl = new URL(mFormUri.toString());
            Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
            HttpUtils.doPost(report, reportUrl);
        } catch (Exception e) {
            throw new ReportSenderException("Error while sending report to Http Post Form.", e);
        }

    }

}