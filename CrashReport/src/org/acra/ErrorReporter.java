package org.acra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class ErrorReporter implements Thread.UncaughtExceptionHandler {
    private static final String LOG_TAG = CrashReportingApplication.LOG_TAG;
    private static final int MAX_SEND_REPORTS = 5;
    private static final String VERSION_NAME_KEY = "entry.0.single";
    private static final String PACKAGE_NAME_KEY = "entry.1.single";
    private static final String FILE_PATH_KEY = "entry.2.single";
    private static final String PHONE_MODEL_KEY = "entry.3.single";
    private static final String ANDROID_VERSION_KEY = "entry.4.single";
    private static final String BOARD_KEY = "entry.5.single";
    private static final String BRAND_KEY = "entry.6.single";
    private static final String DEVICE_KEY = "entry.7.single";
    private static final String DISPLAY_KEY = "entry.8.single";
    private static final String FINGERPRINT_KEY = "entry.9.single";
    private static final String HOST_KEY = "entry.10.single";
    private static final String ID_KEY = "entry.11.single";
    private static final String MODEL_KEY = "entry.12.single";
    private static final String PRODUCT_KEY = "entry.13.single";
    private static final String TAGS_KEY = "entry.14.single";
    private static final String TIME_KEY = "entry.15.single";
    private static final String TYPE_KEY = "entry.16.single";
    private static final String USER_KEY = "entry.17.single";
    private static final String TOTAL_MEM_SIZE_KEY = "entry.18.single";
    private static final String AVAILABLE_MEM_SIZE_KEY = "entry.19.single";
    private static final String CUSTOM_DATA_KEY = "entry.20.single";
    private static final String STACK_TRACE_KEY = "entry.21.single";
    Properties mCrashProperties = new Properties();
    Map<String, String> mCustomParameters = new HashMap<String, String>();

    private Thread.UncaughtExceptionHandler mDfltExceptionHandler;
    private static ErrorReporter mInstanceSingleton;
    private Context mContext;
    private static Uri mFormUri;

    public ErrorReporter() {
    }

    public void setFormUri(Uri formUri) {
        mFormUri = formUri;
    }

    public void addCustomData(String Key, String Value) {
        mCustomParameters.put(Key, Value);
    }

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

    public static ErrorReporter getInstance() {
        if (mInstanceSingleton == null)
            mInstanceSingleton = new ErrorReporter();
        return mInstanceSingleton;
    }

    public void init(Context context) {
        mDfltExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context;
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    void retrieveCrashData(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            // Version
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            mCrashProperties.put(VERSION_NAME_KEY, pi.versionName);
            // Package name
            mCrashProperties.put(PACKAGE_NAME_KEY, pi.packageName);
            // Device model
            mCrashProperties.put(PHONE_MODEL_KEY, android.os.Build.MODEL);
            // Android version
            mCrashProperties.put(ANDROID_VERSION_KEY,
                    android.os.Build.VERSION.RELEASE);

            // Android build data
            mCrashProperties.put(BOARD_KEY, android.os.Build.BOARD);
            mCrashProperties.put(BRAND_KEY, android.os.Build.BRAND);
            mCrashProperties.put(DEVICE_KEY, android.os.Build.DEVICE);
            mCrashProperties.put(DISPLAY_KEY, android.os.Build.DISPLAY);
            mCrashProperties.put(FINGERPRINT_KEY, android.os.Build.FINGERPRINT);
            mCrashProperties.put(HOST_KEY, android.os.Build.HOST);
            mCrashProperties.put(ID_KEY, android.os.Build.ID);
            mCrashProperties.put(MODEL_KEY, android.os.Build.MODEL);
            mCrashProperties.put(PRODUCT_KEY, android.os.Build.PRODUCT);
            mCrashProperties.put(TAGS_KEY, android.os.Build.TAGS);
            mCrashProperties.put(TIME_KEY, "" + android.os.Build.TIME);
            mCrashProperties.put(TYPE_KEY, android.os.Build.TYPE);
            mCrashProperties.put(USER_KEY, android.os.Build.USER);

            // Device Memory
            mCrashProperties.put(TOTAL_MEM_SIZE_KEY, ""
                    + getTotalInternalMemorySize());
            mCrashProperties.put(AVAILABLE_MEM_SIZE_KEY, ""
                    + getAvailableInternalMemorySize());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while retrieving crash data", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    public void uncaughtException(Thread t, Throwable e) {
        retrieveCrashData(mContext);
        // TODO: add a field in the googledoc form for the crash date.
        // Date CurDate = new Date();
        // Report += "Error Report collected on : " + CurDate.toString();

        // Add custom info, they are all stored in a single field
        mCrashProperties.put(CUSTOM_DATA_KEY, createCustomInfoString());

        // Build stack trace
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        mCrashProperties.put(STACK_TRACE_KEY, result.toString());
        printWriter.close();

        try {
            sendCrashReport(mContext, mCrashProperties);
        } catch (Exception anyException) {
            // The crash report will be posted on the next launch
            saveCrashReportFile();
        }

        // Let the official exception handler do it's job
        // TODO: display a developer-defined message
        mDfltExceptionHandler.uncaughtException(t, e);
    }

    /**
     * Sends the report in an HTTP POST to a Google Doc Form
     * 
     * @param context
     * @param errorContent
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    private void sendCrashReport(Context context, Properties errorContent)
            throws UnsupportedEncodingException, IOException, KeyManagementException, NoSuchAlgorithmException {
        errorContent.put("pageNumber", "0");
        errorContent.put("backupCache", "");
        errorContent.put("submit", "Envoyer");

        URL reportUrl = new URL(mFormUri.toString());
        Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
        HttpUtils.doPost(errorContent, reportUrl);
    }

    private void saveCrashReportFile() {
        try {
            Random generator = new Random();
            int random = generator.nextInt(99999);
            String FileName = "stack-" + random + ".stacktrace";
            FileOutputStream trace = mContext.openFileOutput(FileName,
                    Context.MODE_PRIVATE);
            mCrashProperties.save(trace, "");
            trace.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occured while writing the report file...",
                    e);
        }
    }

    private String[] getCrashReportFilesList() {
        File dir = new File(mCrashProperties.get(FILE_PATH_KEY) + "/");
        Log.d(LOG_TAG, "Looking for error files in "
                + mCrashProperties.get(FILE_PATH_KEY));
        // Try to create the files folder if it doesn't exist
        dir.mkdir();
        // Filter for ".stacktrace" files
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".stacktrace");
            }
        };
        return dir.list(filter);
    }

    public void checkAndSendReports(Context context) {
        try {
            mCrashProperties.put(FILE_PATH_KEY, context.getFilesDir()
                    .getAbsolutePath());
            String[] reportFilesList = getCrashReportFilesList();
            if (reportFilesList.length > 0) {
                Properties previousCrashReport = new Properties();
                // send only a few reports to avoid ANR
                int curIndex = 0;
                for (String curString : reportFilesList) {
                    if (curIndex++ <= MAX_SEND_REPORTS) {
                        String filePath = mCrashProperties.get(FILE_PATH_KEY)
                                + "/" + curString;
                        InputStream input = new FileInputStream(filePath);
                        previousCrashReport.load(input);
                        input.close();
                    }

                    // DELETE FILES !!!!
                    File curFile = new File(mCrashProperties.get(FILE_PATH_KEY)
                            + "/" + curString);
                    curFile.delete();
                    sendCrashReport(context, previousCrashReport);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}