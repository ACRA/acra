package org.acra.sender;

import static org.acra.ACRA.LOG_TAG;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.util.HttpUtils;

import android.net.Uri;
import android.util.Log;

public class HttpPostSender implements ReportSender {
    private Uri mFormUri = null;
    private Map<ReportField, String> mMapping = null;

    public HttpPostSender(String formUri, Map<ReportField, String> mapping) {
        mFormUri = Uri.parse(formUri);
        mMapping = mapping;
    }

    @Override
    public void send(Map<ReportField, String> report) throws ReportSenderException {

        try {
            URL reportUrl;
            Map<String, String> finalReport = remap(report);
            reportUrl = new URL(mFormUri.toString());
            Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
            HttpUtils.doPost(finalReport, reportUrl, ACRA.getConfig().formUriBasicAuthLogin(), ACRA.getConfig().formUriBasicAuthPassword());
        } catch (Exception e) {
            throw new ReportSenderException("Error while sending report to Http Post Form.", e);
        }

    }

    private Map<String, String> remap(Map<ReportField, String> report) {
        Map<String, String> finalReport = new HashMap<String, String>(report.size());
        for(ReportField field : report.keySet()) {
            if(mMapping == null || mMapping.get(field) == null) {
                finalReport.put(field.toString(), report.get(field));
            } else {
                finalReport.put(mMapping.get(field), report.get(field));
            }
        }
        return null;
    }

}