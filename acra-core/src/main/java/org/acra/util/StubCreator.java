package org.acra.util;

import org.acra.ACRA;
import org.acra.ErrorReporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class StubCreator {
    private StubCreator() {
    }

    public static ErrorReporter createErrorReporterStub() {
        return createStub(ErrorReporter.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String message = ACRA.isACRASenderServiceProcess() ? "in SenderService process" : "before ACRA#init (if you did call #init, check if your configuration is valid)";
                ACRA.log.w(ACRA.LOG_TAG, String.format("ErrorReporter#%s called %s. THIS CALL WILL BE IGNORED!", method.getName(), message));
                return null;
            }
        });
    }

    public static <T> T createStub(Class<T> interfaceClass, InvocationHandler handler) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(StubCreator.class.getClassLoader(), new Class[]{interfaceClass}, handler);
    }
}
