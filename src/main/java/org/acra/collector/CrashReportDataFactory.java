/*
 *  Copyright 2012 Kevin Gaudin
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

package org.acra.collector;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.ACRAConfiguration;
import org.acra.util.Installation;
import org.acra.util.PackageManagerWrapper;
import org.acra.util.ReportUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APPLICATION_LOG;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.BUILD;
import static org.acra.ReportField.BUILD_CONFIG;
import static org.acra.ReportField.CRASH_CONFIGURATION;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.DEVICE_FEATURES;
import static org.acra.ReportField.DEVICE_ID;
import static org.acra.ReportField.DISPLAY;
import static org.acra.ReportField.DROPBOX;
import static org.acra.ReportField.DUMPSYS_MEMINFO;
import static org.acra.ReportField.ENVIRONMENT;
import static org.acra.ReportField.EVENTSLOG;
import static org.acra.ReportField.FILE_PATH;
import static org.acra.ReportField.INITIAL_CONFIGURATION;
import static org.acra.ReportField.INSTALLATION_ID;
import static org.acra.ReportField.IS_SILENT;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.MEDIA_CODEC_LIST;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.RADIOLOG;
import static org.acra.ReportField.SETTINGS_GLOBAL;
import static org.acra.ReportField.SETTINGS_SECURE;
import static org.acra.ReportField.SETTINGS_SYSTEM;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.STACK_TRACE_HASH;
import static org.acra.ReportField.THREAD_DETAILS;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_EMAIL;
import static org.acra.ReportField.USER_IP;

/**
 * Responsible for creating the CrashReportData for an Exception.
 * <p>
 * Also responsible for holding the custom data to send with each report.
 * </p>
 *
 * @author William Ferguson
 * @since 4.3.0
 */
public final class CrashReportDataFactory {

    private final Context context;
    private final ACRAConfiguration config;
    private final SharedPreferences prefs;
    private final Map<String, String> customParameters = new LinkedHashMap<>();
    private final Calendar appStartDate;
    private final String initialConfiguration;

    public CrashReportDataFactory(Context context, ACRAConfiguration config,
                                  SharedPreferences prefs, Calendar appStartDate,
                                  String initialConfiguration) {
        this.context = context;
        this.config = config;
        this.prefs = prefs;
        this.appStartDate = appStartDate;
        this.initialConfiguration = initialConfiguration;
    }

    /**
     * <p>
     * Adds a custom key and value to be reported with the generated
     * CashReportData.
     * </p>
     * <p>
     * The key/value pairs will be stored in the "custom" column, as a text
     * containing one 'key = value' pair on each line.
     * </p>
     *
     * @param key   A key for your custom data.
     * @param value The value associated to your key.
     * @return The previous value for this key if there was one, or null.
     */
    public String putCustomData(String key, String value) {
        return customParameters.put(key, value);
    }

    /**
     * Removes a key/value pair from the custom data field.
     *
     * @param key The key of the data to be removed.
     * @return The value for this key before removal.
     */
    public String removeCustomData(String key) {
        return customParameters.remove(key);
    }

    /**
     * Removes all key/value pairs from the custom data field.
     */
    public void clearCustomData() {
        customParameters.clear();
    }

    /**
     * Gets the current value for a key in the custom data field.
     *
     * @param key The key of the data to be retrieved.
     * @return The value for this key.
     */
    public String getCustomData(String key) {
        return customParameters.get(key);
    }

    /**
     * Collects crash data.
     *
     * @param builder ReportBuilder for whom to crete the crash report.
     */
    @NonNull
    public CrashReportData createCrashData(@NonNull ReportBuilder builder) {
        final CrashReportData crashReportData = new CrashReportData();
        try {
            final List<ReportField> crashReportFields = config.getReportFields();

            // Make every entry here bullet proof and move any slightly dodgy
            // ones to the end.
            // This ensures that we collect as much info as possible before
            // something crashes the collection process.

            try {
                crashReportData.put(STACK_TRACE, getStackTrace(builder.getMessage(), builder.getException()));
            } catch (RuntimeException e) {
                ACRA.log.e(LOG_TAG, "Error while retrieving STACK_TRACE data", e);
            }

            // Collect DropBox and logcat. This is done first because some ROMs spam the log with every get on
            // Settings.
            final PackageManagerWrapper pm = new PackageManagerWrapper(context);

            // Before JellyBean, this required the READ_LOGS permission
            // Since JellyBean, READ_LOGS is not granted to third-party apps anymore for security reasons.
            // Though, we can call logcat without any permission and still get traces related to our app.
            final boolean hasReadLogsPermission = pm.hasPermission(Manifest.permission.READ_LOGS) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
            if (prefs.getBoolean(ACRA.PREF_ENABLE_SYSTEM_LOGS, true) && hasReadLogsPermission) {
                if (ACRA.DEV_LOGGING)
                    ACRA.log.d(LOG_TAG, "READ_LOGS granted! ACRA can include LogCat and DropBox data.");
                final LogCatCollector logCatCollector = new LogCatCollector();
                if (crashReportFields.contains(LOGCAT)) {
                    try {
                        crashReportData.put(LOGCAT, logCatCollector.collectLogCat(config, null));
                    } catch (RuntimeException e) {
                        ACRA.log.e(LOG_TAG, "Error while retrieving LOGCAT data", e);
                    }
                }
                if (crashReportFields.contains(EVENTSLOG)) {
                    try {
                        crashReportData.put(EVENTSLOG, logCatCollector.collectLogCat(config, "events"));
                    } catch (RuntimeException e) {
                        ACRA.log.e(LOG_TAG, "Error while retrieving EVENTSLOG data", e);
                    }
                }
                if (crashReportFields.contains(RADIOLOG)) {
                    try {
                        crashReportData.put(RADIOLOG, logCatCollector.collectLogCat(config, "radio"));
                    } catch (RuntimeException e) {
                        ACRA.log.e(LOG_TAG, "Error while retrieving RADIOLOG data", e);
                    }
                }
                if (crashReportFields.contains(DROPBOX)) {
                    try {
                        crashReportData.put(DROPBOX, new DropBoxCollector().read(context, config));
                    } catch (RuntimeException e) {
                        ACRA.log.e(LOG_TAG, "Error while retrieving DROPBOX data", e);
                    }
                }
            } else {
                if (ACRA.DEV_LOGGING)
                    ACRA.log.d(LOG_TAG, "READ_LOGS not allowed. ACRA will not include LogCat and DropBox data.");
            }

            try {
                crashReportData.put(ReportField.USER_APP_START_DATE, ReportUtils.getTimeString(appStartDate));
            } catch (RuntimeException e) {
                ACRA.log.e(LOG_TAG, "Error while retrieving USER_APP_START_DATE data", e);
            }

            if (builder.isSendSilently()) {
                crashReportData.put(IS_SILENT, "true");
            }

            // Always generate report uuid
            try {
                crashReportData.put(ReportField.REPORT_ID, UUID.randomUUID().toString());
            } catch (RuntimeException e) {
                ACRA.log.e(LOG_TAG, "Error while retrieving REPORT_ID data", e);
            }

            // Always generate crash time
            try {
                final Calendar curDate = new GregorianCalendar();
                crashReportData.put(ReportField.USER_CRASH_DATE, ReportUtils.getTimeString(curDate));
            } catch (RuntimeException e) {
                ACRA.log.e(LOG_TAG, "Error while retrieving USER_CRASH_DATE data", e);
            }

            // StackTrace hash
            if (crashReportFields.contains(STACK_TRACE_HASH)) {
                try {
                    crashReportData.put(ReportField.STACK_TRACE_HASH, getStackTraceHash(builder.getException()));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving STACK_TRACE_HASH data", e);
                }
            }

            // Installation unique ID
            if (crashReportFields.contains(INSTALLATION_ID)) {
                try {
                    crashReportData.put(INSTALLATION_ID, Installation.id(context));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving INSTALLATION_ID data", e);
                }
            }

            // Device Configuration when crashing
            if (crashReportFields.contains(INITIAL_CONFIGURATION)) {
                try {
                    crashReportData.put(INITIAL_CONFIGURATION, initialConfiguration);
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving INITIAL_CONFIGURATION data", e);
                }
            }
            if (crashReportFields.contains(CRASH_CONFIGURATION)) {
                try {
                    crashReportData.put(CRASH_CONFIGURATION, ConfigurationCollector.collectConfiguration(context));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving CRASH_CONFIGURATION data", e);
                }
            }

            // Collect meminfo
            if (!(builder.getException() instanceof OutOfMemoryError) && crashReportFields.contains(DUMPSYS_MEMINFO)) {
                try {
                    crashReportData.put(DUMPSYS_MEMINFO, DumpSysCollector.collectMemInfo());
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving DUMPSYS_MEMINFO data", e);
                }
            }

            // Application Package name
            if (crashReportFields.contains(PACKAGE_NAME)) {
                try {
                    crashReportData.put(PACKAGE_NAME, context.getPackageName());
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving PACKAGE_NAME data", e);
                }
            }

            // Android OS Build details
            if (crashReportFields.contains(BUILD)) {
                try {
                    crashReportData.put(BUILD, ReflectionCollector.collectConstants(android.os.Build.class) + ReflectionCollector.collectConstants(android.os.Build.VERSION.class, "VERSION"));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving BUILD data", e);
                }
            }

            // Device model
            if (crashReportFields.contains(PHONE_MODEL)) {
                try {
                    crashReportData.put(PHONE_MODEL, android.os.Build.MODEL);
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving PHONE_MODEL data", e);
                }
            }
            // Android version
            if (crashReportFields.contains(ANDROID_VERSION)) {
                try {
                    crashReportData.put(ANDROID_VERSION, android.os.Build.VERSION.RELEASE);
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving ANDROID_VERSION data", e);
                }
            }

            // Device Brand (manufacturer)
            if (crashReportFields.contains(BRAND)) {
                try {
                    crashReportData.put(BRAND, android.os.Build.BRAND);
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving BRAND data", e);
                }
            }
            if (crashReportFields.contains(PRODUCT)) {
                try {
                    crashReportData.put(PRODUCT, android.os.Build.PRODUCT);
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving PRODUCT data", e);
                }
            }

            // Device Memory
            if (crashReportFields.contains(TOTAL_MEM_SIZE)) {
                try {
                    crashReportData.put(TOTAL_MEM_SIZE, Long.toString(ReportUtils.getTotalInternalMemorySize()));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving TOTAL_MEM_SIZE data", e);
                }
            }
            if (crashReportFields.contains(AVAILABLE_MEM_SIZE)) {
                try {
                    crashReportData.put(AVAILABLE_MEM_SIZE, Long.toString(ReportUtils.getAvailableInternalMemorySize()));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving AVAILABLE_MEM_SIZE data", e);
                }
            }

            // Application file path
            if (crashReportFields.contains(FILE_PATH)) {
                try {
                    crashReportData.put(FILE_PATH, ReportUtils.getApplicationFilePath(context));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving FILE_PATH data", e);
                }
            }

            // Main display details
            if (crashReportFields.contains(DISPLAY)) {
                try {
                    crashReportData.put(DISPLAY, DisplayManagerCollector.collectDisplays(context));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving DISPLAY data", e);
                }
            }

            // Add custom info, they are all stored in a single field
            if (crashReportFields.contains(CUSTOM_DATA)) {
                try {
                    crashReportData.put(CUSTOM_DATA, createCustomInfoString(builder.getCustomData()));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving CUSTOM_DATA data", e);
                }
            }

            if (crashReportFields.contains(BUILD_CONFIG)) {
                try {
                    final Class buildConfigClass = getBuildConfigClass();
                    if (buildConfigClass != null) {
                        crashReportData.put(BUILD_CONFIG, ReflectionCollector.collectConstants(buildConfigClass));
                    }
                } catch (ClassNotFoundException ignored) {
                    // We have already logged this when we had the name of the class that wasn't found.
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving BUILD_CONFIG data", e);
                }
            }

            // Add user email address, if set in the app's preferences
            if (crashReportFields.contains(USER_EMAIL)) {
                try {
                    crashReportData.put(USER_EMAIL, prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, "N/A"));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving USER_EMAIL data", e);
                }
            }

            // Device features
            if (crashReportFields.contains(DEVICE_FEATURES)) {
                try {
                    crashReportData.put(DEVICE_FEATURES, DeviceFeaturesCollector.getFeatures(context));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving DEVICE_FEATURES data", e);
                }
            }

            // Environment (External storage state)
            if (crashReportFields.contains(ENVIRONMENT)) {
                try {
                    crashReportData.put(ENVIRONMENT, ReflectionCollector.collectStaticGettersResults(Environment.class));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving ENVIRONMENT data", e);
                }
            }

            final SettingsCollector settingsCollector = new SettingsCollector(context, config);
            // System settings
            if (crashReportFields.contains(SETTINGS_SYSTEM)) {
                try {
                    crashReportData.put(SETTINGS_SYSTEM, settingsCollector.collectSystemSettings());
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving SETTINGS_SYSTEM data", e);
                }
            }

            // Secure settings
            if (crashReportFields.contains(SETTINGS_SECURE)) {
                try {
                    crashReportData.put(SETTINGS_SECURE, settingsCollector.collectSecureSettings());
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving SETTINGS_SECURE data", e);
                }
            }

            // Global settings
            if (crashReportFields.contains(SETTINGS_GLOBAL)) {
                try {

                    crashReportData.put(SETTINGS_GLOBAL, settingsCollector.collectGlobalSettings());
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving SETTINGS_GLOBAL data", e);
                }
            }

            // SharedPreferences
            if (crashReportFields.contains(SHARED_PREFERENCES)) {
                try {
                    crashReportData.put(SHARED_PREFERENCES, new SharedPreferencesCollector(context, config).collect());
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving SHARED_PREFERENCES data", e);
                }
            }

            // Now get all the crash data that relies on the PackageManager.getPackageInfo()
            // (which may or may not be here).
            try {
                final PackageInfo pi = pm.getPackageInfo();
                if (pi != null) {
                    // Application Version
                    if (crashReportFields.contains(APP_VERSION_CODE)) {
                        crashReportData.put(APP_VERSION_CODE, Integer.toString(pi.versionCode));
                    }
                    if (crashReportFields.contains(APP_VERSION_NAME)) {
                        crashReportData.put(APP_VERSION_NAME, pi.versionName != null ? pi.versionName : "not set");
                    }
                } else {
                    // Could not retrieve package info...
                    crashReportData.put(APP_VERSION_NAME, "Package info unavailable");
                }
            } catch (RuntimeException e) {
                ACRA.log.e(LOG_TAG, "Error while retrieving APP_VERSION_CODE and APP_VERSION_NAME data", e);
            }

            // Retrieve UDID(IMEI) if permission is available
            if (crashReportFields.contains(DEVICE_ID) && prefs.getBoolean(ACRA.PREF_ENABLE_DEVICE_ID, true)
                    && pm.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
                try {
                    final String deviceId = ReportUtils.getDeviceId(context);
                    if (deviceId != null) {
                        crashReportData.put(DEVICE_ID, deviceId);
                    }
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving DEVICE_ID data", e);
                }
            }

            // Application specific log file
            if (crashReportFields.contains(APPLICATION_LOG)) {
                try {
                    final String logFile = new LogFileCollector().collectLogFile(context, config.applicationLogFile(), config.applicationLogFileLines());
                    crashReportData.put(APPLICATION_LOG, logFile);
                } catch (IOException e) {
                    ACRA.log.e(LOG_TAG, "Error while reading application log file " + config.applicationLogFile(), e);
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving APPLICATION_LOG data", e);

                }
            }

            // Media Codecs list
            if (crashReportFields.contains(MEDIA_CODEC_LIST)) {
                try {
                    crashReportData.put(MEDIA_CODEC_LIST, MediaCodecListCollector.collectMediaCodecList());
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving MEDIA_CODEC_LIST data", e);
                }
            }

            // Failing thread details
            if (crashReportFields.contains(THREAD_DETAILS)) {
                try {
                    crashReportData.put(THREAD_DETAILS, ThreadCollector.collect(builder.getUncaughtExceptionThread()));
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving THREAD_DETAILS data", e);
                }
            }

            // IP addresses
            if (crashReportFields.contains(USER_IP)) {
                try {
                    crashReportData.put(USER_IP, ReportUtils.getLocalIpAddress());
                } catch (RuntimeException e) {
                    ACRA.log.e(LOG_TAG, "Error while retrieving USER_IP data", e);
                }
            }

        } catch (RuntimeException e) {
            ACRA.log.e(LOG_TAG, "Error while retrieving crash data", e);
        }

        return crashReportData;
    }

    /**
     * Generates the string which is posted in the single custom data field in
     * the GoogleDocs Form.
     *
     * @return A string with a 'key = value' pair on each line.
     */
    private String createCustomInfoString(@Nullable Map<String, String> reportCustomData) {
        Map<String, String> params = customParameters;

        if (reportCustomData != null) {
            params = new HashMap<>(params);
            params.putAll(reportCustomData);
        }

        final StringBuilder customInfo = new StringBuilder();
        for (final String currentKey : params.keySet()) {
            String currentVal = params.get(currentKey);
            customInfo.append(currentKey);
            customInfo.append(" = ");
            // We need to escape new lines in values or they are transformed into new
            // custom fields. => let's replace all '\n' with "\\n"
            if (currentVal != null) {
                currentVal = currentVal.replaceAll("\n", "\\\\n");
            }
            customInfo.append(currentVal);
            customInfo.append("\n");
        }
        return customInfo.toString();
    }

    private String getStackTrace(@Nullable String msg, Throwable th) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        if (msg != null && !TextUtils.isEmpty(msg)) {
            printWriter.println(msg);
        }

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }

    private String getStackTraceHash(Throwable th) {
        final StringBuilder res = new StringBuilder();
        Throwable cause = th;
        while (cause != null) {
            final StackTraceElement[] stackTraceElements = cause.getStackTrace();
            for (final StackTraceElement e : stackTraceElements) {
                res.append(e.getClassName());
                res.append(e.getMethodName());
            }
            cause = cause.getCause();
        }

        return Integer.toHexString(res.toString().hashCode());
    }

    @Nullable
    private Class<?> getBuildConfigClass() throws ClassNotFoundException {
        final Class configuredBuildConfig = config.buildConfigClass();
        if ((configuredBuildConfig != null) && !configuredBuildConfig.equals(Object.class)) {
            // If set via annotations or programatically then it will have a real value,
            // otherwise it will be Object.class (annotation default) or null (explicit programmatic).
            return configuredBuildConfig;
        }

        final String className = context.getPackageName() + ".BuildConfig";
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            ACRA.log.e(LOG_TAG, "Not adding buildConfig to log. Class Not found : " + className + ". Please configure 'buildConfigClass' in your ACRA config");
            throw e;
        }
    }
}