package org.acra;

/**
 * The interface can be used with
 * {@link ErrorReporter#setExceptionHandlerInitializer(ExceptionHandlerInitializer)}
 * to add an additional initialization of the {@link ErrorReporter} before
 * exception is handled.
 * 
 * @see ErrorReporter#setExceptionHandlerInitializer(ExceptionHandlerInitializer)
 */
public interface ExceptionHandlerInitializer {
    /**
     * Called before {@link ErrorReporter} handles the Exception.
     * 
     * @param reporter The {@link ErrorReporter} that will handle the exception
     */
    void initializeExceptionHandler(ErrorReporter reporter);
}