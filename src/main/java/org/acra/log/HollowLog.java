package org.acra.log;

import android.support.annotation.Nullable;

/**
 * Stub implementation of {@link org.acra.log.ACRALog}, quenches all logging.
 */
@SuppressWarnings("unused")
public final class HollowLog implements ACRALog {
    @Override
    public int v(String tag, String msg) {
        return 0;
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Override
    public int d(String tag, String msg) {
        return 0;
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Override
    public int i(String tag, String msg) {
        return 0;
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Override
    public int w(String tag, String msg) {
        return 0;
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Override
    public int w(String tag, Throwable tr) {
        return 0;
    }

    @Override
    public int e(String tag, String msg) {
        return 0;
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Nullable
    @Override
    public String getStackTraceString(Throwable tr) {
        return null;
    }
}
