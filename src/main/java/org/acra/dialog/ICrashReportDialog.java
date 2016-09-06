/*
 *  Copyright 2016
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.acra.config.ACRAConfiguration;

/**
 * Basic interface for a CrashReportDialog. Can be fulfilled using a {@link CrashReportDelegate}
 *
 * @author F43nd1r
 * @since 4.9.1
 */
public interface ICrashReportDialog {
    /**
     * Responsible for creating and showing the crash report dialog.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link Activity#onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    void init(@Nullable Bundle savedInstanceState);

    /**
     * Cancel any pending crash reports.
     */
    void cancelReports();

    /**
     * Send crash report given user's comment and email address. If none should be empty strings
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the client.
     */
    void sendCrash(@Nullable String comment, @Nullable String userEmail);

    ACRAConfiguration getConfig();

    Throwable getException();
}
