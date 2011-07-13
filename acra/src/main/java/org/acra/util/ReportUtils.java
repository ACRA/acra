package org.acra.util;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.Display;

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
     * Returns a String representation of the content of a {@link android.view.Display} object. It might be interesting in a future
     * release to replace this with a reflection-based collector like {@link org.acra.ConfigurationInspector}.
     *
     * @param display   Display to be inspected.
     * @return A String representation of the content of the given {@link android.view.Display} object.
     */
    public static String getDisplayAsString(Display display) {
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        final StringBuilder result = new StringBuilder();
        result.append("width=").append(display.getWidth()).append('\n').append("height=").append(display.getHeight())
                .append('\n').append("pixelFormat=").append(display.getPixelFormat()).append('\n')
                .append("refreshRate=").append(display.getRefreshRate()).append("fps").append('\n')
                .append("metrics.density=x").append(metrics.density).append('\n').append("metrics.scaledDensity=x")
                .append(metrics.scaledDensity).append('\n').append("metrics.widthPixels=").append(metrics.widthPixels)
                .append('\n').append("metrics.heightPixels=").append(metrics.heightPixels).append('\n')
                .append("metrics.xdpi=").append(metrics.xdpi).append('\n').append("metrics.ydpi=").append(metrics.ydpi);

        return result.toString();
    }
}
