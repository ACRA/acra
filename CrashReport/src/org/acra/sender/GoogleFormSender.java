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

public class GoogleFormSender implements ReportSender {
    Uri mFormUri = null;

    public GoogleFormSender(String formKey) {
        mFormUri = Uri.parse("http://spreadsheets.google.com/formResponse?formkey=" + formKey + "&amp;ifq");
    }

    @Override
    public void send(Properties report) throws ReportSenderException {
        // values observed in the GoogleDocs original html form
        report.put("pageNumber", "0");
        report.put("backupCache", "");
        report.put("submit", "Envoyer");

        try {
            URL reportUrl;
            reportUrl = new URL(mFormUri.toString());
            Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
            HttpUtils.doPost(report, reportUrl);
        } catch (Exception e) {
            throw new ReportSenderException("Error while sending report to Google Form.", e);
        }

    }

}
