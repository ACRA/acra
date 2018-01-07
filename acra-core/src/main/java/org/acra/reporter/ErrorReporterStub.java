package org.acra.reporter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.ErrorReporter;

/**
 * @author F43nd1r
 * @since 29.12.2017
 */

public class ErrorReporterStub implements ErrorReporter {
    @Override
    public String putCustomData(@NonNull String key, String value) {
        warnStubCalled();
        return null;
    }

    @Override
    public String removeCustomData(@NonNull String key) {
        warnStubCalled();
        return null;
    }

    @Override
    public void clearCustomData() {
        warnStubCalled();

    }

    @Override
    public String getCustomData(@NonNull String key) {
        warnStubCalled();
        return null;
    }

    @Override
    public void handleSilentException(@Nullable Throwable e) {
        warnStubCalled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        warnStubCalled();
    }

    @Override
    public boolean isRegistered() {
        return false;
    }

    @Override
    public void handleException(@Nullable Throwable e, boolean endApplication) {
        warnStubCalled();
    }

    @Override
    public void handleException(@Nullable Throwable e) {
        warnStubCalled();
    }

    private void warnStubCalled() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String methodName = stackTraceElements.length > 3 ? stackTraceElements[3].getMethodName() : null;
        String message = ACRA.isACRASenderServiceProcess() ? "in SenderService process" : "before ACRA#init (if you did call #init, check if your configuration is valid)";
        ACRA.log.w(ACRA.LOG_TAG, String.format("ErrorReporter#%s called %s. THIS CALL WILL BE IGNORED!", methodName, message));
    }
}
