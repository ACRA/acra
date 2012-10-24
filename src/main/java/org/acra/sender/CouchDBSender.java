package org.acra.sender;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.util.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class CouchDBSender implements ReportSender {

    private String mCouchDBBaseURL;
    public CouchDBSender(String couchDBBaseURL) {
        this.mCouchDBBaseURL = couchDBBaseURL;
    }

    @Override
    public void send(CrashReportData errorContent) throws ReportSenderException {
        
        JSONObject jsonReport = new JSONObject();
        for(ReportField key : errorContent.keySet()) {
            try {
                jsonReport.accumulate(key.name(), errorContent.getProperty(key));
            } catch (JSONException e) {
                throw new ReportSenderException("Could not create JSON object for key " + key, e);
            }
        }
        try {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.sendPut(new URL(mCouchDBBaseURL + "/" + errorContent.getProperty(ReportField.REPORT_ID)), jsonReport.toString());
        } catch (MalformedURLException e) {
            ACRA.log.e(ACRA.LOG_TAG, "Error : ", e);
        } catch (IOException e) {
            ACRA.log.e(ACRA.LOG_TAG, "Error : ", e);
        }
    }

}
