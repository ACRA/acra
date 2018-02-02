package org.acra.log;

import android.support.annotation.Nullable;

import org.robolectric.util.Logger;

/**
 * @author F43nd1r
 * @since 01.02.18
 */
public class RobolectricLog implements ACRALog {

    public RobolectricLog() {
    }

    @Override
    public int v(String tag, String msg) {
        Logger.debug(msg);
        return 0;
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        Logger.debug(msg, tr);
        return 0;
    }

    @Override
    public int d(String tag, String msg) {
        Logger.debug(msg);
        return 0;
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        Logger.debug(msg, tr);
        return 0;
    }

    @Override
    public int i(String tag, String msg) {
        Logger.info(msg);
        return 0;
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        Logger.info(msg, tr);
        return 0;
    }

    @Override
    public int w(String tag, String msg) {
        Logger.warn(msg);
        return 0;
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        Logger.warn(msg, tr);
        return 0;
    }

    @Override
    public int w(String tag, Throwable tr) {
        Logger.warn("", tr);
        return 0;
    }

    @Override
    public int e(String tag, String msg) {
        Logger.error(msg);
        return 0;
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        Logger.error(msg, tr);
        return 0;
    }

    @Nullable
    @Override
    public String getStackTraceString(Throwable tr) {
        return String.valueOf(tr);
    }
}
