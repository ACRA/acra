package org.acra.log;


import android.util.Log;

/**
 * Responsible for delegating calls to the Android logging system.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
public final class AndroidLogDelegate implements ACRALog {
    @Override
    public int v(String tag, String msg) {
        return Log.v(tag, msg);
    }
    @Override
    public int v(String tag, String msg, Throwable tr) {
        return Log.v(tag, msg, tr);
    }
    @Override
    public int d(String tag, String msg) {
        return Log.d(tag, msg);
    }
    @Override
    public int d(String tag, String msg, Throwable tr) {
        return Log.d(tag, msg, tr);
    }
    @Override
    public int i(String tag, String msg) {
        return Log.i(tag, msg);
    }
    @Override
    public int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }
    @Override
    public int w(String tag, String msg) {
        return Log.w(tag, msg);
    }
    @Override
    public int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }
    //public native  boolean isLoggable(java.lang.String tag, int level);
    @Override
    public int w(String tag, Throwable tr) {
        return Log.w(tag, tr);
    }
    @Override
    public int e(String tag, String msg) {
        return Log.e(tag, msg);
    }
    @Override
    public int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }
    @Override
    public String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }
    //public native  int println(int priority, java.lang.String tag, java.lang.String msg);
}
