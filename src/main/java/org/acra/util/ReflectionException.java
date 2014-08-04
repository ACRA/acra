package org.acra.util;

/**
 * Thrown when an error occurs during reflection.
 */
public final class ReflectionException extends Exception  {

    public ReflectionException(String msg, Throwable th) {
        super(msg, th);
    }
}
