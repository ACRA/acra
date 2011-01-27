package org.acra.sender;

import static org.acra.ACRA.LOG_TAG;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.acra.CrashReportData;
import org.acra.ReportField;
import org.acra.util.HttpUtils;

import android.net.Uri;
import android.util.Log;

public class GoogleFormSender implements ReportSender {
    private Uri mFormUri = null;

    public GoogleFormSender(String formKey) {
        mFormUri = Uri.parse("http://spreadsheets.google.com/formResponse?formkey=" + formKey + "&amp;ifq");
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {
        Map<String,String> formParams = remap(report);
        // values observed in the GoogleDocs original html form
        formParams.put("pageNumber", "0");
        formParams.put("backupCache", "");
        formParams.put("submit", "Envoyer");

        try {
            URL reportUrl;
            reportUrl = new URL(mFormUri.toString());
            Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
            HttpUtils.doPost(formParams, reportUrl, null, null);
        } catch (Exception e) {
            throw new ReportSenderException("Error while sending report to Google Form.", e);
        }

    }

    private Map<String, String> remap(Map<ReportField, String> report) {
        Map<String, String> result = new HashMap<String, String>();
        
        for(Object originalKey : report.keySet()) {
            switch((ReportField)originalKey) {
            case VERSION_NAME:
                result.put("entry.0.single",report.get(originalKey));
                break;
            case PACKAGE_NAME:
                result.put("entry.1.single",report.get(originalKey));
                break;
            case FILE_PATH:
                result.put("entry.2.single",report.get(originalKey));
                break;
            case PHONE_MODEL:
                result.put("entry.3.single",report.get(originalKey));
                break;
            case ANDROID_VERSION:
                result.put("entry.4.single",report.get(originalKey));
                break;
            case BOARD:
                result.put("entry.5.single",report.get(originalKey));
                break;
            case BRAND:
                result.put("entry.6.single",report.get(originalKey));
                break;
            case DEVICE:
                result.put("entry.7.single",report.get(originalKey));
                break;
            case BUILD_DISPLAY:
                result.put("entry.8.single",report.get(originalKey));
                break;
            case FINGERPRINT:
                result.put("entry.9.single",report.get(originalKey));
                break;
            case HOST:
                result.put("entry.10.single",report.get(originalKey));
                break;
            case ID:
                result.put("entry.11.single",report.get(originalKey));
                break;
            case MODEL:
                result.put("entry.12.single",report.get(originalKey));
                break;
            case PRODUCT:
                result.put("entry.13.single",report.get(originalKey));
                break;
            case TAGS:
                result.put("entry.14.single",report.get(originalKey));
                break;
            case TIME:
                result.put("entry.15.single",report.get(originalKey));
                break;
            case TYPE:
                result.put("entry.16.single",report.get(originalKey));
                break;
            case USER:
                result.put("entry.17.single",report.get(originalKey));
                break;
            case TOTAL_MEM_SIZE:
                result.put("entry.18.single",report.get(originalKey));
                break;
            case AVAILABLE_MEM_SIZE:
                result.put("entry.19.single",report.get(originalKey));
                break;
            case CUSTOM_DATA:
                result.put("entry.20.single",report.get(originalKey));
                break;
            case STACK_TRACE:
                result.put("entry.21.single",report.get(originalKey));
                break;
            case INITIAL_CONFIGURATION:
                result.put("entry.22.single",report.get(originalKey));
                break;
            case CRASH_CONFIGURATION:
                result.put("entry.23.single",report.get(originalKey));
                break;
            case DISPLAY:
                result.put("entry.24.single",report.get(originalKey));
                break;
            case USER_COMMENT:
                result.put("entry.25.single",report.get(originalKey));
                break;
            case USER_CRASH_DATE:
                result.put("entry.26.single",report.get(originalKey));
                break;
            case DUMPSYS_MEMINFO:
                result.put("entry.27.single",report.get(originalKey));
                break;
            case DROPBOX:
                result.put("entry.28.single",report.get(originalKey));
                break;
            case LOGCAT:
                result.put("entry.29.single",report.get(originalKey));
                break;
            case EVENTSLOG:
                result.put("entry.30.single",report.get(originalKey));
                break;
            case RADIOLOG:
                result.put("entry.31.single",report.get(originalKey));
                break;
            default:
                result.put(originalKey.toString(),report.get(originalKey));
                break;
            }
        }
        return result;
    }

}
