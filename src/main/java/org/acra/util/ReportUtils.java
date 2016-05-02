package org.acra.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import org.acra.ACRA;
import org.acra.ACRAConstants;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for providing base utilities used when constructing the report.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
public final class ReportUtils {
    private ReportUtils(){}

    /**
     * Calculates the free memory of the device. This is based on an inspection of the filesystem, which in android
     * devices is stored in RAM.
     *
     * @return Number of bytes available.
     */
    public static long getAvailableInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize;
        final long availableBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            //noinspection deprecation
            blockSize = stat.getBlockSize();
            //noinspection deprecation
            availableBlocks = stat.getAvailableBlocks();
        }
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
        final long blockSize;
        final long totalBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
        }
        else {
            //noinspection deprecation
            blockSize = stat.getBlockSize();
            //noinspection deprecation
            totalBlocks = stat.getBlockCount();
        }
        return totalBlocks * blockSize;
    }

    /**
     * Returns the DeviceId according to the TelephonyManager.
     *
     * @param context Context for the application being reported.
     * @return Returns the DeviceId according to the TelephonyManager or null if there is no TelephonyManager.
     */
    @Nullable
    public static String getDeviceId(@NonNull Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (RuntimeException e) {
            ACRA.log.w(LOG_TAG, "Couldn't retrieve DeviceId for : " + context.getPackageName(), e);
            return null;
        }
    }

    @NonNull
    public static String getApplicationFilePath(@NonNull Context context) {
        final File filesDir = context.getFilesDir();
        if (filesDir != null) {
            return filesDir.getAbsolutePath();
        }

        ACRA.log.w(LOG_TAG, "Couldn't retrieve ApplicationFilePath for : " + context.getPackageName());
        return "Couldn't retrieve ApplicationFilePath";
    }

    @NonNull
    public static String getLocalIpAddress() {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                final NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    final InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (!first) {
                            result.append('\n');
                        }
                        result.append(inetAddress.getHostAddress());
                        first = false;
                    }
                }
            }
        } catch (SocketException ex) {
            ACRA.log.w(LOG_TAG, ex.toString());
        }
        return result.toString();
    }

    @NonNull
    public static String getTimeString(@NonNull Calendar time) {
        final SimpleDateFormat format = new SimpleDateFormat(ACRAConstants.DATE_TIME_FORMAT_STRING, Locale.ENGLISH);
        return format.format(time.getTimeInMillis());
    }
}
