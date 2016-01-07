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

import android.content.Context;
import android.net.Uri;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.config.AcraConfig;
import org.acra.util.HttpRequest;
import org.acra.util.JSONReportBuilder.JSONReportException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

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
 * Just create and declare a {@link ReportSenderFactory} that constructs a mapping
 * from each {@link ReportField} to another name.
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
         * Send data as a www form encoded list of key/values.
         * @see <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4">Form content types</a>
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

    private final AcraConfig config;
    private final Uri mFormUri;
    private final Map<ReportField, String> mMapping;
    private final Method mMethod;
    private final Type mType;
    private String mUsername;
    private String mPassword;

    /**
     * <p>
     * Create a new HttpSender instance with its destination taken from the supplied config.
     * </p>
     *
     * @param config    AcraConfig declaring the
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
    public HttpSender(AcraConfig config, Method method, Type type, Map<ReportField, String> mapping) {
        this(config, method, type, null, mapping);
    }

    /**
     * <p>
     * Create a new HttpPostSender instance with a fixed destination provided as
     * a parameter. Configuration changes to the formUri are not applied.
     * </p>
     *
     * @param config    AcraConfig declaring the
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
    public HttpSender(AcraConfig config, Method method, Type type, String formUri, Map<ReportField, String> mapping) {
        this.config = config;
        mMethod = method;
        mFormUri = (formUri == null) ? null : Uri.parse(formUri);
        mMapping = mapping;
        mType = type;
        mUsername = null;
        mPassword = null;        
    }
    
    /**
     * <p>
     * Set credentials for this HttpSender that override (if present) the ones
     * set globally.
     * </p>
     * 
     * @param username
     *            The username to set for HTTP Basic Auth.
     * @param password
     *            The password to set for HTTP Basic Auth.
     */
    @SuppressWarnings( "unused" )
    public void setBasicAuth(String username, String password) {
        mUsername = username;
        mPassword = password;
    }    

    @Override
    public void send(Context context, CrashReportData report) throws ReportSenderException {

        try {
            URL reportUrl = mFormUri == null ? new URL(config.formUri()) : new URL(mFormUri.toString());
            ACRA.log.d(LOG_TAG, "Connect to " + reportUrl.toString());

            final String login = mUsername != null ? mUsername : isNull(config.formUriBasicAuthLogin()) ? null : config.formUriBasicAuthLogin();
            final String password = mPassword != null ? mPassword : isNull(config.formUriBasicAuthPassword()) ? null : config.formUriBasicAuthPassword();

            final HttpRequest request = new HttpRequest(config);
            request.setConnectionTimeOut(config.connectionTimeout());
            request.setSocketTimeOut(config.socketTimeout());
            request.setLogin(login);
            request.setPassword(password);
            request.setHeaders(config.getHttpHeaders());

            // Generate report body depending on requested type
            final String reportAsString;
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
            throw new ReportSenderException("Error while sending " + config.reportType()
                    + " report via Http " + mMethod.name(), e);
        } catch (JSONReportException e) {
            throw new ReportSenderException("Error while sending " + config.reportType()
                    + " report via Http " + mMethod.name(), e);
        }
    }

    private Map<String, String> remap(Map<ReportField, String> report) {

        ReportField[] fields = config.customReportContent();
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

    private boolean isNull(String aString) {
        return aString == null || ACRAConstants.NULL_VALUE.equals(aString);
    }
}