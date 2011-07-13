/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
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
package org.acra;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.acra.annotation.ReportsCrashes;
import org.acra.sender.ReportSender;
import org.acra.util.Installation;
import org.acra.util.ReportUtils;

import android.Manifest.permission;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * <p>
 * The ErrorReporter is a Singleton object in charge of collecting crash context data and sending crash reports. It
 * registers itself as the Application's Thread default {@link UncaughtExceptionHandler}.
 * </p>
 * <p>
 * When a crash occurs, it collects data of the crash context (device, system, stack trace...) and writes a report file
 * in the application private directory. This report file is then sent :
 * <ul>
 * <li>immediately if {@link #mReportingInteractionMode} is set to {@link ReportingInteractionMode#SILENT} or
 * {@link ReportingInteractionMode#TOAST},</li>
 * <li>on application start if in the previous case the transmission could not technically be made,</li>
 * <li>when the user accepts to send it if {@link #mReportingInteractionMode} is set to
 * {@link ReportingInteractionMode#NOTIFICATION}.</li>
 * </ul>
 * </p>
 * <p>
 * If an error occurs while sending a report, it is kept for later attempts.
 * </p>
 */
public class ErrorReporter implements Thread.UncaughtExceptionHandler {

    // Our singleton instance.
    private static ErrorReporter mInstanceSingleton;


    // TODO Separate out global crash report data from instance data. Don't want to pollute the data of 2 instances.
    // This is where we collect crash data
    private static CrashReportData mCrashProperties = new CrashReportData();



    // The application context
    private Context mContext;

    private boolean enabled = false;

    /**
     * Contains the active {@link ReportSender}s.
     */
    private final List<ReportSender> mReportSenders = new ArrayList<ReportSender>();

    // Some custom parameters can be added by the application developer. These
    // parameters are stored here.
    private final Map<String, String> mCustomParameters = new HashMap<String, String>();

    private final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();

    // A reference to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the default exception handling after sending
    // the report.
    private Thread.UncaughtExceptionHandler mDfltExceptionHandler;

    // The Configuration obtained on application start.
    private String mInitialConfiguration;

    // User interaction mode defined by the application developer.
    private ReportingInteractionMode mReportingInteractionMode = ReportingInteractionMode.SILENT;

    /**
     * Deprecated. Use {@link #putCustomData(String, String)}.
     * 
     * @param key   A key for your custom data.
     * @param value The value associated to your key.
     */
    @Deprecated
    public void addCustomData(String key, String value) {
        mCustomParameters.put(key, value);
    }

    /**
     * <p>
     * Use this method to provide the ErrorReporter with data of your running application. You should call this at
     * several key places in your code the same way as you would output important debug data in a log file. Only the
     * latest value is kept for each key (no history of the values is sent in the report).
     * </p>
     * <p>
     * The key/value pairs will be stored in the GoogleDoc spreadsheet in the "custom" column, as a text containing a
     * 'key = value' pair on each line.
     * </p>
     * 
     * @param key   A key for your custom data.
     * @param value The value associated to your key.
     * @return The previous value for this key if there was one, or null.
     * @see #removeCustomData(String)
     * @see #getCustomData(String)
     */
    public String putCustomData(String key, String value) {
        return mCustomParameters.put(key, value);
    }

    /**
     * Removes a key/value pair from your reports custom data field.
     * 
     * @param key   The key of the data to be removed.
     * @return The value for this key before removal.
     * @see #putCustomData(String, String)
     * @see #getCustomData(String)
     */
    public String removeCustomData(String key) {
        return mCustomParameters.remove(key);
    }

    /**
     * Gets the current value for a key in your reports custom data field.
     * 
     * @param key   The key of the data to be retrieved.
     * @return The value for this key.
     * @see #putCustomData(String, String)
     * @see #removeCustomData(String)
     */
    public String getCustomData(String key) {
        return mCustomParameters.get(key);
    }

    /**
     * Generates the string which is posted in the single custom data field in the GoogleDocs Form.
     * 
     * @return A string with a 'key = value' pair on each line.
     */
    private String createCustomInfoString() {
        final StringBuilder customInfo = new StringBuilder();
        for (final String currentKey : mCustomParameters.keySet()) {
            final String currentVal = mCustomParameters.get(currentKey);
            customInfo.append(currentKey);
            customInfo.append(" = ");
            customInfo.append(currentVal);
            customInfo.append("\n");
        }
        return customInfo.toString();
    }

    /**
     * Create or return the singleton instance.
     * 
     * @return the current instance of ErrorReporter.
     */
    public static synchronized ErrorReporter getInstance() {
        if (mInstanceSingleton == null) {
            mInstanceSingleton = new ErrorReporter();
        }
        return mInstanceSingleton;
    }

    /**
     * <p>
     * This is where the ErrorReporter replaces the default {@link UncaughtExceptionHandler}.
     * </p>
     * 
     * @param context
     *            The android application context.
     */
    public void init(Context context) {
        // If mDfltExceptionHandler is not null, initialization is already done.
        // Don't do it twice to avoid losing the original handler.
        if (mDfltExceptionHandler == null) {
            mDfltExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            enabled = true;
            Thread.setDefaultUncaughtExceptionHandler(this);
            mContext = context;
            // Store the initial Configuration state.
            mInitialConfiguration = ConfigurationInspector.toString(mContext.getResources().getConfiguration());
        }
    }

    /**
     * Collects crash data.
     * 
     * @param context   The application context.
     */
    private void retrieveCrashData(Context context) {
        try {
            final List<ReportField> fieldsList = Arrays.asList(getCrashReportFields());
            final SharedPreferences prefs = ACRA.getACRASharedPreferences();

            // Generate report uuid
            if (fieldsList.contains(REPORT_ID)) {
                mCrashProperties.put(ReportField.REPORT_ID, UUID.randomUUID().toString());
            }

            // Collect meminfo
            if (fieldsList.contains(DUMPSYS_MEMINFO)) {
                mCrashProperties.put(DUMPSYS_MEMINFO, DumpSysCollector.collectMemInfo());
            }

            final PackageManager pm = context.getPackageManager();

            // Collect DropBox and logcat
            if (pm != null) {
                if (prefs.getBoolean(ACRA.PREF_ENABLE_SYSTEM_LOGS, true)
                        && pm.checkPermission(permission.READ_LOGS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    Log.i(ACRA.LOG_TAG, "READ_LOGS granted! ACRA can include LogCat and DropBox data.");
                    if (fieldsList.contains(LOGCAT)) {
                        mCrashProperties.put(LOGCAT, LogCatCollector.collectLogCat(null));
                    }
                    if (fieldsList.contains(EVENTSLOG)) {
                        mCrashProperties.put(EVENTSLOG, LogCatCollector.collectLogCat("events"));
                    }
                    if (fieldsList.contains(RADIOLOG)) {
                        mCrashProperties.put(RADIOLOG, LogCatCollector.collectLogCat("radio"));
                    }
                    if (fieldsList.contains(DROPBOX)) {
                        mCrashProperties.put(DROPBOX,
                                DropBoxCollector.read(mContext, ACRA.getConfig().additionalDropBoxTags()));
                    }
                } else {
                    Log.i(ACRA.LOG_TAG, "READ_LOGS not allowed. ACRA will not include LogCat and DropBox data.");
                }

                // Retrieve UDID(IMEI) if permission is available
                if (fieldsList.contains(DEVICE_ID)
                        && prefs.getBoolean(ACRA.PREF_ENABLE_DEVICE_ID, true)
                        && pm.checkPermission(permission.READ_PHONE_STATE, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    final String deviceId = tm.getDeviceId();
                    if (deviceId != null) {
                        mCrashProperties.put(DEVICE_ID, deviceId);
                    }
                }
            }

            // Installation unique ID
            if (fieldsList.contains(INSTALLATION_ID)) {
                mCrashProperties.put(INSTALLATION_ID, Installation.id(mContext));
            }

            // Device Configuration when crashing
            if (fieldsList.contains(INITIAL_CONFIGURATION)) {
                mCrashProperties.put(INITIAL_CONFIGURATION, mInitialConfiguration);
            }
            if (fieldsList.contains(CRASH_CONFIGURATION)) {
                Configuration crashConf = context.getResources().getConfiguration();
                mCrashProperties.put(CRASH_CONFIGURATION, ConfigurationInspector.toString(crashConf));
            }

            final PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (pi != null) {
                // Application Version
                if (fieldsList.contains(APP_VERSION_CODE)) {
                    mCrashProperties.put(APP_VERSION_CODE, Integer.toString(pi.versionCode));
                }
                if (fieldsList.contains(APP_VERSION_NAME)) {
                    mCrashProperties.put(APP_VERSION_NAME, pi.versionName != null ? pi.versionName : "not set");
                }
            } else {
                // Could not retrieve package info...
                mCrashProperties.put(APP_VERSION_NAME, "Package info unavailable");
            }

            // Application Package name
            if (fieldsList.contains(PACKAGE_NAME)) {
                mCrashProperties.put(PACKAGE_NAME, context.getPackageName());
            }

            // Android OS Build details
            if (fieldsList.contains(BUILD)) {
                mCrashProperties.put(BUILD, ReflectionCollector.collectConstants(android.os.Build.class));
            }

            // Device model
            if (fieldsList.contains(PHONE_MODEL)) {
                mCrashProperties.put(PHONE_MODEL, android.os.Build.MODEL);
            }
            // Android version
            if (fieldsList.contains(ANDROID_VERSION)) {
                mCrashProperties.put(ANDROID_VERSION, android.os.Build.VERSION.RELEASE);
            }

            // Device Brand (manufacturer)
            if (fieldsList.contains(BRAND)) {
                mCrashProperties.put(BRAND, android.os.Build.BRAND);
            }
            if (fieldsList.contains(PRODUCT)) {
                mCrashProperties.put(PRODUCT, android.os.Build.PRODUCT);
            }

            // Device Memory
            if (fieldsList.contains(TOTAL_MEM_SIZE)) {
                mCrashProperties.put(TOTAL_MEM_SIZE, Long.toString(ReportUtils.getTotalInternalMemorySize()));
            }
            if (fieldsList.contains(AVAILABLE_MEM_SIZE)) {
                mCrashProperties.put(AVAILABLE_MEM_SIZE, Long.toString(ReportUtils.getAvailableInternalMemorySize()));
            }

            // Application file path
            if (fieldsList.contains(FILE_PATH)) {
                mCrashProperties.put(FILE_PATH, context.getFilesDir().getAbsolutePath());
            }

            // Main display details
            if (fieldsList.contains(DISPLAY)) {
                final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                mCrashProperties.put(DISPLAY, ReportUtils.getDisplayAsString(display));
            }

            // User crash date with local timezone
            if (fieldsList.contains(USER_CRASH_DATE)) {
                final Time curDate = new Time();
                curDate.setToNow();
                mCrashProperties.put(USER_CRASH_DATE, curDate.format3339(false));
            }

            // Add custom info, they are all stored in a single field
            if (fieldsList.contains(CUSTOM_DATA)) {
                mCrashProperties.put(CUSTOM_DATA, createCustomInfoString());
            }

            // Add user email address, if set in the app's preferences
            if (fieldsList.contains(USER_EMAIL)) {
                mCrashProperties.put(USER_EMAIL, prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, "N/A"));
            }

            // Device features
            if (fieldsList.contains(DEVICE_FEATURES)) {
                mCrashProperties.put(DEVICE_FEATURES, DeviceFeaturesCollector.getFeatures(context));
            }

            // Environment (External storage state)
            if (fieldsList.contains(ENVIRONMENT)) {
                mCrashProperties.put(ENVIRONMENT, ReflectionCollector.collectStaticGettersResults(Environment.class));
            }

            // System settings
            if (fieldsList.contains(SETTINGS_SYSTEM)) {
                mCrashProperties.put(SETTINGS_SYSTEM, SettingsCollector.collectSystemSettings(mContext));
            }

            // Secure settings
            if (fieldsList.contains(SETTINGS_SECURE)) {
                mCrashProperties.put(SETTINGS_SECURE, SettingsCollector.collectSecureSettings(mContext));
            }

            // SharedPreferences
            if (fieldsList.contains(SHARED_PREFERENCES)) {
                mCrashProperties.put(SHARED_PREFERENCES, SharedPreferencesCollector.collect(mContext));
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while retrieving crash data", e);
        }
    }

    /**
     * @return Array of ReportField to use for a crash.
     */
    private ReportField[] getCrashReportFields() {
        // TODO decide this once during #init and store it as List of ReportField
        final ReportsCrashes config = ACRA.getConfig();
        final ReportField[] customReportFields = config.customReportContent();
        if (customReportFields.length != 0) {
            Log.d(LOG_TAG, "Using custom Report Fields");
            return customReportFields;
        }

        if (config.mailTo() == null || "".equals(config.mailTo())) {
            Log.d(LOG_TAG, "Using default Report Fields");
            return ACRA.DEFAULT_REPORT_FIELDS;
        }

        Log.d(LOG_TAG, "Using default Mail Report Fields");
        return ACRA.DEFAULT_MAIL_REPORT_FIELDS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang .Thread, java.lang.Throwable)
     */
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(ACRA.LOG_TAG,
                "ACRA caught a " + e.getClass().getSimpleName() + " exception for " + mContext.getPackageName()
                        + ". Building report.");

        // This is a real exception, clear the IS_SILENT field from any previous silent exception
        mCrashProperties.remove(IS_SILENT);

        // Generate and send crash report
        final Thread worker = handleException(e, mReportingInteractionMode);

        if (mReportingInteractionMode == ReportingInteractionMode.TOAST) {
            try {
                // Wait a bit to let the user read the toast
                Thread.sleep(4000);
            } catch (InterruptedException e1) {
                Log.e(LOG_TAG, "Error : ", e1);
            }
        }

        if (worker != null) {
            while (worker.isAlive()) { // TODO replace with worker.join();
                try {
                    // Wait for the report sender to finish it's task before
                    // killing the process
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    Log.e(LOG_TAG, "Error : ", e1);
                }
            }
        }

        if (mReportingInteractionMode == ReportingInteractionMode.SILENT
                || (mReportingInteractionMode == ReportingInteractionMode.TOAST && ACRA.getConfig()
                        .forceCloseDialogAfterToast())) {
            // If using silent mode, let the system default handler do it's job
            // and display the force close dialog.
            mDfltExceptionHandler.uncaughtException(t, e);
        } else {
            // If ACRA handles user notifications with a Toast or a Notification
            // the Force Close dialog is one more notification to the user...
            // We choose to close the process ourselves using the same actions.
            try {
                final PackageManager pm = mContext.getPackageManager();
                final CharSequence appName = pm.getApplicationInfo(mContext.getPackageName(), 0).loadLabel(mContext.getPackageManager());
                Log.e(LOG_TAG, appName + " fatal error : " + e.getMessage(), e);
            } catch (NameNotFoundException e2) {
                Log.e(LOG_TAG, "Error : ", e2);
            } finally {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        }
    }

    /**
     * Try to send a report, if an error occurs stores a report file for a later attempt.
     *
     * @param e                         Throwable to be reported.
     *                                  If null the report will contain a new Exception("Report requested by developer").
     * @param reportingInteractionMode  The desired interaction mode.
     * @return A running ReportsSenderWorker that is sending the reports if the interaction mode is silent, toast
     *  or the always accept preference is true, otherwise returns null.
     */
    private SendWorker handleException(Throwable e, ReportingInteractionMode reportingInteractionMode) {

        boolean sendOnlySilentReports = false;
        if (reportingInteractionMode == null) {
            // No interaction mode defined, we assume it has been set during ACRA.initACRA()
            reportingInteractionMode = mReportingInteractionMode;
        } else {
            // An interaction mode has been provided. If ACRA has been
            // initialized with a non SILENT mode and this mode is overridden
            // with SILENT, then we have to send only reports which have been
            // explicitly declared as silent via handleSilentException().
            if (reportingInteractionMode == ReportingInteractionMode.SILENT
                    && mReportingInteractionMode != ReportingInteractionMode.SILENT) {
                sendOnlySilentReports = true;
            }
        }

        if (e == null) {
            e = new Exception("Report requested by developer");
        }

        if (reportingInteractionMode == ReportingInteractionMode.TOAST
                || (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION && ACRA.getConfig()
                        .resToastText() != 0)) {
            new Thread() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(mContext, ACRA.getConfig().resToastText(), Toast.LENGTH_LONG).show();
                    Looper.loop();
                }

            }.start();
        }

        retrieveCrashData(mContext);

        // Build stack trace
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        Log.getStackTraceString(e);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        mCrashProperties.put(STACK_TRACE, result.toString());
        printWriter.close();

        // Always write the report file

        final String reportFileName = getReportFileName(mCrashProperties);
        saveCrashReportFile(reportFileName, mCrashProperties);

        // Remove IS_SILENT if it was set, or it will persist in the next non-silent report
        mCrashProperties.remove(IS_SILENT);
        mCrashProperties.remove(USER_COMMENT);

        if (reportingInteractionMode == ReportingInteractionMode.SILENT
                || reportingInteractionMode == ReportingInteractionMode.TOAST
                || ACRA.getACRASharedPreferences().getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {

            // Approve and then send reports now
            Log.v(ACRA.LOG_TAG, "About to start ReportSenderWorker from #handleException");
            return startSendingReports(sendOnlySilentReports, true);
        } else if (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
            // Send reports when user accepts
            notifySendReport(reportFileName);
        }
        return null;
    }

    public SendWorker startSendingReports(boolean onlySendSilentReports, boolean approveReportsFirst) {
        final SendWorker worker = new SendWorker(mContext, mReportSenders, onlySendSilentReports, approveReportsFirst);
        worker.start();
        return worker;
    }

    /**
     * Send a report for this {@link Throwable} silently (forces the use of {@link ReportingInteractionMode#SILENT} for
     * this report, whatever is the mode set for the application. Very useful for tracking difficult defects.
     * 
     * @param e The {@link Throwable} to be reported.
     *          If null the report will contain a new Exception("Report requested by developer").
     * @return The Thread which has been created to send the report or null if ACRA is disabled.
     */
    public Thread handleSilentException(Throwable e) {
        // Mark this report as silent.
        if (enabled) {
            mCrashProperties.put(IS_SILENT, "true");
            return handleException(e, ReportingInteractionMode.SILENT);
        } else {
            Log.d(LOG_TAG, "ACRA is disabled. Silent report not sent.");
            return null;
        }
    }

    /**
     * Send a status bar notification. The action triggered when the notification is selected is to start the
     * {@link CrashReportDialog} Activity.
     *
     * @param reportFileName    Name of the report file to send.
     */
    void notifySendReport(String reportFileName) {
        // This notification can't be set to AUTO_CANCEL because after a crash,
        // clicking on it restarts the application and this triggers a check
        // for pending reports which issues the notification back.
        // Notification cancellation is done in the dialog activity displayed
        // on notification click.
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        final ReportsCrashes conf = ACRA.getConfig();

        // Default notification icon is the warning symbol
        final int icon = conf.resNotifIcon();

        final CharSequence tickerText = mContext.getText(conf.resNotifTickerText());
        final long when = System.currentTimeMillis();
        final Notification notification = new Notification(icon, tickerText, when);

        final CharSequence contentTitle = mContext.getText(conf.resNotifTitle());
        final CharSequence contentText = mContext.getText(conf.resNotifText());

        final Intent notificationIntent = new Intent(mContext, CrashReportDialog.class);
        Log.d(LOG_TAG, "Creating Notification for " + reportFileName);
        notificationIntent.putExtra(ACRAConstants.EXTRA_REPORT_FILE_NAME, reportFileName);
        final PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);

        // Send new notification
        notificationManager.cancelAll();
        notificationManager.notify(ACRA.NOTIF_CRASH_ID, notification);
    }

    private String getReportFileName(CrashReportData crashData) {
        final Time now = new Time();
        now.setToNow();
        final long timestamp = now.toMillis(false);
        final String isSilent = crashData.getProperty(IS_SILENT);
        return "" + timestamp + (isSilent != null ? ACRAConstants.SILENT_SUFFIX : "") + ACRAConstants.REPORTFILE_EXTENSION;
    }

    /**
     * When a report can't be sent, it is saved here in a file in the root of the application private directory.
     * 
     * @param fileName
     *            In a few rare cases, we write the report again with additional data (user comment for example). In
     *            such cases, you can provide the already existing file name here to overwrite the report file. If null,
     *            a new file report will be generated
     * @param crashData
     *            Can be used to save an alternative (or previously generated) report data. Used to store again a report
     *            with the addition of user comment. If null, the default current crash data are used.
     */
    private void saveCrashReportFile(String fileName, CrashReportData crashData) {
        try {
            Log.d(LOG_TAG, "Writing crash report file.");
            final CrashReportPersister persister = new CrashReportPersister(mContext);
            persister.store(crashData, fileName);
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred while writing the report file...", e);
        }
    }


    /**
     * Set the wanted user interaction mode for sending reports.
     * 
     * @param reportingInteractionMode  ReportingInteractionMode to use with this ErrorReporter.
     */
    void setReportingInteractionMode(ReportingInteractionMode reportingInteractionMode) {
        mReportingInteractionMode = reportingInteractionMode;
    }

    /**
     * This method looks for pending reports and does the action required depending on the interaction mode set.
     */
    public void checkReportsOnApplicationStart() {
        final CrashReportFinder reportFinder = new CrashReportFinder(mContext);
        final String[] filesList = reportFinder.getCrashReportFiles();
        if (filesList != null && filesList.length > 0) {
            final boolean onlySilentOrApprovedReports = containsOnlySilentOrApprovedReports(filesList);
            // Immediately send reports for SILENT and TOAST modes.
            // Immediately send reports in NOTIFICATION mode only if they are
            // all silent or approved.
            if (mReportingInteractionMode == ReportingInteractionMode.SILENT
                    || mReportingInteractionMode == ReportingInteractionMode.TOAST
                    || (mReportingInteractionMode == ReportingInteractionMode.NOTIFICATION && onlySilentOrApprovedReports)) {

                if (mReportingInteractionMode == ReportingInteractionMode.TOAST && !onlySilentOrApprovedReports) {
                    // Display the Toast in TOAST mode only if there are
                    // non-silent reports.
                    Toast.makeText(mContext, ACRA.getConfig().resToastText(), Toast.LENGTH_LONG).show();
                }

                Log.v(ACRA.LOG_TAG, "About to start ReportSenderWorker from #checkReportOnApplicationStart");
                startSendingReports(false, false);
            } else if (ACRA.getConfig().deleteUnapprovedReportsOnApplicationStart()) {
                // NOTIFICATION mode, and there are unapproved reports to send
                // (latest notification has been ignored: neither accepted nor
                // refused). The application developer has decided that these
                // reports should not be renotified ==> destroy them.
                deletePendingNonApprovedReports();
            } else {
                // NOTIFICATION mode, and there are unapproved reports to send
                // (latest notification has been ignored: neither accepted nor
                // refused).
                // Display the notification.
                // The user comment will be associated to the latest report
                notifySendReport(getLatestNonSilentReport(filesList));
            }
        }
    }

    /**
     * Retrieve the most recently created "non silent" report from an array of report file names. A non silent is any
     * report which has not been created with {@link #handleSilentException(Throwable)}.
     * 
     * @param filesList
     *            An array of report file names.
     * @return The most recently created "non silent" report file name.
     */
    private String getLatestNonSilentReport(String[] filesList) {
        if (filesList != null && filesList.length > 0) {
            for (int i = filesList.length - 1; i >= 0; i--) {
                if (!fileNameParser.isSilent(filesList[i])) {
                    return filesList[i];
                }
            }
            // We should never have this result, but this should be secure...
            return filesList[filesList.length - 1];
        } else {
            return null;
        }
    }

    /**
     * Delete all report files stored.
     */
    public void deletePendingReports() {
        deletePendingReports(true, true, 0);
    }

    /**
     * Delete all pending SILENT reports. These are the reports created with {@link #handleSilentException(Throwable)}.
     */
    public void deletePendingSilentReports() {
        deletePendingReports(true, false, 0);
    }

    /**
     * Delete all pending non approved reports.
     */
    public void deletePendingNonApprovedReports() {
        // In NOTIFICATION mode, we have to keep the latest report which could
        // be needed for an existing not yet discarded notification.
        final int nbReportsToKeep = mReportingInteractionMode == ReportingInteractionMode.NOTIFICATION ? 1 : 0;
        deletePendingReports(false, true, nbReportsToKeep);
    }

    /**
     * Delete pending reports.
     * 
     * @param deleteApprovedReports     Set to true to delete approved and silent reports.
     * @param deleteNonApprovedReports  Set to true to delete non approved/silent reports.
     * @param nbOfLatestToKeep          Number of pending reports to retain.
     */
    private void deletePendingReports(boolean deleteApprovedReports, boolean deleteNonApprovedReports, int nbOfLatestToKeep) {
        // TODO Check logic and instances where nbOfLatestToKeep = X, because that might stop us from deleting any reports.
        final CrashReportFinder reportFinder = new CrashReportFinder(mContext);
        final String[] filesList = reportFinder.getCrashReportFiles();
        Arrays.sort(filesList);
        if (filesList != null) {
            for (int iFile = 0; iFile < filesList.length - nbOfLatestToKeep; iFile++) {
                final String fileName = filesList[iFile];
                final boolean isReportApproved = fileNameParser.isApproved(fileName);
                if ((isReportApproved && deleteApprovedReports) || (!isReportApproved && deleteNonApprovedReports)) {
                    final File fileToDelete = new File(mContext.getFilesDir(), fileName);
                    if (!fileToDelete.delete()) {
                        Log.e(ACRA.LOG_TAG, "Could not delete report : " + fileToDelete);
                    }
                }
            }
        }
    }

    /**
     * Disable ACRA : sets this Thread's {@link UncaughtExceptionHandler} back to the system default.
     */
    public void disable() {
        if (mContext != null) {
            Log.d(ACRA.LOG_TAG, "ACRA is disabled for " + mContext.getPackageName());
        } else {
            Log.d(ACRA.LOG_TAG, "ACRA is disabled.");
        }
        if (mDfltExceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(mDfltExceptionHandler);
            enabled = false;
        }
    }

    /**
     * Checks if an array of reports files names contains only silent or approved reports.
     * 
     * @param reportFileNames   Array of report locations to check.
     * @return True if there are only silent or approved reports. False if there is at least one non-approved report.
     */
    private boolean containsOnlySilentOrApprovedReports(String[] reportFileNames) {
        for (String reportFileName : reportFileNames) {
            if (!fileNameParser.isApproved(reportFileName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a {@link ReportSender} to the list of active {@link ReportSender}s.
     * 
     * @param sender    The {@link ReportSender} to be added.
     */
    public void addReportSender(ReportSender sender) {
        mReportSenders.add(sender);
    }

    /**
     * Remove a specific instance of {@link ReportSender} from the list of active {@link ReportSender}s.
     * 
     * @param sender    The {@link ReportSender} instance to be removed.
     */
    public void removeReportSender(ReportSender sender) {
        mReportSenders.remove(sender);
    }

    /**
     * Remove all {@link ReportSender} instances from a specific class.
     * 
     * @param senderClass   ReportSender class whose instances should be removed.
     */
    public void removeReportSenders(Class<?> senderClass) {
        if (ReportSender.class.isAssignableFrom(senderClass)) {
            for (ReportSender sender : mReportSenders) {
                if (senderClass.isInstance(sender)) {
                    mReportSenders.remove(sender);
                }
            }
        }
    }

    /**
     * Clears the list of active {@link ReportSender}s.
     * You should then call {@link #addReportSender(ReportSender)} or ACRA will not send any report anymore.
     */
    public void removeAllReportSenders() {
        mReportSenders.clear();
    }

    /**
     * Removes all previously set {@link ReportSender}s and set the given one as the new {@link ReportSender}.
     * 
     * @param sender    ReportSender to set as the sole sender for this ErrorReporter.
     */
    public void setReportSender(ReportSender sender) {
        removeAllReportSenders();
        addReportSender(sender);
    }

    /**
     * Sets the application start date.
     * This will be included in the reports, will be helpful compared to user_crash date.
     * 
     * @param appStartDate  Time at which the application started.
     */
    public void setAppStartDate(Time appStartDate) {
        mCrashProperties.put(ReportField.USER_APP_START_DATE, appStartDate.format3339(false));
    }
}