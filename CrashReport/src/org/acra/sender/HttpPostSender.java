package org.acra.sender;

import static org.acra.ACRA.LOG_TAG;

import java.net.URL;

import org.acra.CrashReportData;

import android.net.Uri;
import android.util.Log;

public class HttpPostSender implements ReportSender {
    Uri mFormUri = null;

    public HttpPostSender(String formUri) {
        mFormUri = Uri.parse(formUri);
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {

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