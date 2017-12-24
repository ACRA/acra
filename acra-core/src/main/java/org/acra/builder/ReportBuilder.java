/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acra.builder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Fluent API used to assemble the different options used for a crash report.
 *
 * @since 4.8.0
 */
public final class ReportBuilder {

    private String message;
    private Thread uncaughtExceptionThread;
    private Throwable exception;
    private final Map<String, String> customData = new HashMap<>();

    private boolean sendSilently = false;
    private boolean endApplication = false;

    /**
     * Set the error message to be reported.
     *
     * @param msg the error message
     * @return the updated {@code ReportBuilder}
     */
    @NonNull
    @SuppressWarnings("unused")
    public ReportBuilder message(@Nullable String msg) {
        message = msg;
        return this;
    }

    /**
     * @return the error message, or null if none is present
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * Sets the Thread on which an uncaught Exception occurred.
     *
     * @param thread Thread on which an uncaught Exception occurred.
     * @return the updated {@code ReportBuilder}
     */
    @NonNull
    public ReportBuilder uncaughtExceptionThread(@Nullable Thread thread) {
        uncaughtExceptionThread = thread;
        return this;
    }

    /**
     * @return the Thread on which an uncaught Exception occurred, or null if none present
     */
    @Nullable
    public Thread getUncaughtExceptionThread() {
        return uncaughtExceptionThread;
    }

    /**
     * Set the stack trace to be reported
     *
     * @param e The exception that should be associated with this report
     * @return the updated {@code ReportBuilder}
     */
    @NonNull
    public ReportBuilder exception(@Nullable Throwable e) {
        exception = e;
        return this;
    }

    /**
     * @return the exception, or null if none present
     */
    @Nullable
    public Throwable getException() {
        return exception;
    }

    /**
     * Sets additional values to be added to {@link org.acra.ReportField#CUSTOM_DATA}. Values
     * specified here take precedence over globally specified custom data.
     *
     * @param customData a map of custom key-values to be attached to the report
     * @return the updated {@code ReportBuilder}
     */
    @NonNull
    @SuppressWarnings("unused")
    public ReportBuilder customData(@NonNull Map<String, String> customData) {
        this.customData.putAll(customData);
        return this;
    }

    /**
     * Sets an additional value to be added to {@link org.acra.ReportField#CUSTOM_DATA}. The value
     * specified here takes precedence over globally specified custom data.
     *
     * @param key   the key identifying the custom data
     * @param value the value for the custom data entry
     * @return the updated {@code ReportBuilder}
     */
    @NonNull
    @SuppressWarnings("unused")
    public ReportBuilder customData(@NonNull String key, String value) {
        customData.put(key, value);
        return this;
    }

    /**
     * @return a map with all custom data
     */
    @NonNull
    public Map<String, String> getCustomData() {
        return new HashMap<>(customData);
    }

    /**
     * Forces the report to be sent silently, ignoring all interactions
     *
     * @return the updated {@code ReportBuilder}
     */
    @NonNull
    public ReportBuilder sendSilently() {
        sendSilently = true;
        return this;
    }

    /**
     * @return if this should send silently
     */
    public boolean isSendSilently() {
        return sendSilently;
    }

    /**
     * Ends the application after sending the crash report
     *
     * @return the updated {@code ReportBuilder}
     */
    @NonNull
    public ReportBuilder endApplication() {
        endApplication = true;
        return this;
    }

    /**
     * @return if this should stop the application after collecting
     */
    public boolean isEndApplication() {
        return endApplication;
    }

    /**
     * Assembles and sends the crash report.
     *
     * @param reportExecutor ReportExecutor to use to build the report.
     */
    public void build(@NonNull ReportExecutor reportExecutor) {
        if (message == null && exception == null) {
            message = "Report requested by developer";
        }

        reportExecutor.execute(this);
    }
}
