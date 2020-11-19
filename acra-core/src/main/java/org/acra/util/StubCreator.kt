package org.acra.util

import org.acra.ACRA.isACRASenderServiceProcess
import org.acra.ErrorReporter
import org.acra.log.warn
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

object StubCreator {
    fun createErrorReporterStub(): ErrorReporter {
        return createStub { _, method, _ ->
            val message = if (isACRASenderServiceProcess()) "in SenderService process" else "before ACRA#init (if you did call #init, check if your configuration is valid)"
            warn { "ErrorReporter#${method.name} called $message. THIS CALL WILL BE IGNORED!" }
            null
        }
    }

    inline fun <reified T> createStub(handler: InvocationHandler): T = createStub(T::class.java, handler)

    @JvmStatic
    fun <T> createStub(interfaceClass: Class<T>, handler: InvocationHandler): T {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(StubCreator::class.java.classLoader, arrayOf<Class<*>>(interfaceClass), handler) as T
    }
}