package org.acra.collector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.acra.ACRA;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

final class DisplayManagerCollector {

    final static SparseArray<String> mFlagsNames = new SparseArray<String>();
    final static SparseArray<String> mDensities = new SparseArray<String>();

    public static String collectDisplays(Context ctx) {
        Display[] displays = null;
        final StringBuilder result = new StringBuilder();

        if (Compatibility.getAPILevel() < 17) {
            // Before Android 4.2, there was a single display available from the
            // window manager
            final WindowManager windowManager = (WindowManager) ctx
                    .getSystemService(android.content.Context.WINDOW_SERVICE);
            displays = new Display[1];
            displays[0] = windowManager.getDefaultDisplay();
        } else {
            // Since Android 4.2, we can fetch multiple displays with the
            // DisplayManager.
            try {
                Object displayManager = ctx.getSystemService((String) (ctx.getClass().getField("DISPLAY_SERVICE")
                        .get(null)));
                Method getDisplays = displayManager.getClass().getMethod("getDisplays");
                displays = (Display[]) getDisplays.invoke(displayManager);
            } catch (IllegalArgumentException e) {
                ACRA.log.w(ACRA.LOG_TAG, "Error while collecting DisplayManager data: ", e);
            } catch (SecurityException e) {
                ACRA.log.w(ACRA.LOG_TAG, "Error while collecting DisplayManager data: ", e);
            } catch (IllegalAccessException e) {
                ACRA.log.w(ACRA.LOG_TAG, "Error while collecting DisplayManager data: ", e);
            } catch (NoSuchFieldException e) {
                ACRA.log.w(ACRA.LOG_TAG, "Error while collecting DisplayManager data: ", e);
            } catch (NoSuchMethodException e) {
                ACRA.log.w(ACRA.LOG_TAG, "Error while collecting DisplayManager data: ", e);
            } catch (InvocationTargetException e) {
                ACRA.log.w(ACRA.LOG_TAG, "Error while collecting DisplayManager data: ", e);
            }
        }

        for (Display display : displays) {
            result.append(collectDisplayData(display));
        }

        return result.toString();
    }

    private static Object collectDisplayData(Display display) {
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        final StringBuilder result = new StringBuilder();

        result.append(collectCurrentSizeRange(display));
        result.append(collectFlags(display));
        result.append(display.getDisplayId()).append(".height=").append(display.getHeight()).append('\n');
        result.append(collectMetrics(display, "getMetrics"));
        result.append(collectName(display));
        result.append(display.getDisplayId()).append(".orientation=").append(display.getOrientation()).append('\n');
        result.append(display.getDisplayId()).append(".pixelFormat=").append(display.getPixelFormat()).append('\n');
        result.append(collectMetrics(display, "getRealMetrics"));
        result.append(collectSize(display, "getRealSize"));
        result.append(collectRectSize(display));
        result.append(display.getDisplayId()).append(".refreshRate=").append(display.getRefreshRate()).append('\n');
        result.append(collectRotation(display));
        result.append(collectSize(display, "getSize"));
        result.append(display.getDisplayId()).append(".width=").append(display.getWidth()).append('\n');
        result.append(collectIsValid(display));

        return result.toString();
    }

    private static Object collectIsValid(Display display) {
        StringBuilder result = new StringBuilder();
        try {
            Method isValid = display.getClass().getMethod("isValid");
            Boolean value = (Boolean) isValid.invoke(display);
            result.append(display.getDisplayId()).append(".isValid=").append(value).append('\n');
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return result.toString();
    }

    private static Object collectRotation(Display display) {
        StringBuilder result = new StringBuilder();
        try {
            Method getRotation = display.getClass().getMethod("getRotation");
            int rotation = (Integer) getRotation.invoke(display);
            result.append(display.getDisplayId()).append(".rotation=");
            switch (rotation) {
            case Surface.ROTATION_0:
                result.append("ROTATION_0");
                break;
            case Surface.ROTATION_90:
                result.append("ROTATION_90");
                break;
            case Surface.ROTATION_180:
                result.append("ROTATION_180");
                break;
            case Surface.ROTATION_270:
                result.append("ROTATION_270");
                break;
            default:
                result.append(rotation);
                break;
            }
            result.append('\n');
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return result.toString();
    }

    private static Object collectRectSize(Display display) {
        StringBuilder result = new StringBuilder();
        try {
            Method getRectSize = display.getClass().getMethod("getRectSize", Rect.class);
            Rect size = new Rect();
            getRectSize.invoke(display, size);
            result.append(display.getDisplayId()).append(".rectSize=[").append(size.top).append(',').append(size.left)
                    .append(',').append(size.width()).append(',').append(size.height()).append(']').append('\n');
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return result.toString();
    }

    private static Object collectSize(Display display, String methodName) {
        StringBuilder result = new StringBuilder();
        try {
            Method getRealSize = display.getClass().getMethod(methodName, Point.class);
            Point size = new Point();
            getRealSize.invoke(display, size);
            result.append(display.getDisplayId()).append('.').append(methodName).append("=[").append(size.x)
                    .append(',').append(size.y).append(']').append('\n');
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return result.toString();
    }

    private static String collectCurrentSizeRange(Display display) {
        StringBuilder result = new StringBuilder();
        try {
            Method getCurrentSizeRange = display.getClass().getMethod("getCurrentSizeRange", Point.class, Point.class);
            Point smallest = new Point(), largest = new Point();
            getCurrentSizeRange.invoke(display, smallest, largest);
            result.append(display.getDisplayId()).append(".currentSizeRange.smallest=[").append(smallest.x).append(',')
                    .append(smallest.y).append(']').append('\n');
            result.append(display.getDisplayId()).append(".currentSizeRange.largest=[").append(largest.x).append(',')
                    .append(largest.y).append(']').append('\n');
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return result.toString();
    }

    private static String collectFlags(Display display) {
        StringBuilder result = new StringBuilder();
        try {
            Method getFlags = display.getClass().getMethod("getFlags");
            int flags = (Integer) getFlags.invoke(display);

            for (Field field : display.getClass().getFields()) {
                if (field.getName().startsWith("FLAG_")) {
                    mFlagsNames.put(field.getInt(null), field.getName());
                }
            }

            result.append(display.getDisplayId()).append(".flags=").append(activeFlags(mFlagsNames, flags))
                    .append('\n');
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return result.toString();
    }

    private static String collectName(Display display) {
        StringBuilder result = new StringBuilder();
        try {
            Method getName = display.getClass().getMethod("getName");
            String name = (String) getName.invoke(display);

            result.append(display.getDisplayId()).append(".name=").append(name).append('\n');
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return result.toString();
    }

    private static Object collectMetrics(Display display, String methodName) {
        StringBuilder result = new StringBuilder();
        try {
            Method getMetrics = display.getClass().getMethod(methodName);
            DisplayMetrics metrics = (DisplayMetrics) getMetrics.invoke(display);

            for (Field field : DisplayMetrics.class.getFields()) {
                if (field.getType().equals(Integer.class) && field.getName().startsWith("DENSITY_")
                        && !field.getName().equals("DENSITY_DEFAULT")) {
                    mDensities.put(field.getInt(null), field.getName());
                }
            }

            result.append(display.getDisplayId()).append('.').append(methodName).append(".density=")
                    .append(metrics.density).append('\n');
            result.append(display.getDisplayId()).append('.').append(methodName).append(".densityDpi=")
                    .append(metrics.getClass().getField("densityDpi")).append('\n');
            result.append(display.getDisplayId()).append('.').append(methodName).append("scaledDensity=x")
                    .append(metrics.scaledDensity).append('\n');
            result.append(display.getDisplayId()).append('.').append(methodName).append(".widthPixels=")
                    .append(metrics.widthPixels).append('\n');
            result.append(display.getDisplayId()).append('.').append(methodName).append(".heightPixels=")
                    .append(metrics.heightPixels).append('\n');
            result.append(display.getDisplayId()).append('.').append(methodName).append(".xdpi=").append(metrics.xdpi)
                    .append('\n');
            result.append(display.getDisplayId()).append('.').append(methodName).append(".ydpi=").append(metrics.ydpi)
                    .append('\n');

        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (NoSuchFieldException e) {
        }
        return result.toString();
    }

    /**
     * Some fields contain multiple value types which can be isolated by
     * applying a bitmask. That method returns the concatenation of active
     * values.
     * 
     * @param valueNames
     *            The array containing the different values and names for this
     *            field. Must contain mask values too.
     * @param bitfield
     *            The bitfield to inspect.
     * @return The names of the different values contained in the bitfield,
     *         separated by '+'.
     */
    private static String activeFlags(SparseArray<String> valueNames, int bitfield) {
        final StringBuilder result = new StringBuilder();

        // Look for masks, apply it an retrieve the masked value
        for (int i = 0; i < valueNames.size(); i++) {
            final int maskValue = valueNames.keyAt(i);
            final int value = bitfield & maskValue;
            if (value > 0) {
                if (result.length() > 0) {
                    result.append('+');
                }
                result.append(valueNames.get(value));
            }
        }
        return result.toString();
    }

}
