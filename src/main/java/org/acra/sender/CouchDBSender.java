package org.acra.sender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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
        for (ReportField key : errorContent.keySet()) {
            try {
                if (key.containsKeyValuePairs()) {
                    JSONObject subObject = new JSONObject();
                    String strContent = errorContent.getProperty(key);
                    BufferedReader reader = new BufferedReader(new StringReader(strContent),1024);
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            int equalsIndex = line.indexOf('=');
                            if (equalsIndex > 0) {
                                subObject.accumulate(line.substring(0, equalsIndex).trim(), line.substring(equalsIndex+1).trim());
                            }
                        }
                    } catch (IOException e) {
                        ACRA.log.e(ACRA.LOG_TAG, "Error while converting " + key.name() + " to JSON.", e);
                    }
                    jsonReport.accumulate(key.name(), subObject);
                } else {
                    jsonReport.accumulate(key.name(), errorContent.getProperty(key));
                }
            } catch (JSONException e) {
                throw new ReportSenderException("Could not create JSON object for key " + key, e);
            }
        }
        try {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.sendPut(new URL(mCouchDBBaseURL + "/" + errorContent.getProperty(ReportField.REPORT_ID)),
                    jsonReport.toString());
        } catch (MalformedURLException e) {
            ACRA.log.e(ACRA.LOG_TAG, "Error : ", e);
        } catch (IOException e) {
            ACRA.log.e(ACRA.LOG_TAG, "Error : ", e);
        }
    }

}
