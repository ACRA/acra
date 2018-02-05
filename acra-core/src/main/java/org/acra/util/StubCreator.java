package org.acra.util;

import android.support.annotation.NonNull;
import org.acra.ACRA;
import org.acra.ErrorReporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public final class StubCreator {
    private StubCreator() {
    }

    @NonNull
    public static ErrorReporter createErrorReporterStub() {
        return createStub(ErrorReporter.class, (proxy, method, args) -> {
            String message = ACRA.isACRASenderServiceProcess() ? "in SenderService process" : "before ACRA#init (if you did call #init, check if your configuration is valid)";
            ACRA.log.w(ACRA.LOG_TAG, String.format("ErrorReporter#%s called %s. THIS CALL WILL BE IGNORED!", method.getName(), message));
            return null;
        });
    }

    @NonNull
    public static <T> T createStub(Class<T> interfaceClass, InvocationHandler handler) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(StubCreator.class.getClassLoader(), new Class[]{interfaceClass}, handler);
    }
}
