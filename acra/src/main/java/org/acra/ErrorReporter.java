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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.acra.annotation.ReportsCrashes;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.util.Installation;

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
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * <p>
 * The ErrorReporter is a Singleton object in charge of collecting crash context
 * data and sending crash reports. It registers itself as the Application's
 * Thread default {@link UncaughtExceptionHandler}.
 * </p>
 * <p>
 * When a crash occurs, it collects data of the crash context (device, system,
 * stack trace...) and writes a report file in the application private
 * directory. This report file is then sent :
 * <ul>
 * <li>immediately if {@link #mReportingInteractionMode} is set to
 * {@link ReportingInteractionMode#SILENT} or
 * {@link ReportingInteractionMode#TOAST},</li>
 * <li>on application start if in the previous case the transmission could not
 * technically be made,</li>
 * <li>when the user accepts to send it if {@link #mReportingInteractionMode} is
 * set to {@link ReportingInteractionMode#NOTIFICATION}.</li>
 * </ul>
 * </p>
 * <p>
 * If an error occurs while sending a report, it is kept for later attempts.
 * </p>
 */
public class ErrorReporter implements Thread.UncaughtExceptionHandler {

    public static final String REPORTFILE_EXTENSION = ".stacktrace";

    /**
     * Contains the active {@link ReportSender}s.
     */
    private static ArrayList<ReportSender> mReportSenders = new ArrayList<ReportSender>();

    /**
     * Checks and send reports on a separate Thread.
     * 
     * @author Kevin Gaudin
     */
    final class ReportsSenderWorker extends Thread {
        private String mCommentedReportFileName = null;
        private String mUserComment = null;
        private String mUserEmail = null;
        private boolean mSendOnlySilentReports = false;
        private boolean mApprovePendingReports = false;

        /**
         * Creates a new {@link ReportsSenderWorker} to try sending pending
         * reports.
         * 
         * @param sendOnlySilentReports
         *            If set to true, will send only reports which have been
         *            explicitly declared as silent by the application
         *            developer.
         */
        public ReportsSenderWorker(boolean sendOnlySilentReports) {
            mSendOnlySilentReports = sendOnlySilentReports;
        }

        /**
         * Creates a new {@link ReportsSenderWorker} which will try to send ALL
         * pending reports.
         */
        public ReportsSenderWorker() {
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            if (mApprovePendingReports) {
                approvePendingReports();
                mCommentedReportFileName = mCommentedReportFileName.replace(REPORTFILE_EXTENSION, APPROVED_SUFFIX
                        + REPORTFILE_EXTENSION);
            }
            addUserDataToReport(mContext, mCommentedReportFileName, mUserComment, mUserEmail);
            checkAndSendReports(mContext, mSendOnlySilentReports);
        }

        /**
         * Associates a user comment to a specific report file name.
         * 
         * @param reportFileName
         *            The file name of the report.
         * @param userComment
         *            The comment given by the user.
         */
        void setUserComment(String reportFileName, String userComment) {
            mCommentedReportFileName = reportFileName;
            mUserComment = userComment;
        }

        /**
         * Associates a user email to a specific report file name.
         * 
         * @param reportFileName
         *            The file name of the report.
         * @param userEmail
         *            The email address given by the user.
         */
        void setUserEmail(String reportFileName, String userEmail) {
            mCommentedReportFileName = reportFileName;
            mUserEmail = userEmail;
        }

        /**
         * Sets all pending reports as approved for sending by the user.
         */
        public void setApprovePendingReports() {
            mApprovePendingReports = true;
        }
    }

    /**
     * This is the number of previously stored reports that we send in
     * {@link #checkAndSendReports(Context, boolean)}. The number of reports is
     * limited to avoid ANR on application start.
     */
    private static final int MAX_SEND_REPORTS = 5;

    // This is where we collect crash data
    private static CrashReportData mCrashProperties = new CrashReportData();

    // Some custom parameters can be added by the application developer. These
    // parameters are stored here.
    Map<String, String> mCustomParameters = new HashMap<String, String>();
    // This key is used to store the silent state of a report sent by
    // handleSilentException().
    static final String SILENT_SUFFIX = "-" + IS_SILENT;
    // Suffix to be added to report files when they have been approved by the
    // user in NOTIFICATION mode
    static final String APPROVED_SUFFIX = "-approved";

    // Used in the intent starting CrashReportDialog to provide the name of the
    // latest generated report file in order to be able to associate the user
    // comment.
    static final String EXTRA_REPORT_FILE_NAME = "REPORT_FILE_NAME";

    // A reference to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the default exception handling after sending
    // the report.
    private Thread.UncaughtExceptionHandler mDfltExceptionHandler;

    // Our singleton instance.
    private static ErrorReporter mInstanceSingleton;

    // The application context
    private static Context mContext;

    // The Configuration obtained on application start.
    private String mInitialConfiguration;

    // User interaction mode defined by the application developer.
    private ReportingInteractionMode mReportingInteractionMode = ReportingInteractionMode.SILENT;

    /**
     * Flag all pending reports as "approved" by the user. These reports can be
     * sent.
     */
    public void approvePendingReports() {
        Log.d(LOG_TAG, "Mark all pending reports as approved.");
        String[] reportFileNames = getCrashReportFilesList();
        File reportFile = null;
        String newName;
        for (String reportFileName : reportFileNames) {
            if (!isApproved(reportFileName)) {
                reportFile = new File(mContext.getFilesDir(), reportFileName);
                newName = reportFileName.replace(REPORTFILE_EXTENSION, APPROVED_SUFFIX + REPORTFILE_EXTENSION);
                reportFile.renameTo(new File(mContext.getFilesDir(), newName));
            }
        }
    }

    /**
     * Deprecated. Use {@link #putCustomData(String, String)}.
     * 
     * @param key
     * @param value
     */
    @Deprecated
    public void addCustomData(String key, String value) {
        mCustomParameters.put(key, value);
    }

    /**
     * <p>
     * Use this method to provide the ErrorReporter with data of your running
     * application. You should call this at several key places in your code the
     * same way as you would output important debug data in a log file. Only the
     * latest value is kept for each key (no history of the values is sent in
     * the report).
     * </p>
     * <p>
     * The key/value pairs will be stored in the GoogleDoc spreadsheet in the
     * "custom" column, as a text containing a 'key = value' pair on each line.
     * </p>
     * 
     * @param key
     *            A key for your custom data.
     * @param value
     *            The value associated to your key.
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
     * @param key
     *            The key to be removed.
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
     * @param key
     *            The key to be retrieved.
     * @return The value for this key.
     * @see #putCustomData(String, String)
     * @see #removeCustomData(String)
     */
    public String getCustomData(String key) {
        return mCustomParameters.get(key);
    }

    /**
     * Generates the string which is posted in the single custom data field in
     * the GoogleDocs Form.
     * 
     * @return A string with a 'key = value' pair on each line.
     */
    private String createCustomInfoString() {
        String CustomInfo = "";
        Iterator<String> iterator = mCustomParameters.keySet().iterator();
        while (iterator.hasNext()) {
            String CurrentKey = iterator.next();
            String CurrentVal = mCustomParameters.get(CurrentKey);
            CustomInfo += CurrentKey + " = " + CurrentVal + "\n";
        }
        return CustomInfo;
    }

    /**
     * Create or return the singleton instance.
     * 
     * @return the current instance of ErrorReporter.
     */
    public static ErrorReporter getInstance() {
        if (mInstanceSingleton == null) {
            mInstanceSingleton = new ErrorReporter();
        }
        return mInstanceSingleton;
    }

    /**
     * <p>
     * This is where the ErrorReporter replaces the default
     * {@link UncaughtExceptionHandler}.
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
            Thread.setDefaultUncaughtExceptionHandler(this);
            mContext = context;
            // Store the initial Configuration state.
            mInitialConfiguration = ConfigurationInspector.toString(mContext.getResources().getConfiguration());
        }
    }

    /**
     * Calculates the free memory of the device. This is based on an inspection
     * of the filesystem, which in android devices is stored in RAM.
     * 
     * @return Number of bytes available.
     */
    private static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * Calculates the total memory of the device. This is based on an inspection
     * of the filesystem, which in android devices is stored in RAM.
     * 
     * @return Total number of bytes.
     */
    private static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * Collects crash data.
     * 
     * @param context
     *            The application context.
     */
    private void retrieveCrashData(Context context) {
        try {
            ReportsCrashes config = ACRA.getConfig();
            ReportField[] fields = config.customReportContent();
            if (fields.length == 0) {
                if (config.mailTo() == null || "".equals(config.mailTo())) {
                    fields = ReportsCrashes.DEFAULT_REPORT_FIELDS;
                } else if (!"".equals(config.mailTo())) {
                    fields = ReportsCrashes.DEFAULT_MAIL_REPORT_FIELDS;
                }
            }
            List<ReportField> fieldsList = Arrays.asList(fields);

            SharedPreferences prefs = ACRA.getACRASharedPreferences();

            // Generate report uuid
            if (fieldsList.contains(REPORT_ID)) {
                mCrashProperties.put(ReportField.REPORT_ID, UUID.randomUUID().toString());
            }

            // Collect meminfo
            if (fieldsList.contains(DUMPSYS_MEMINFO)) {
                mCrashProperties.put(DUMPSYS_MEMINFO, DumpSysCollector.collectMemInfo());
            }

            PackageManager pm = context.getPackageManager();

            // Collect DropBox and logcat
            if (pm != null) {
                if (prefs.getBoolean(ACRA.PREF_ENABLE_SYSTEM_LOGS, true)
                        && pm.checkPermission(permission.READ_LOGS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    Log.i(ACRA.LOG_TAG, "READ_LOGS granted! ACRA can include LogCat and DropBox data.");
                    if (fieldsList.contains(LOGCAT)) {
                        mCrashProperties.put(LOGCAT, LogCatCollector.collectLogCat(null).toString());
                    }
                    if (fieldsList.contains(EVENTSLOG)) {
                        if (ACRA.getConfig().includeEventsLogcat()) {
                            mCrashProperties.put(EVENTSLOG, LogCatCollector.collectLogCat("events").toString());
                        } else {
                            mCrashProperties.put(EVENTSLOG, "@ReportsCrashes(includeEventsLog=false)");
                        }
                    }
                    if (fieldsList.contains(RADIOLOG)) {
                        if (ACRA.getConfig().includeRadioLogcat()) {
                            mCrashProperties.put(RADIOLOG, LogCatCollector.collectLogCat("radio").toString());
                        } else {
                            mCrashProperties.put(RADIOLOG, "@ReportsCrashes(includeRadioLog=false)");
                        }
                    }
                    if (fieldsList.contains(DROPBOX)) {
                        if (ACRA.getConfig().includeDropBox()) {
                            mCrashProperties.put(DROPBOX,
                                    DropBoxCollector.read(mContext, ACRA.getConfig().additionalDropBoxTags()));
                        } else {
                            mCrashProperties.put(DROPBOX, "@ReportsCrashes(includeDropBox=false)");
                        }
                    }
                } else {
                    Log.i(ACRA.LOG_TAG, "READ_LOGS not allowed. ACRA will not include LogCat and DropBox data.");
                }

                // Retrieve UDID(IMEI) if permission is available
                if (fieldsList.contains(DEVICE_ID)
                        && prefs.getBoolean(ACRA.PREF_ENABLE_DEVICE_ID, true)
                        && pm.checkPermission(permission.READ_PHONE_STATE, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    String deviceId = tm.getDeviceId();
                    if (deviceId != null) {
                        mCrashProperties.put(DEVICE_ID, deviceId);
                    }
                }
            }
            
            // Installation unique ID
            if(fieldsList.contains(INSTALLATION_ID)) {
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

            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
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
                mCrashProperties.put(TOTAL_MEM_SIZE, Long.toString(getTotalInternalMemorySize()));
            }
            if (fieldsList.contains(AVAILABLE_MEM_SIZE)) {
                mCrashProperties.put(AVAILABLE_MEM_SIZE, Long.toString(getAvailableInternalMemorySize()));
            }

            // Application file path
            if (fieldsList.contains(FILE_PATH)) {
                mCrashProperties.put(FILE_PATH, context.getFilesDir().getAbsolutePath());
            }

            // Main display details
            if (fieldsList.contains(DISPLAY)) {
                Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
                mCrashProperties.put(DISPLAY, toString(display));
            }

            // User crash date with local timezone
            if (fieldsList.contains(USER_CRASH_DATE)) {
                Time curDate = new Time();
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
            if(fieldsList.contains(ENVIRONMENT)) {
                mCrashProperties.put(ENVIRONMENT, ReflectionCollector.collectStaticGettersResults(Environment.class));
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while retrieving crash data", e);
        }
    }

    /**
     * Returns a String representation of the content of a {@link Display}
     * object. It might be interesting in a future release to replace this with
     * a reflection-based collector like {@link ConfigurationInspector}.
     * 
     * @param display
     *            A Display instance to be inspected.
     * @return A String representation of the content of the given
     *         {@link Display} object.
     */
    private static String toString(Display display) {
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        StringBuilder result = new StringBuilder();
        result.append("width=").append(display.getWidth()).append('\n').append("height=").append(display.getHeight())
                .append('\n').append("pixelFormat=").append(display.getPixelFormat()).append('\n')
                .append("refreshRate=").append(display.getRefreshRate()).append("fps").append('\n')
                .append("metrics.density=x").append(metrics.density).append('\n').append("metrics.scaledDensity=x")
                .append(metrics.scaledDensity).append('\n').append("metrics.widthPixels=").append(metrics.widthPixels)
                .append('\n').append("metrics.heightPixels=").append(metrics.heightPixels).append('\n')
                .append("metrics.xdpi=").append(metrics.xdpi).append('\n').append("metrics.ydpi=").append(metrics.ydpi);

        return result.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(ACRA.LOG_TAG,
                "ACRA caught a " + e.getClass().getSimpleName() + " exception for " + mContext.getPackageName()
                        + ". Building report.");
        // Generate and send crash report
        ReportsSenderWorker worker = handleException(e);

        if (mReportingInteractionMode == ReportingInteractionMode.TOAST) {
            try {
                // Wait a bit to let the user read the toast
                Thread.sleep(4000);
            } catch (InterruptedException e1) {
                Log.e(LOG_TAG, "Error : ", e1);
            }
        }

        if (worker != null) {
            while (worker.isAlive()) {
                try {
                    // Wait for the report sender to finish it's task before
                    // killing the process
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    Log.e(LOG_TAG, "Error : ", e1);
                }
            }
        }

        if (mReportingInteractionMode == ReportingInteractionMode.SILENT) {
            // If using silent mode, let the system default handler do it's job
            // and display the force close dialog.
            mDfltExceptionHandler.uncaughtException(t, e);
        } else {
            // If ACRA handles user notifications whit a Toast or a Notification
            // the Force Close dialog is one more notification to the user...
            // We choose to close the process ourselves using the same actions.
            CharSequence appName = "Application";
            try {
                PackageManager pm = mContext.getPackageManager();
                appName = pm.getApplicationInfo(mContext.getPackageName(), 0).loadLabel(mContext.getPackageManager());
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
     * Try to send a report, if an error occurs stores a report file for a later
     * attempt. You can set the {@link ReportingInteractionMode} for this
     * specific report. Use {@link #handleException(Throwable)} to use the
     * Application default interaction mode.
     * 
     * @param e
     *            The Throwable to be reported. If null the report will contain
     *            a new Exception("Report requested by developer").
     * @param reportingInteractionMode
     *            The desired interaction mode.
     */
    ReportsSenderWorker handleException(Throwable e, ReportingInteractionMode reportingInteractionMode) {
        boolean sendOnlySilentReports = false;

        if (reportingInteractionMode == null) {
            // No interaction mode defined, we assume it has been set during
            // ACRA.initACRA()
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
        String reportFileName = saveCrashReportFile(null, null);

        if (reportingInteractionMode == ReportingInteractionMode.SILENT
                || reportingInteractionMode == ReportingInteractionMode.TOAST
                || ACRA.getACRASharedPreferences().getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) {
            // Send reports now
            approvePendingReports();
            ReportsSenderWorker wk = new ReportsSenderWorker(sendOnlySilentReports);
            wk.start();
            return wk;
        } else if (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
            // Send reports when user accepts
            notifySendReport(reportFileName);
        }
        return null;
    }

    /**
     * Send a report for this {@link Throwable} with the reporting interaction
     * mode set on the Application level by the developer.
     * 
     * @param e
     *            The {@link Throwable} to be reported. If null the report will
     *            contain a new Exception("Report requested by developer").
     */
    public ReportsSenderWorker handleException(Throwable e) {
        return handleException(e, mReportingInteractionMode);
    }

    /**
     * Send a report for this {@link Throwable} silently (forces the use of
     * {@link ReportingInteractionMode#SILENT} for this report, whatever is the
     * mode set for the application. Very useful for tracking difficult defects.
     * 
     * @param e
     *            The {@link Throwable} to be reported. If null the report will
     *            contain a new Exception("Report requested by developer").
     */
    public ReportsSenderWorker handleSilentException(Throwable e) {
        // Mark this report as silent.
        mCrashProperties.put(IS_SILENT, "true");
        return handleException(e, ReportingInteractionMode.SILENT);
    }

    /**
     * Send a status bar notification. The action triggered when the
     * notification is selected is to start the {@link CrashReportDialog}
     * Activity.
     */
    void notifySendReport(String reportFileName) {
        // This notification can't be set to AUTO_CANCEL because after a crash,
        // clicking on it restarts the application and this triggers a check
        // for pending reports which issues the notification back.
        // Notification cancellation is done in the dialog activity displayed
        // on notification click.
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        ReportsCrashes conf = ACRA.getConfig();

        // Default notification icon is the warning symbol
        int icon = conf.resNotifIcon();

        CharSequence tickerText = mContext.getText(conf.resNotifTickerText());
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);

        CharSequence contentTitle = mContext.getText(conf.resNotifTitle());
        CharSequence contentText = mContext.getText(conf.resNotifText());

        Intent notificationIntent = new Intent(mContext, CrashReportDialog.class);
        Log.d(LOG_TAG, "Creating Notification for " + reportFileName);
        notificationIntent.putExtra(EXTRA_REPORT_FILE_NAME, reportFileName);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);

        // Send new notification
        notificationManager.cancelAll();
        notificationManager.notify(ACRA.NOTIF_CRASH_ID, notification);
    }

    /**
     * Sends the report with all configured ReportSenders. If at least one
     * sender completed its job, the report is considered as sent and will not
     * be sent again for failing senders.
     * 
     * @param context
     *            The application context.
     * @param errorContent
     *            Crash data.
     * @throws ReportSenderException
     *             If unable to send the crash report.
     */
    private static void sendCrashReport(Context context, CrashReportData errorContent) throws ReportSenderException {
        boolean sentAtLeastOnce = false;
        for (ReportSender sender : mReportSenders) {
            try {
                sender.send(errorContent);
                // If at least one sender worked, don't re-send the report
                // later.
                sentAtLeastOnce = true;
            } catch (ReportSenderException e) {
                Log.w(LOG_TAG, "An exception occured while executing a ReportSender.", e);
                if (!sentAtLeastOnce) {
                    Log.e(LOG_TAG, "The first sender failed, ACRA will try all senders again later.");
                    throw e;
                } else {
                    Log.w(LOG_TAG, "ReportSender of class " + sender.getClass().getName()
                            + " failed but other senders completed their task. ACRA will not send this report again.");
                }
            }
        }
    }

    /**
     * When a report can't be sent, it is saved here in a file in the root of
     * the application private directory.
     * 
     * @param fileName
     *            In a few rare cases, we write the report again with additional
     *            data (user comment for example). In such cases, you can
     *            provide the already existing file name here to overwrite the
     *            report file. If null, a new file report will be generated
     * @param crashData
     *            Can be used to save an alternative (or previously generated)
     *            report data. Used to store again a report with the addition of
     *            user comment. If null, the default current crash data are
     *            used.
     */
    private static String saveCrashReportFile(String fileName, CrashReportData crashData) {
        try {
            Log.d(LOG_TAG, "Writing crash report file.");
            if (crashData == null) {
                crashData = mCrashProperties;
            }
            if (fileName == null) {
                Time now = new Time();
                now.setToNow();
                long timestamp = now.toMillis(false);
                String isSilent = crashData.getProperty(IS_SILENT);
                fileName = "" + timestamp + (isSilent != null ? SILENT_SUFFIX : "") + REPORTFILE_EXTENSION;
            }
            FileOutputStream reportFile = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            crashData.store(reportFile, "");
            reportFile.close();
            return fileName;
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occured while writing the report file...", e);
        }
        return null;
    }

    /**
     * Returns an array containing the names of pending crash report files.
     * 
     * @return an array containing the names of pending crash report files.
     */
    String[] getCrashReportFilesList() {
        File dir = mContext.getFilesDir();
        if (dir != null) {
            Log.d(LOG_TAG, "Looking for error files in " + dir.getAbsolutePath());

            // Filter for ".stacktrace" files
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(REPORTFILE_EXTENSION);
                }
            };
            return dir.list(filter);
        } else {
            Log.w(LOG_TAG,
                    "Application files directory does not exist! The application may not be installed correctly. Please try reinstalling.");
            return new String[0];
        }
    }

    /**
     * Send pending reports.
     * 
     * @param context
     *            The application context.
     * @param sendOnlySilentReports
     *            Send only reports explicitly declared as SILENT by the
     *            developer (sent via {@link #handleSilentException(Throwable)}.
     */
    void checkAndSendReports(Context context, boolean sendOnlySilentReports) {
        File curFile = null;
        try {
            String[] reportFiles = getCrashReportFilesList();
            if (reportFiles != null && reportFiles.length > 0) {
                Arrays.sort(reportFiles);
                CrashReportData previousCrashReport = new CrashReportData();
                // send only a few reports to avoid overloading the network
                int reportsSentCount = 0;
                for (String curFileName : reportFiles) {
                    curFile = null;
                    if (!sendOnlySilentReports || (sendOnlySilentReports && isSilent(curFileName))) {
                        if (reportsSentCount < MAX_SEND_REPORTS) {
                            Log.i(LOG_TAG, "Sending file " + curFileName);
                            curFile = new File(context.getFilesDir(), curFileName);
                            FileInputStream input = context.openFileInput(curFileName);
                            previousCrashReport.load(input);
                            input.close();
                            sendCrashReport(context, previousCrashReport);

                            // DELETE FILES !!!!
                            curFile.delete();
                        }
                        reportsSentCount++;
                    }
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (curFile != null) {
                curFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (curFile != null) {
                curFile.delete();
            }
        } catch (ReportSenderException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the wanted user interaction mode for sending reports.
     * 
     * @param reportingInteractionMode
     */
    void setReportingInteractionMode(ReportingInteractionMode reportingInteractionMode) {
        mReportingInteractionMode = reportingInteractionMode;
    }

    /**
     * This method looks for pending reports and does the action required
     * depending on the interaction mode set.
     */
    public void checkReportsOnApplicationStart() {
        String[] filesList = getCrashReportFilesList();
        if (filesList != null && filesList.length > 0) {
            boolean onlySilentOrApprovedReports = containsOnlySilentOrApprovedReports(filesList);
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

                new ReportsSenderWorker().start();
            } else if (ACRA.getConfig().deleteUnapprovedReportsOnApplicationStart()) {
                // NOTIFICATION mode, and there are unapproved reports to send
                // (latest notification has been ignored: neither accepted nor
                // refused). The application developer has decided that these
                // reports should not be renotified ==> destroy them.
                ErrorReporter.getInstance().deletePendingNonApprovedReports();
            } else {
                // NOTIFICATION mode, and there are unapproved reports to send
                // (latest notification has been ignored: neither accepted nor
                // refused).
                // Display the notification.
                // The user comment will be associated to the latest report
                ErrorReporter.getInstance().notifySendReport(getLatestNonSilentReport(filesList));
            }
        }

    }

    /**
     * Retrieve the most recently created "non silent" report from an array of
     * report file names. A non silent is any report which has not been created
     * with {@link #handleSilentException(Throwable)}.
     * 
     * @param filesList
     *            An array of report file names.
     * @return The most recently created "non silent" report file name.
     */
    private String getLatestNonSilentReport(String[] filesList) {
        if (filesList != null && filesList.length > 0) {
            for (int i = filesList.length - 1; i >= 0; i--) {
                if (!isSilent(filesList[i])) {
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
     * Delete all pending SILENT reports. These are the reports created with
     * {@link #handleSilentException(Throwable)}.
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
        int nbReportsToKeep = mReportingInteractionMode == ReportingInteractionMode.NOTIFICATION ? 1 : 0;
        deletePendingReports(false, true, nbReportsToKeep);
    }

    /**
     * Delete pending reports.
     * 
     * @param deleteApprovedReports
     *            Set to true to delete approved and silent reports.
     * @param deleteNonApprovedReports
     *            Set to true to delete non approved/silent reports.
     */
    private void deletePendingReports(boolean deleteApprovedReports, boolean deleteNonApprovedReports,
            int nbOfLatestToKeep) {
        String[] filesList = getCrashReportFilesList();
        Arrays.sort(filesList);
        if (filesList != null) {
            boolean isReportApproved = false;
            String fileName;
            for (int iFile = 0; iFile < filesList.length - nbOfLatestToKeep; iFile++) {
                fileName = filesList[iFile];
                isReportApproved = isApproved(fileName);
                if ((isReportApproved && deleteApprovedReports) || (!isReportApproved && deleteNonApprovedReports)) {
                    new File(mContext.getFilesDir(), fileName).delete();
                }
            }
        }
    }

    /**
     * Disable ACRA : sets this Thread's {@link UncaughtExceptionHandler} back
     * to the system default.
     */
    public void disable() {
        if (mContext != null) {
            Log.d(ACRA.LOG_TAG, "ACRA is disabled for " + mContext.getPackageName());
        } else {
            Log.d(ACRA.LOG_TAG, "ACRA is disabled.");
        }
        if (mDfltExceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(mDfltExceptionHandler);
        }
    }

    /**
     * Checks if an array of reports files names contains only silent or
     * approved reports.
     * 
     * @param reportFileNames
     *            the list of reports (as provided by
     *            {@link #getCrashReportFilesList()})
     * @return True if there are only silent or approved reports. False if there
     *         is at least one non-approved report.
     */
    private boolean containsOnlySilentOrApprovedReports(String[] reportFileNames) {
        for (String reportFileName : reportFileNames) {
            if (!isApproved(reportFileName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Guess that a report is silent from its file name.
     * 
     * @param reportFileName
     * @return True if the report has been declared explicitly silent using
     *         {@link #handleSilentException(Throwable)}.
     */
    private boolean isSilent(String reportFileName) {
        return reportFileName.contains(SILENT_SUFFIX);
    }

    /**
     * <p>
     * Returns true if the report is considered as approved. This includes:
     * </p>
     * <ul>
     * <li>Reports which were pending when the user agreed to send a report in
     * the NOTIFICATION mode Dialog.</li>
     * <li>Explicit silent reports</li>
     * </ul>
     * 
     * @param reportFileName
     * @return True if a report can be sent.
     */
    private boolean isApproved(String reportFileName) {
        return isSilent(reportFileName) || reportFileName.contains(APPROVED_SUFFIX);
    }

    /**
     * Sets the user comment value in an existing report file. User comments are
     * ALWAYS entered by the user in a Dialog which is displayed after
     * application restart. This means that the report file has already been
     * generated and saved to the filesystem. Associating the comment to the
     * report requires to reopen an existing report, insert the comment value
     * and save the report back.
     * 
     * @param context
     *            The application context.
     * @param commentedReportFileName
     *            The file name of the report which should receive the comment.
     * @param userComment
     *            The comment entered by the user.
     * @param userEmail
     */
    private static void addUserDataToReport(Context context, String commentedReportFileName, String userComment,
            String userEmail) {
        Log.d(LOG_TAG, "Add user comment to " + commentedReportFileName);
        if (commentedReportFileName != null && userComment != null) {
            try {
                FileInputStream input = context.openFileInput(commentedReportFileName);
                CrashReportData commentedCrashReport = new CrashReportData();
                Log.d(LOG_TAG, "Loading Properties report to insert user comment.");
                commentedCrashReport.load(input);
                input.close();
                commentedCrashReport.put(USER_COMMENT, userComment);
                commentedCrashReport.put(USER_EMAIL, userEmail);
                saveCrashReportFile(commentedReportFileName, commentedCrashReport);
            } catch (FileNotFoundException e) {
                Log.w(LOG_TAG, "User comment not added: ", e);
            } catch (InvalidPropertiesFormatException e) {
                Log.w(LOG_TAG, "User comment not added: ", e);
            } catch (IOException e) {
                Log.w(LOG_TAG, "User comment not added: ", e);
            }

        }
    }

    /**
     * Add a {@link ReportSender} to the list of active {@link ReportSender}s.
     * 
     * @param sender
     *            The {@link ReportSender} to be added.
     */
    public void addReportSender(ReportSender sender) {
        mReportSenders.add(sender);
    }

    /**
     * Remove a specific instance of {@link ReportSender} from the list of
     * active {@link ReportSender}s.
     * 
     * @param sender
     *            The {@link ReportSender} instance to be removed.
     */
    public void removeReportSender(ReportSender sender) {
        mReportSenders.remove(sender);
    }

    /**
     * Remove all {@link ReportSender} instances from a specific class.
     * 
     * @param senderClass
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
     * Clears the list of active {@link ReportSender}s. You should then call
     * {@link #addReportSender(ReportSender)} or ACRA will not send any report
     * anymore.
     */
    public void removeAllReportSenders() {
        mReportSenders.clear();
    }

    /**
     * Removes all previously set {@link ReportSender}s and set the given one as
     * the new {@link ReportSender}.
     * 
     * @param sender
     */
    public void setReportSender(ReportSender sender) {
        removeAllReportSenders();
        addReportSender(sender);
    }

    /**
     * Sets the application start date. This will be included in the reports,
     * will be helpfull compared to user_crash date.
     * 
     * @param appStartDate
     */
    public void setAppStartDate(Time appStartDate) {
        mCrashProperties.put(ReportField.USER_APP_START_DATE, appStartDate.format3339(false));
    }
}