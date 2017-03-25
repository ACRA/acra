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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.attachment.DefaultAttachmentProvider;
import org.acra.collections.ImmutableSet;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.http.BinaryHttpRequest;
import org.acra.http.DefaultHttpRequest;
import org.acra.http.HttpUtils;
import org.acra.http.MultipartHttpRequest;
import org.acra.model.Element;
import org.acra.util.InstanceCreator;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * <p>
 * To use specific POST parameter names, you can provide your own report fields
 * mapping scheme:
 * </p>
 * <pre>
 * Just create and declare a {@link ReportSenderFactory} that constructs a mapping
 * from each {@link ReportField} to another name.
 * </pre>
 */
public class HttpSender implements ReportSender {

    /**
     * Available HTTP methods to send data. Only POST and PUT are currently
     * supported.
     */
    public enum Method {
        POST {
            @Override
            URL createURL(String baseUrl, CrashReportData report) throws MalformedURLException {
                return new URL(baseUrl);
            }
        },
        PUT {
            @Override
            URL createURL(String baseUrl, CrashReportData report) throws MalformedURLException {
                return new URL(baseUrl + '/' + report.getProperty(ReportField.REPORT_ID));
            }
        };

        abstract URL createURL(String baseUrl, CrashReportData report) throws MalformedURLException;
    }

    /**
     * Type of report data encoding, currently supports Html Form encoding and
     * JSON.
     */
    public enum Type {
        /**
         * Send data as a www form encoded list of key/values.
         *
         * @see <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4">Form content types</a>
         */
        FORM("application/x-www-form-urlencoded") {
            @Override
            String convertReport(HttpSender sender, CrashReportData report) throws IOException {
                return HttpUtils.getParamsAsFormString(sender.convertToForm(report));
            }
        },
        /**
         * Send data as a structured JSON tree.
         */
        JSON("application/json") {
            @Override
            String convertReport(HttpSender sender, CrashReportData report) throws IOException {
                return sender.convertToJson(report).toString();
            }
        };
        private final String contentType;

        Type(String contentType) {
            this.contentType = contentType;
        }

        @NonNull
        public String getContentType() {
            return contentType;
        }

        abstract String convertReport(HttpSender sender, CrashReportData report) throws IOException;
    }

    private final ACRAConfiguration config;
    @Nullable
    private final Uri mFormUri;
    private final Map<ReportField, String> mMapping;
    private final Method mMethod;
    private final Type mType;
    @Nullable
    private String mUsername;
    @Nullable
    private String mPassword;

    /**
     * <p>
     * Create a new HttpSender instance with its destination taken from the supplied config.
     * Uses {@link ReportField} values converted to String with .toString() as form parameters.
     * </p>
     *
     * @param config AcraConfig declaring the
     * @param method HTTP {@link Method} to be used to send data. Currently only
     *               {@link Method#POST} and {@link Method#PUT} are available. If
     *               {@link Method#PUT} is used, the {@link ReportField#REPORT_ID}
     *               is appended to the formUri to be compliant with RESTful APIs.
     * @param type   {@link Type} of encoding used to send the report body.
     *               {@link Type#FORM} is a simple Key/Value pairs list as defined
     *               by the application/x-www-form-urlencoded mime type.
     */
    public HttpSender(@NonNull ACRAConfiguration config, @NonNull Method method, @NonNull Type type) {
        this(config, method, type, null);
    }

    /**
     * <p>
     * Create a new HttpSender instance with its destination taken from the supplied config.
     * </p>
     *
     * @param config  AcraConfig declaring the
     * @param method  HTTP {@link Method} to be used to send data. Currently only
     *                {@link Method#POST} and {@link Method#PUT} are available. If
     *                {@link Method#PUT} is used, the {@link ReportField#REPORT_ID}
     *                is appended to the formUri to be compliant with RESTful APIs.
     * @param type    {@link Type} of encoding used to send the report body.
     *                {@link Type#FORM} is a simple Key/Value pairs list as defined
     *                by the application/x-www-form-urlencoded mime type.
     * @param mapping Applies only to {@link Method#POST} method parameter. If null,
     *                POST parameters will be named with {@link ReportField} values
     *                converted to String with .toString(). If not null, POST
     *                parameters will be named with the result of
     *                mapping.get(ReportField.SOME_FIELD);
     */
    public HttpSender(@NonNull ACRAConfiguration config, @NonNull Method method, @NonNull Type type, @Nullable Map<ReportField, String> mapping) {
        this(config, method, type, null, mapping);
    }

    /**
     * <p>
     * Create a new HttpPostSender instance with a fixed destination provided as
     * a parameter. Configuration changes to the formUri are not applied.
     * </p>
     *
     * @param config  AcraConfig declaring the
     * @param method  HTTP {@link Method} to be used to send data. Currently only
     *                {@link Method#POST} and {@link Method#PUT} are available. If
     *                {@link Method#PUT} is used, the {@link ReportField#REPORT_ID}
     *                is appended to the formUri to be compliant with RESTful APIs.
     * @param type    {@link Type} of encoding used to send the report body.
     *                {@link Type#FORM} is a simple Key/Value pairs list as defined
     *                by the application/x-www-form-urlencoded mime type.
     * @param formUri The URL of your server-side crash report collection script.
     * @param mapping Applies only to {@link Method#POST} method parameter. If null,
     *                POST parameters will be named with {@link ReportField} values
     *                converted to String with .toString(). If not null, POST
     *                parameters will be named with the result of
     *                mapping.get(ReportField.SOME_FIELD);
     */
    public HttpSender(@NonNull ACRAConfiguration config, @NonNull Method method, @NonNull Type type, @Nullable String formUri, @Nullable Map<ReportField, String> mapping) {
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
     * @param username The username to set for HTTP Basic Auth.
     * @param password The password to set for HTTP Basic Auth.
     */
    @SuppressWarnings("unused")
    public void setBasicAuth(@Nullable String username, @Nullable String password) {
        mUsername = username;
        mPassword = password;
    }

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData report) throws ReportSenderException {

        try {
            final String baseUrl = mFormUri == null ? config.formUri() : mFormUri.toString();
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Connect to " + baseUrl);

            final String login = mUsername != null ? mUsername : isNull(config.formUriBasicAuthLogin()) ? null : config.formUriBasicAuthLogin();
            final String password = mPassword != null ? mPassword : isNull(config.formUriBasicAuthPassword()) ? null : config.formUriBasicAuthPassword();

            final InstanceCreator instanceCreator = new InstanceCreator();
            final List<Uri> uris = instanceCreator.create(config.attachmentUriProvider(), new DefaultAttachmentProvider()).getAttachments(context, config);

            // Generate report body depending on requested type
            final String reportAsString = mType.convertReport(this, report);

            // Adjust URL depending on method
            final URL reportUrl = mMethod.createURL(baseUrl, report);

            sendHttpRequests(config, context, mMethod, mType, login, password, config.connectionTimeout(),
                    config.socketTimeout(), config.httpHeaders(), reportAsString, reportUrl, uris);

        } catch (@NonNull IOException e) {
            throw new ReportSenderException("Error while sending " + config.reportType()
                    + " report via Http " + mMethod.name(), e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void sendHttpRequests(@NonNull ACRAConfiguration configuration, @NonNull Context context, @NonNull Method method, @NonNull Type type,
                                    @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                    @NonNull String content, @NonNull URL url, @NonNull List<Uri> attachments) throws IOException {
        switch (method) {
            case POST:
                if (attachments.isEmpty()) {
                    sendWithoutAttachments(configuration, context, method, type, login, password, connectionTimeOut, socketTimeOut, headers, content, url);
                } else {
                    postMultipart(configuration, context, type, login, password, connectionTimeOut, socketTimeOut, headers, content, url, attachments);
                }
                break;
            case PUT:
                sendWithoutAttachments(configuration, context, method, type, login, password, connectionTimeOut, socketTimeOut, headers, content, url);
                for (Uri uri : attachments) {
                    putAttachment(configuration, context, login, password, connectionTimeOut, socketTimeOut, headers, url, uri);
                }
                break;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void sendWithoutAttachments(@NonNull ACRAConfiguration configuration, @NonNull Context context, @NonNull Method method, @NonNull Type type,
                                          @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                          @NonNull String content, @NonNull URL url) throws IOException {
        new DefaultHttpRequest(configuration, context, method, type, login, password, connectionTimeOut, socketTimeOut, headers).send(url, content);
    }

    @SuppressWarnings("WeakerAccess")
    protected void postMultipart(@NonNull ACRAConfiguration configuration, @NonNull Context context, @NonNull Type type,
                                 @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                 @NonNull String content, @NonNull URL url, @NonNull List<Uri> attachments) throws IOException {
        new MultipartHttpRequest(configuration, context, type, login, password, connectionTimeOut, socketTimeOut, headers).send(url, Pair.create(content, attachments));
    }

    @SuppressWarnings("WeakerAccess")
    protected void putAttachment(@NonNull ACRAConfiguration configuration, @NonNull Context context,
                                 @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                 @NonNull URL url, @NonNull Uri attachment) throws IOException {
        final URL attachmentUrl = new URL(url.toString() + "-" + HttpUtils.getFileNameFromUri(context, attachment));
        new BinaryHttpRequest(configuration, context, Method.PUT, login, password, connectionTimeOut, socketTimeOut, headers).send(attachmentUrl, attachment);
    }

    /**
     * Convert a report to json
     *
     * @param report the report to convert
     * @return a json representation of the report
     */
    @SuppressWarnings("WeakerAccess")
    protected JSONObject convertToJson(CrashReportData report) {
        return report.toJSON();
    }

    /**
     * Convert a report to a form-prepared map
     *
     * @param report the report to convert
     * @return a form representation of the report
     */
    @SuppressWarnings("WeakerAccess")
    protected Map<String, String> convertToForm(CrashReportData report) {
        return remap(report);
    }

    @NonNull
    private Map<String, String> remap(@NonNull Map<ReportField, Element> report) {

        Set<ReportField> fields = config.reportContent();
        if (fields.isEmpty()) {
            fields = new ImmutableSet<ReportField>(ACRAConstants.DEFAULT_REPORT_FIELDS);
        }

        final Map<String, String> finalReport = new HashMap<String, String>(report.size());
        for (ReportField field : fields) {
            final Element element = report.get(field);
            final String value = element != null ? TextUtils.join("\n", element.flatten()) : null;
            if (mMapping == null || mMapping.get(field) == null) {
                finalReport.put(field.toString(), value);
            } else {
                finalReport.put(mMapping.get(field), value);
            }
        }
        return finalReport;
    }

    private boolean isNull(@Nullable String aString) {
        return aString == null || ACRAConstants.NULL_VALUE.equals(aString);
    }

}