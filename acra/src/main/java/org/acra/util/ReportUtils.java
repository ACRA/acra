package org.acra.util;

import java.io.File;

import org.acra.ACRA;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Responsible for providing base utilities used when constructing the report.
 * <p/>
 * User: William
 * Date: 13/07/11
 * Time: 8:36 PM
 */
public final class ReportUtils {

    /**
     * Calculates the free memory of the device. This is based on an inspection of the filesystem, which in android
     * devices is stored in RAM.
     *
     * @return Number of bytes available.
     */
    public static long getAvailableInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize = stat.getBlockSize();
        final long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * Calculates the total memory of the device. This is based on an inspection of the filesystem, which in android
     * devices is stored in RAM.
     *
     * @return Total number of bytes.
     */
    public static long getTotalInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize = stat.getBlockSize();
        final long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * Returns the DeviceId according to the TelephonyManager.
     *
     * @param context   Context for the application being reported.
     * @return Returns the DeviceId according to the TelephonyManager or null if there is no TelephonyManager.
     */
    public static String getDeviceId(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (RuntimeException e) {
            Log.w(ACRA.LOG_TAG, "Couldn't retrieve DeviceId for : " + context.getPackageName(), e);
            return null;
        }
    }

    public static String getApplicationFilePath(Context context) {
        final File filesDir = context.getFilesDir();
        if (filesDir != null) {
            return filesDir.getAbsolutePath();
        }

        Log.w(ACRA.LOG_TAG, "Couldn't retrieve ApplicationFilePath for : " + context.getPackageName());
        return "Couldn't retrieve ApplicationFilePath";
    }

    /**
     * Returns a String representation of the content of a {@link android.view.Display} object.
     *
     * @param context   Context for the application being reported.
     * @return A String representation of the content of the default Display of the Window Service.
     */
    public static String getDisplayDetails(Context context) {
        try {
            final WindowManager windowManager = (WindowManager) context.getSystemService(android.content.Context.WINDOW_SERVICE);
            final Display display = windowManager.getDefaultDisplay();
            final DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);

            final StringBuilder result = new StringBuilder();
            result.append("width=").                    append(display.getWidth()).append('\n');
            result.append("height=").                   append(display.getHeight()).append('\n');
            result.append("pixelFormat=").              append(display.getPixelFormat()).append('\n');
            result.append("refreshRate=").              append(display.getRefreshRate()).append("fps").append('\n');
            result.append("metrics.density=x").         append(metrics.density).append('\n');
            result.append("metrics.scaledDensity=x").   append(metrics.scaledDensity).append('\n');
            result.append("metrics.widthPixels=").      append(metrics.widthPixels).append('\n');
            result.append("metrics.heightPixels=").     append(metrics.heightPixels).append('\n');
            result.append("metrics.xdpi=").             append(metrics.xdpi).append('\n');
            result.append("metrics.ydpi=").             append(metrics.ydpi);
            return result.toString();

        } catch (RuntimeException e) {
            Log.w(ACRA.LOG_TAG, "Couldn't retrieve DisplayDetails for : " + context.getPackageName(), e);
            return "Couldn't retrieve Display Details";
        }
    }

    /**
     * Returns the current Configuration for this application.
     *
     * @param context   Context for the application being reported.
     * @return A String representation of the current configuration for the application.
     */
    public static String getCrashConfiguration(Context context) {
        try {
            final Configuration crashConf = context.getResources().getConfiguration();
            return ConfigurationInspector.toString(crashConf);
        } catch (RuntimeException e) {
            Log.w(ACRA.LOG_TAG, "Couldn't retrieve CrashConfiguration for : " + context.getPackageName(), e);
            return "Couldn't retrieve crash config";
        }
    }
}
