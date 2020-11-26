package org.acra.log

import org.acra.ACRA


inline fun debug(messageGenerator: () -> String) {
    if(ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, messageGenerator.invoke())
}

inline fun info(messageGenerator: () -> String) {
    ACRA.log.i(ACRA.LOG_TAG, messageGenerator.invoke())
}

inline fun warn(messageGenerator: () -> String) {
    ACRA.log.w(ACRA.LOG_TAG, messageGenerator.invoke())
}

inline fun warn(throwable: Throwable, messageGenerator: () -> String) {
    ACRA.log.w(ACRA.LOG_TAG, messageGenerator.invoke(), throwable)
}

inline fun error(messageGenerator: () -> String) {
    ACRA.log.e(ACRA.LOG_TAG, messageGenerator.invoke())
}

inline fun error(throwable: Throwable, messageGenerator: () -> String) {
    ACRA.log.e(ACRA.LOG_TAG, messageGenerator.invoke(), throwable)
}