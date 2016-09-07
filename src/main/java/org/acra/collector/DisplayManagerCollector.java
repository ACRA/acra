/*
 *  Copyright 2016
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

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.hardware.display.DisplayManagerCompat;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;

import java.lang.reflect.Field;

/**
 * Collects information about the connected display(s)
 */
final class DisplayManagerCollector extends Collector {
    private final Context context;
    private final SparseArray<String> flagNames = new SparseArray<String>();

    DisplayManagerCollector(Context context) {
        super(ReportField.DISPLAY);
        this.context = context;
    }


    @NonNull
    @Override
    String collect(ReportField reportField, ReportBuilder reportBuilder) {
        final StringBuilder result = new StringBuilder();
        for (Display display : DisplayManagerCompat.getInstance(context).getDisplays()) {
            result.append(collectDisplayData(display));
        }

        return result.toString();
    }

    @NonNull
    private Object collectDisplayData(@NonNull Display display) {
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        //noinspection deprecation
        return collectCurrentSizeRange(display) +
                collectFlags(display) +
                display.getDisplayId() + ".height=" + display.getHeight() + '\n' +
                collectMetrics(display) +
                collectName(display) +
                display.getDisplayId() + ".orientation=" + display.getRotation() + '\n' +
                display.getDisplayId() + ".pixelFormat=" + display.getPixelFormat() + '\n' +
                collectRealMetrics(display) +
                collectRealSize(display) +
                collectRectSize(display) +
                display.getDisplayId() + ".refreshRate=" + display.getRefreshRate() + '\n' +
                collectRotation(display) +
                collectSize(display) +
                display.getDisplayId() + ".width=" + display.getWidth() + '\n' +
                collectIsValid(display);
    }

    @NonNull
    private static String collectIsValid(@NonNull Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return display.getDisplayId() + ".isValid=" + display.isValid() + '\n';
        }
        return "";
    }

    @NonNull
    private static String collectRotation(@NonNull Display display) {
        return display.getDisplayId() + ".rotation=" + rotationToString(display.getRotation()) + '\n';
    }

    @NonNull
    private static String rotationToString(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return "ROTATION_0";
            case Surface.ROTATION_90:
                return "ROTATION_90";
            case Surface.ROTATION_180:
                return "ROTATION_180";
            case Surface.ROTATION_270:
                return "ROTATION_270";
            default:
                return String.valueOf(rotation);
        }
    }

    @NonNull
    private static String collectRectSize(@NonNull Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            final Rect size = new Rect();
            display.getRectSize(size);
            return display.getDisplayId() + ".rectSize=[" + size.top + ',' + size.left +
                    ',' + size.width() + ',' + size.height() + ']' + '\n';
        }
        return "";
    }

    @NonNull
    private static String collectSize(@NonNull Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            final Point size = new Point();
            display.getSize(size);
            return display.getDisplayId() + ".size=[" + size.x
                    + ',' + size.y + ']' + '\n';
        }
        return "";
    }

    private static String collectRealSize(@NonNull Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final Point size = new Point();
            display.getRealSize(size);
            return display.getDisplayId() + ".realSize=[" + size.x
                    + ',' + size.y + ']' + '\n';
        }
        return "";
    }

    @NonNull
    private static String collectCurrentSizeRange(@NonNull Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Point smallest = new Point();
            final Point largest = new Point();
            display.getCurrentSizeRange(smallest, largest);
            return display.getDisplayId() + ".currentSizeRange.smallest=[" + smallest.x + ',' + smallest.y + "]\n"
                    + display.getDisplayId() + ".currentSizeRange.largest=[" + largest.x + ',' + largest.y + "]\n";
        }
        return "";
    }

    @NonNull
    private String collectFlags(@NonNull Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final int flags = display.getFlags();
            for (Field field : display.getClass().getFields()) {
                if (field.getName().startsWith("FLAG_")) {
                    try {
                        flagNames.put(field.getInt(null), field.getName());
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
            return display.getDisplayId() + ".flags=" + activeFlags(flags) + '\n';
        }
        return "";
    }

    @NonNull
    private static String collectName(@NonNull Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return display.getDisplayId() + ".name=" + display.getName() + '\n';
        }
        return "";
    }

    @NonNull
    private static String collectMetrics(@NonNull Display display) {
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return collectMetrics(display.getDisplayId() + ".metrics", metrics);
    }

    @NonNull
    private static String collectRealMetrics(@NonNull Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            return collectMetrics(display.getDisplayId() + ".realMetrics", metrics);
        }
        return "";
    }

    @NonNull
    private static String collectMetrics(@NonNull String prefix, @NonNull DisplayMetrics metrics) {
        return prefix + ".density=" + metrics.density + '\n'
                + prefix + ".densityDpi=" + metrics.densityDpi + '\n'
                + prefix + ".scaledDensity=x" + metrics.scaledDensity + '\n'
                + prefix + ".widthPixels=" + metrics.widthPixels + '\n'
                + prefix + ".heightPixels=" + metrics.heightPixels + '\n'
                + prefix + ".xdpi=" + metrics.xdpi + '\n'
                + prefix + ".ydpi=" + metrics.ydpi + '\n';
    }

    /**
     * Some fields contain multiple value types which can be isolated by
     * applying a bitmask. That method returns the concatenation of active
     * values.
     *
     * @param bitfield   The bitfield to inspect.
     * @return The names of the different values contained in the bitfield,
     * separated by '+'.
     */
    @NonNull
    private String activeFlags(int bitfield) {
        final StringBuilder result = new StringBuilder();

        // Look for masks, apply it an retrieve the masked value
        for (int i = 0; i < flagNames.size(); i++) {
            final int maskValue = flagNames.keyAt(i);
            final int value = bitfield & maskValue;
            if (value > 0) {
                if (result.length() > 0) {
                    result.append('+');
                }
                result.append(flagNames.get(value));
            }
        }
        return result.toString();
    }

}
