package org.acra.log;


import android.support.annotation.NonNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Responsible for delegating calls to the Android logging system.
 * <p/>
 * User: William
 * Date: 17/07/11
 * Time: 11:06 AM
 */
public final class NonAndroidLog implements ACRALog {

    // Not that it really matters but these levels match those used in Android.util.Log
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    //public static final int ASSERT = 7;

    private int logLevel = VERBOSE;
    private final PrintStream out = System.out;

    /**
     * Any log that is output at level less that the supplied logLevel will be ignored.
     * <p>
     *     The default log level is {@link NonAndroidLog#VERBOSE}
     * </p>
     *
     * @param logLevel LogLevel to use to filter log output.
     */
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int v(String tag, String msg) {
        if (logLevel <= VERBOSE) {
            out.println(tag + " : " + msg);
        }
        return 0;
    }

    public int v(String tag, String msg, @NonNull Throwable tr) {
        if (logLevel <= VERBOSE) {
            out.println(tag + " : " + msg);
            tr.printStackTrace(out);
        }
        return 0;
    }

    public int d(String tag, String msg) {
        if (logLevel <= DEBUG) {
            out.println(tag + " : " + msg);
        }
        return 0;
    }

    public int d(String tag, String msg, @NonNull Throwable tr) {
        if (logLevel <= DEBUG) {
            out.println(tag + " : " + msg);
            tr.printStackTrace(out);
        }
        return 0;
    }

    public int i(String tag, String msg) {
        if (logLevel <= INFO) {
            out.println(tag + " : " + msg);
        }
        return 0;
    }

    public int i(String tag, String msg, @NonNull Throwable tr) {
        if (logLevel <= INFO) {
            out.println(tag + " : " + msg);
            tr.printStackTrace(out);
        }
        return 0;
    }

    public int w(String tag, String msg) {
        if (logLevel <= WARN) {
            out.println(tag + " : " + msg);
        }
        return 0;
    }

    public int w(String tag, String msg, @NonNull Throwable tr) {
        if (logLevel <= WARN) {
            out.println(tag + " : " + msg);
            tr.printStackTrace(out);
        }
        return 0;
    }

    //public native  boolean isLoggable(java.lang.String tag, int level);
    public int w(String tag, @NonNull Throwable tr) {
        if (logLevel <= WARN) {
            out.println(tag + " : ");
            tr.printStackTrace(out);
        }
        return 0;
    }

    public int e(String tag, String msg) {
        if (logLevel <= ERROR) {
            out.println(tag + " : " + msg);
        }
        return 0;
    }

    public int e(String tag, String msg, @NonNull Throwable tr) {
        if (logLevel <= ERROR) {
            out.println(tag + " : " + msg);
            tr.printStackTrace(out);
        }
        return 0;
    }

    public String getStackTraceString(Throwable tr) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = tr;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }
    //public native  int println(int priority, java.lang.String tag, java.lang.String msg);
}
