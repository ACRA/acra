package org.acra.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.collector.CollectorUtil;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSenderException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONReportBuilder {
    /**
     * <p>
     * Create a JSONObject containing the whole report data with the most
     * detailed possible structure depth. Fields are not just converted to a
     * single key=value pair. If a value can be decomposed into subobjects, it
     * is done.
     * </p>
     *
     * <p>
     * For example, a String containing:
     *
     * <pre>
     * some.key.name1=value1
     * some.key.name2=value2
     * some.other=value3
     * any.other.key=value4
     * key.without.value5
     * </pre>
     *
     * is converted to
     *
     * <pre>
     * {
     *   some : {
     *     key : {
     *       name1 : "value1",
     *       name2 : "value2"
     *     },
     *     other : "value3"
     *   },
     *   any : {
     *     other : {
     *       key : "value4"
     *     }
     *   }
     *   key.without.value : true
     * }
     * </pre>
     *
     * </p>
     *
     * @param errorContent
     *            The ACRA report data structure.
     * @return A JSONObject containing all fields from the report converted to
     *         JSON.
     * @throws ReportSenderException
     * @throws JSONReportException
     */
    public static JSONObject buildJSONReport(CrashReportData errorContent) throws JSONReportException {
        JSONObject jsonReport = new JSONObject();
        BufferedReader reader = null;
        for (ReportField key : errorContent.keySet()) {
            try {
                // Each ReportField can be identified as a substructure and not
                // a simple String value.
                if (key.containsKeyValuePairs()) {
                    JSONObject subObject = new JSONObject();
                    String strContent = errorContent.getProperty(key);
                    reader = new BufferedReader(new StringReader(strContent), 1024);
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            addJSONFromProperty(subObject, line);
                        }
                    } catch (IOException e) {
                        ACRA.log.e(ACRA.LOG_TAG, "Error while converting " + key.name() + " to JSON.", e);
                    }
                    jsonReport.accumulate(key.name(), subObject);
                } else {
                    // This field is a simple String value, store it as it is
                    jsonReport.accumulate(key.name(), guessType(errorContent.getProperty(key)));
                }
            } catch (JSONException e) {
                throw new JSONReportException("Could not create JSON object for key " + key, e);
            } finally {
            	CollectorUtil.safeClose(reader);
            }
        }
        return jsonReport;
    }

    /**
     * <p>
     * Given a String containing key=value pairs on each line, adds a detailed
     * JSON structure to an existing JSONObject, reusing intermediate subobjects
     * if available when keys are composed of a succession of subkeys delimited
     * by dots.
     * </p>
     *
     * <p>
     * For example, adding the string "metrics.xdpi=160.0" to an object
     * containing
     *
     * <pre>
     * {
     *   "metrics" : { "ydpi" : "160.0"},
     *   "width" : "320",
     *   "height" : "533"
     * }
     * </pre>
     *
     * results in
     *
     * <pre>
     * {
     *   "metrics" : { "ydpi" : "160.0", "xdpi" : "160.0"},
     *   "width" : "320",
     *   "height" : "533"
     * }
     * </pre>
     *
     * </p>
     *
     * @param destination
     *            The JSONObject where the data must be inserted.
     * @param propertyString
     *            A string containing "some.key.name=Any value"
     * @throws JSONException
     */
    private static void addJSONFromProperty(JSONObject destination, String propertyString) throws JSONException {
        int equalsIndex = propertyString.indexOf('=');
        if (equalsIndex > 0) {
            JSONObject finalObject = destination;
            String currentKey = propertyString.substring(0, equalsIndex).trim();
            String currentValue = propertyString.substring(equalsIndex + 1).trim();
            Object value = guessType(currentValue);
            if(value instanceof String) {
                value = ((String) value).replaceAll("\\\\n","\n");
            }
            String[] splitKey = currentKey.split("\\.");
            if (splitKey.length > 1) {
                addJSONSubTree(finalObject, splitKey, value);
            } else {
                finalObject.accumulate(currentKey, value);
            }
        } else {
            destination.put(propertyString.trim(), true);
        }
    }

    private static Object guessType(String value) {
        if (value.equalsIgnoreCase("true"))
            return true;
        if (value.equalsIgnoreCase("false"))
            return false;

        if (value.matches("(?:^|\\s)([1-9](?:\\d*|(?:\\d{0,2})(?:,\\d{3})*)(?:\\.\\d*[1-9])?|0?\\.\\d*[1-9]|0)(?:\\s|$)")) {
            NumberFormat format = NumberFormat.getInstance(Locale.US);
            try {
                Number number = format.parse(value);
                return number;
            } catch (ParseException e) {
                // never mind
            }
        }
        return value;
    }

    /**
     * Deep insert a value inside a JSONObject, reusing existing subobjects when
     * available or creating them when necessary.
     *
     * @param destination
     *            The JSONObject which receives the additional subitem.
     * @param keys
     *            An array containing the path keys leading to where the value
     *            has to be inserted.
     * @param value
     *            The value to be inserted.
     * @throws JSONException
     */
    private static void addJSONSubTree(JSONObject destination, String[] keys, Object value) throws JSONException {
        for (int i = 0; i < keys.length; i++) {
            String subKey = keys[i];
            if (i < keys.length - 1) {
                JSONObject intermediate = null;
                if (destination.isNull(subKey)) {
                    intermediate = new JSONObject();
                    destination.accumulate(subKey, intermediate);
                } else {
                    Object target = destination.get(subKey);
                    if (target instanceof JSONObject) {
                        intermediate = destination.getJSONObject(subKey);
                    } else if (target instanceof JSONArray) {
                        // Unexpected JSONArray, see issue #186
                        JSONArray wildCard = destination.getJSONArray(subKey);
                        for (int j = 0; j < wildCard.length(); j++) {
                            intermediate = wildCard.optJSONObject(j);
                            if (intermediate != null) {
                                // Found the original JSONObject we were looking for
                                break;
                            }
                        }
                    }

                    if (intermediate == null) {
                        ACRA.log.e(ACRA.LOG_TAG, "Unknown json subtree type, see issue #186");
                        // We should never get here, but if we do, drop this value to still send the report
                        return;
                    }
                }
                destination = intermediate;
            } else {
                destination.accumulate(subKey, value);
            }
        }
    }

    public static class JSONReportException extends Exception {
        private static final long serialVersionUID = -694684023635442219L;

        public JSONReportException(String message, Throwable e) {
            super(message, e);
        }
    };
}
