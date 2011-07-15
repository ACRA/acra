package org.acra;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.acra.util.Installation;
import org.acra.util.ReportUtils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Responsible for creating the CrashReportData for an Exception.
 * <p>
 *     Also responsible for holding the custom data to send with each report.
 * </p>
 * User: William
 * Date: 15/07/11
 * Time: 10:41 PM
 */
final class CrashReportDataFactory {

    private final Context context;
    private final List<ReportField> crashReportFields;
    private final Map<String, String> customParameters = new HashMap<String, String>();
    private final Time appStartDate;
    private final String initialConfiguration;

    CrashReportDataFactory(Context context, List<ReportField> crashReportFields, Time appStartDate, String initialConfiguration) {
        this.context = context;
        this.crashReportFields = crashReportFields;
        this.appStartDate = appStartDate;
        this.initialConfiguration = initialConfiguration;
    }

    /**
     * <p>
     * Adds a custom key and value to be reported with the generated CashReportData.
     * </p>
     * <p>
     * The key/value pairs will be stored in the "custom" column,
     * as a text containing one 'key = value' pair on each line.
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
     * @param key   The key of the data to be removed.
     * @return The value for this key before removal.
     */
    public String removeCustomData(String key) {
        return customParameters.remove(key);
    }

    /**
     * Gets the current value for a key in the custom data field.
     *
     * @param key   The key of the data to be retrieved.
     * @return The value for this key.
     */
    public String getCustomData(String key) {
        return customParameters.get(key);
    }

    /**
     * Collects crash data.
     *
     * @param th                Throwable that caused the crash.
     * @param isSilentReport    Whether to report this report as being sent silently.
     * @return CrashReportData representing the current state of the application at the instant of the Exception.
     */
    public CrashReportData createCrashData(Throwable th, boolean isSilentReport) {

        final CrashReportData crashReportData = new CrashReportData();
        try {
            final SharedPreferences prefs = ACRA.getACRASharedPreferences();

            crashReportData.put(ReportField.USER_APP_START_DATE, appStartDate.format3339(false));

            if (isSilentReport) {
                crashReportData.put(IS_SILENT, "true");
            }
            // Generate report uuid
            if (crashReportFields.contains(REPORT_ID)) {
                crashReportData.put(ReportField.REPORT_ID, UUID.randomUUID().toString());
            }

            // Collect meminfo
            if (crashReportFields.contains(DUMPSYS_MEMINFO)) {
                crashReportData.put(DUMPSYS_MEMINFO, DumpSysCollector.collectMemInfo());
            }

            final PackageManager pm = context.getPackageManager();

            // Collect DropBox and logcat
            if (pm != null) {
                if (prefs.getBoolean(ACRA.PREF_ENABLE_SYSTEM_LOGS, true)
                        && pm.checkPermission(Manifest.permission.READ_LOGS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    Log.i(ACRA.LOG_TAG, "READ_LOGS granted! ACRA can include LogCat and DropBox data.");
                    if (crashReportFields.contains(LOGCAT)) {
                        crashReportData.put(LOGCAT, LogCatCollector.collectLogCat(null));
                    }
                    if (crashReportFields.contains(EVENTSLOG)) {
                        crashReportData.put(EVENTSLOG, LogCatCollector.collectLogCat("events"));
                    }
                    if (crashReportFields.contains(RADIOLOG)) {
                        crashReportData.put(RADIOLOG, LogCatCollector.collectLogCat("radio"));
                    }
                    if (crashReportFields.contains(DROPBOX)) {
                        crashReportData.put(DROPBOX, DropBoxCollector.read(context, ACRA.getConfig().additionalDropBoxTags()));
                    }
                } else {
                    Log.i(ACRA.LOG_TAG, "READ_LOGS not allowed. ACRA will not include LogCat and DropBox data.");
                }

                // Retrieve UDID(IMEI) if permission is available
                if (crashReportFields.contains(DEVICE_ID)
                        && prefs.getBoolean(ACRA.PREF_ENABLE_DEVICE_ID, true)
                        && pm.checkPermission(Manifest.permission.READ_PHONE_STATE, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    final String deviceId = tm.getDeviceId();
                    if (deviceId != null) {
                        crashReportData.put(DEVICE_ID, deviceId);
                    }
                }
            }

            // Installation unique ID
            if (crashReportFields.contains(INSTALLATION_ID)) {
                crashReportData.put(INSTALLATION_ID, Installation.id(context));
            }

            // Device Configuration when crashing
            if (crashReportFields.contains(INITIAL_CONFIGURATION)) {
                crashReportData.put(INITIAL_CONFIGURATION, initialConfiguration);
            }
            if (crashReportFields.contains(CRASH_CONFIGURATION)) {
                final Configuration crashConf = context.getResources().getConfiguration();
                crashReportData.put(CRASH_CONFIGURATION, ConfigurationInspector.toString(crashConf));
            }

            final PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
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

            // Application Package name
            if (crashReportFields.contains(PACKAGE_NAME)) {
                crashReportData.put(PACKAGE_NAME, context.getPackageName());
            }

            // Android OS Build details
            if (crashReportFields.contains(BUILD)) {
                crashReportData.put(BUILD, ReflectionCollector.collectConstants(android.os.Build.class));
            }

            // Device model
            if (crashReportFields.contains(PHONE_MODEL)) {
                crashReportData.put(PHONE_MODEL, android.os.Build.MODEL);
            }
            // Android version
            if (crashReportFields.contains(ANDROID_VERSION)) {
                crashReportData.put(ANDROID_VERSION, android.os.Build.VERSION.RELEASE);
            }

            // Device Brand (manufacturer)
            if (crashReportFields.contains(BRAND)) {
                crashReportData.put(BRAND, android.os.Build.BRAND);
            }
            if (crashReportFields.contains(PRODUCT)) {
                crashReportData.put(PRODUCT, android.os.Build.PRODUCT);
            }

            // Device Memory
            if (crashReportFields.contains(TOTAL_MEM_SIZE)) {
                crashReportData.put(TOTAL_MEM_SIZE, Long.toString(ReportUtils.getTotalInternalMemorySize()));
            }
            if (crashReportFields.contains(AVAILABLE_MEM_SIZE)) {
                crashReportData.put(AVAILABLE_MEM_SIZE, Long.toString(ReportUtils.getAvailableInternalMemorySize()));
            }

            // Application file path
            if (crashReportFields.contains(FILE_PATH)) {
                crashReportData.put(FILE_PATH, context.getFilesDir().getAbsolutePath());
            }

            // Main display details
            if (crashReportFields.contains(DISPLAY)) {
                final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                crashReportData.put(DISPLAY, ReportUtils.getDisplayAsString(display));
            }

            // User crash date with local timezone
            if (crashReportFields.contains(USER_CRASH_DATE)) {
                final Time curDate = new Time();
                curDate.setToNow();
                crashReportData.put(USER_CRASH_DATE, curDate.format3339(false));
            }

            // Add custom info, they are all stored in a single field
            if (crashReportFields.contains(CUSTOM_DATA)) {
                crashReportData.put(CUSTOM_DATA, createCustomInfoString());
            }

            // Add user email address, if set in the app's preferences
            if (crashReportFields.contains(USER_EMAIL)) {
                crashReportData.put(USER_EMAIL, prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, "N/A"));
            }

            // Device features
            if (crashReportFields.contains(DEVICE_FEATURES)) {
                crashReportData.put(DEVICE_FEATURES, DeviceFeaturesCollector.getFeatures(context));
            }

            // Environment (External storage state)
            if (crashReportFields.contains(ENVIRONMENT)) {
                crashReportData.put(ENVIRONMENT, ReflectionCollector.collectStaticGettersResults(Environment.class));
            }

            // System settings
            if (crashReportFields.contains(SETTINGS_SYSTEM)) {
                crashReportData.put(SETTINGS_SYSTEM, SettingsCollector.collectSystemSettings(context));
            }

            // Secure settings
            if (crashReportFields.contains(SETTINGS_SECURE)) {
                crashReportData.put(SETTINGS_SECURE, SettingsCollector.collectSecureSettings(context));
            }

            // SharedPreferences
            if (crashReportFields.contains(SHARED_PREFERENCES)) {
                crashReportData.put(SHARED_PREFERENCES, SharedPreferencesCollector.collect(context));
            }

            crashReportData.put(STACK_TRACE, getStackTrace(th));

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while retrieving crash data", e);
        }

        return crashReportData;
    }

    /**
     * Generates the string which is posted in the single custom data field in the GoogleDocs Form.
     *
     * @return A string with a 'key = value' pair on each line.
     */
    private String createCustomInfoString() {
        final StringBuilder customInfo = new StringBuilder();
        for (final String currentKey : customParameters.keySet()) {
            final String currentVal = customParameters.get(currentKey);
            customInfo.append(currentKey);
            customInfo.append(" = ");
            customInfo.append(currentVal);
            customInfo.append("\n");
        }
        return customInfo.toString();
    }

    private String getStackTrace(Throwable th) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        th.printStackTrace(printWriter);

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = th.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }
}
