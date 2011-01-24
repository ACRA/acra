package org.acra.sender;

import static org.acra.ACRA.LOG_TAG;

import java.net.URL;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.util.HttpUtils;

import android.net.Uri;
import android.util.Log;

public class HttpPostSender implements ReportSender {
    Uri mFormUri = null;

    public HttpPostSender(String formUri) {
        mFormUri = Uri.parse(formUri);
    }

    @Override
    public void send(Map<ReportField, String> report) throws ReportSenderException {

        try {
            URL reportUrl;
            reportUrl = new URL(mFormUri.toString());
            Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
            HttpUtils.doPost(report, reportUrl, ACRA.getConfig().formUriBasicAuthLogin(), ACRA.getConfig().formUriBasicAuthPassword());
        } catch (Exception e) {
            throw new ReportSenderException("Error while sending report to Http Post Form.", e);
        }

    }

}