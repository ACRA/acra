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
import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.util.HttpRequest;
import org.acra.util.JSONReportBuilder.JSONReportException;

import android.net.Uri;
import android.util.Log;

/**
 * <p>
 * The {@link ReportSender} used by ACRA when {@link ReportsCrashes#formUri()}
 * has been defined in order to post crash data to a custom server-side data
 * collection script. It sends all data in a POST request with parameters named
 * with easy to understand names (basically a string conversion of
 * {@link ReportField} enum values) or based on your own conversion Map from
 * {@link ReportField} values to String.
 * </p>
 * 
 * <p>
 * To use specific POST parameter names, you can provide your own report fields
 * mapping scheme:
 * </p>
 * 
 * <pre>
 * &#64;ReportsCrashes(...)
 * public class myApplication extends Application {
 * 
 *     public void onCreate() {
 *         super.onCreate();
 *         ACRA.init(this);
 *         Map&lt;ReportField, String&gt; mapping = new HashMap&lt;ReportField, String&gt;();
 *         mapping.put(ReportField.APP_VERSION_CODE, &quot;myAppVerCode'); 
 *         mapping.put(ReportField.APP_VERSION_NAME, &quot;myAppVerName');
 *         //... 
 *         mapping.put(ReportField.USER_EMAIL, &quot;userEmail');
 *         // remove any default report sender
 *         ErrorReporter.getInstance().removeAllReportSenders();
 *         // create your own instance with your specific mapping
 *         ErrorReporter.getInstance().addReportSender(new ReportSender(&quot;http://my.domain.com/reports/receiver.py&quot;, mapping));
 *     }
 * }
 * </pre>
 * 
 */
public class HttpSender implements ReportSender {

    /**
     * Available HTTP methods to send data. Only POST and PUT are currently
     * supported.
     */
    public enum Method {
        POST, PUT
    }

    /**
     * Type of report data encoding, currently supports Html Form encoding and
     * JSON.
     */
    public enum Type {
        /**
         * Send data as a www form encoded list of key/values. {@link http
         * ://www.w3.org/TR/html401/interact/forms.html#h-17.13.4}
         */
        FORM {
            @Override
            public String getContentType() {
                return "application/x-www-form-urlencoded";
            }
        },
        /**
         * Send data as a structured JSON tree.
         */
        JSON {
            @Override
            public String getContentType() {
                return "application/json";
            }
        };

        public abstract String getContentType();
    }

    private final Uri mFormUri;
    private final Map<ReportField, String> mMapping;
    private final Method mMethod;
    private final Type mType;

    /**
     * <p>
     * Create a new HttpSender instance with its destination taken from
     * {@link ACRA#getConfig()} dynamically. Configuration changes to the
     * formUri are applied automatically.
     * </p>
     * 
     * @param method
     *            HTTP {@link Method} to be used to send data. Currently only
     *            {@link Method#POST} and {@link Method#PUT} are available. If
     *            {@link Method#PUT} is used, the {@link ReportField#REPORT_ID}
     *            is appended to the formUri to be compliant with RESTful APIs.
     * 
     * @param type
     *            {@link Type} of encoding used to send the report body.
     *            {@link Type#FORM} is a simple Key/Value pairs list as defined
     *            by the application/x-www-form-urlencoded mime type.
     * 
     * @param mapping
     *            Applies only to {@link Method#POST} method parameter. If null,
     *            POST parameters will be named with {@link ReportField} values
     *            converted to String with .toString(). If not null, POST
     *            parameters will be named with the result of
     *            mapping.get(ReportField.SOME_FIELD);
     */
    public HttpSender(Method method, Type type, Map<ReportField, String> mapping) {
        mMethod = method;
        mFormUri = null;
        mMapping = mapping;
        mType = type;
    }

    /**
     * <p>
     * Create a new HttpPostSender instance with a fixed destination provided as
     * a parameter. Configuration changes to the formUri are not applied.
     * </p>
     * 
     * @param method
     *            HTTP {@link Method} to be used to send data. Currently only
     *            {@link Method#POST} and {@link Method#PUT} are available. If
     *            {@link Method#PUT} is used, the {@link ReportField#REPORT_ID}
     *            is appended to the formUri to be compliant with RESTful APIs.
     * 
     * @param type
     *            {@link Type} of encoding used to send the report body.
     *            {@link Type#FORM} is a simple Key/Value pairs list as defined
     *            by the application/x-www-form-urlencoded mime type.
     * @param formUri
     *            The URL of your server-side crash report collection script.
     * @param mapping
     *            Applies only to {@link Method#POST} method parameter. If null,
     *            POST parameters will be named with {@link ReportField} values
     *            converted to String with .toString(). If not null, POST
     *            parameters will be named with the result of
     *            mapping.get(ReportField.SOME_FIELD);
     */
    public HttpSender(Method method, Type type, String formUri, Map<ReportField, String> mapping) {
        mMethod = method;
        mFormUri = Uri.parse(formUri);
        mMapping = mapping;
        mType = type;
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {

        try {
            URL reportUrl = mFormUri == null ? new URL(ACRA.getConfig().formUri()) : new URL(mFormUri.toString());
            Log.d(LOG_TAG, "Connect to " + reportUrl.toString());

            final String login = ACRAConfiguration.isNull(ACRA.getConfig().formUriBasicAuthLogin()) ? null : ACRA
                    .getConfig().formUriBasicAuthLogin();
            final String password = ACRAConfiguration.isNull(ACRA.getConfig().formUriBasicAuthPassword()) ? null : ACRA
                    .getConfig().formUriBasicAuthPassword();

            final HttpRequest request = new HttpRequest();
            request.setConnectionTimeOut(ACRA.getConfig().connectionTimeout());
            request.setSocketTimeOut(ACRA.getConfig().socketTimeout());
            request.setMaxNrRetries(ACRA.getConfig().maxNumberOfRequestRetries());
            request.setLogin(login);
            request.setPassword(password);
            request.setHeaders(ACRA.getConfig().getHttpHeaders());

            String reportAsString = "";

            // Generate report body depending on requested type
            switch (mType) {
            case JSON:
                reportAsString = report.toJSON().toString();
                break;
            case FORM:
            default:
                final Map<String, String> finalReport = remap(report);
                reportAsString = HttpRequest.getParamsAsFormString(finalReport);
                break;

            }

            // Adjust URL depending on method
            switch (mMethod) {
            case POST:
                break;
            case PUT:
                reportUrl = new URL(reportUrl.toString() + '/' + report.getProperty(ReportField.REPORT_ID));
                break;
            default:
                throw new UnsupportedOperationException("Unknown method: " + mMethod.name());
            }
            request.send(reportUrl, mMethod, reportAsString, mType);

        } catch (IOException e) {
            throw new ReportSenderException("Error while sending " + ACRA.getConfig().reportType()
                    + " report via Http " + mMethod.name(), e);
        } catch (JSONReportException e) {
            throw new ReportSenderException("Error while sending " + ACRA.getConfig().reportType()
                    + " report via Http " + mMethod.name(), e);
        }
    }

    private Map<String, String> remap(Map<ReportField, String> report) {

        ReportField[] fields = ACRA.getConfig().customReportContent();
        if (fields.length == 0) {
            fields = ACRAConstants.DEFAULT_REPORT_FIELDS;
        }

        final Map<String, String> finalReport = new HashMap<String, String>(report.size());
        for (ReportField field : fields) {
            if (mMapping == null || mMapping.get(field) == null) {
                finalReport.put(field.toString(), report.get(field));
            } else {
                finalReport.put(mMapping.get(field), report.get(field));
            }
        }
        return finalReport;
    }

}