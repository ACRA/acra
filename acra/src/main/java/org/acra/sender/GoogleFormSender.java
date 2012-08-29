/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.sender;

import static org.acra.ACRA.LOG_TAG;

import java.io.IOException;
import java.net.URL;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.collector.CrashReportData;
import org.acra.ReportField;
import org.acra.util.HttpRequest;

import android.net.Uri;
import android.util.Log;

/**
 * ACRA's default {@link ReportSender}: sends report data to a GoogleDocs Form.
 * 
 * @author Kevin Gaudin
 * 
 */
public class GoogleFormSender implements ReportSender {

    /**
     * Creates a new GoogleFormSender which will send data to a Form identified
     * by its key. All parameters are retrieved from {@link ACRA#getConfig()}.
     */
    public GoogleFormSender() {
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {
        Uri formUri = Uri.parse(String.format(ACRA.getConfig().googleFormUrlFormat(), ACRA.getConfig().formKey()));
        final Map<String, String> formParams = remap(report);
        // values observed in the GoogleDocs original html form
        formParams.put("pageNumber", "0");
        formParams.put("backupCache", "");
        formParams.put("submit", "Envoyer");

        try {
            final URL reportUrl = new URL(formUri.toString());
            Log.d(LOG_TAG, "Sending report " + report.get(ReportField.REPORT_ID));
            Log.d(LOG_TAG, "Connect to " + reportUrl);

            final HttpRequest request = new HttpRequest();
            request.setConnectionTimeOut(ACRA.getConfig().connectionTimeout());
            request.setSocketTimeOut(ACRA.getConfig().socketTimeout());
            request.setMaxNrRetries(ACRA.getConfig().maxNumberOfRequestRetries());
            request.sendPost(reportUrl, formParams);

        } catch (IOException e) {
            throw new ReportSenderException("Error while sending report to Google Form.", e);
        }
    }

    private Map<String, String> remap(Map<ReportField, String> report) {

        ReportField[] fields = ACRA.getConfig().customReportContent();
        if (fields.length == 0) {
            fields = ACRA.DEFAULT_REPORT_FIELDS;
        }

        int inputId = 0;
        final Map<String, String> result = new HashMap<String, String>();
        for (ReportField originalKey : fields) {
            switch (originalKey) {
            case APP_VERSION_NAME:
                result.put("entry." + inputId + ".single", "'" + report.get(originalKey));
                break;
            case ANDROID_VERSION:
                result.put("entry." + inputId + ".single", "'" + report.get(originalKey));
                break;
            default:
                result.put("entry." + inputId + ".single", report.get(originalKey));
                break;
            }
            inputId++;
        }
        return result;
    }
}
