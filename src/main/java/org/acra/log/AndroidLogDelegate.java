package org.acra.log;


import android.util.Log;

/**
 * Responsible for delegating calls to the Android logging system.
 * <p/>
 * @author William Ferguson
 * @since 4.3.0
 */
public final class AndroidLogDelegate implements ACRALog {
    public int v(String tag, String msg) {
        return Log.v(tag, msg);
    }
    public int v(String tag, String msg, Throwable tr) {
        return Log.v(tag, msg, tr);
    }
    public int d(String tag, String msg) {
        return Log.d(tag, msg);
    }
    public int d(String tag, String msg, Throwable tr) {
        return Log.d(tag, msg, tr);
    }
    public int i(String tag, String msg) {
        return Log.i(tag, msg);
    }
    public int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }
    public int w(String tag, String msg) {
        return Log.w(tag, msg);
    }
    public int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }
    //public native  boolean isLoggable(java.lang.String tag, int level);
    public int w(String tag, Throwable tr) {
        return Log.w(tag, tr);
    }
    public int e(String tag, String msg) {
        return Log.e(tag, msg);
    }
    public int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }
    public String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }
    //public native  int println(int priority, java.lang.String tag, java.lang.String msg);
}
