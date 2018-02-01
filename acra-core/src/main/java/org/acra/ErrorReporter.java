package org.acra;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This interface contains methods used to interact with ACRA after it has been initialized
 *
 * @author F43nd1r
 * @since 29.12.2017
 */

public interface ErrorReporter {
    /**
     * Use this method to provide the ErrorReporter with data of your running application.
     * You should call this at several key places in your code the same way as you would output important debug data in a log file.
     * Only the latest value is kept for each key (no history of the values is sent in the report).
     *
     * @param key   A key for your custom data.
     * @param value The value associated to your key.
     * @return The previous value for this key if there was one, or null.
     * @see #removeCustomData(String)
     * @see #getCustomData(String)
     */
    String putCustomData(@NonNull String key, String value);

    /**
     * Removes a key/value pair from your reports custom data field.
     *
     * @param key The key of the data to be removed.
     * @return The value for this key before removal.
     * @see #putCustomData(String, String)
     * @see #getCustomData(String)
     */
    String removeCustomData(@NonNull String key);

    /**
     * Removes all key/value pairs from your reports custom data field.
     */
    void clearCustomData();

    /**
     * Gets the current value for a key in your reports custom data field.
     *
     * @param key The key of the data to be retrieved.
     * @return The value for this key.
     * @see #putCustomData(String, String)
     * @see #removeCustomData(String)
     */
    String getCustomData(@NonNull String key);

    /**
     * Send a silent report for the given exception
     *
     * @param e The {@link Throwable} to be reported. If null the report will contain a new Exception("Report requested by developer").
     */
    void handleSilentException(@Nullable Throwable e);


    /**
     * Enable or disable this ErrorReporter. By default it is enabled.
     *
     * @param enabled Whether this ErrorReporter should capture Exceptions and forward them as crash reports.
     */
    void setEnabled(boolean enabled);

    /**
     * Send a normal report for the given exception
     *
     * @param e              The {@link Throwable} to be reported. If null the report will contain a new Exception("Report requested by developer").
     * @param endApplication if you want the application to be ended after sending the report.
     */
    void handleException(@Nullable Throwable e, boolean endApplication);

    /**
     * Send a normal report for the given exception.
     * The application is then killed and restarted by the system.
     *
     * @param e The {@link Throwable} to be reported. If null the report will contain a new Exception("Report requested by developer").
     */
    void handleException(@Nullable Throwable e);
}
