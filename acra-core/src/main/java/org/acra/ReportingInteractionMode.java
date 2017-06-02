/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
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
package org.acra;

/**
 * Defines the different user interaction modes for ACRA.
 * <ul>
 * <li>SILENT: No interaction, reports are sent silently and a "Force close"
 * dialog terminates the app.</li>
 * <li>TOAST: A simple Toast is triggered when the application crashes, the
 * Force close dialog is not displayed.</li>
 * <li>NOTIFICATION: A status bar notification is triggered when the application
 * crashes, the Force close dialog is not displayed. When the user selects the
 * notification, a dialog is displayed asking him if he is ok to send a report</li>
 * </ul>
 */
public enum ReportingInteractionMode {
    /**
     * No interaction, reports are sent silently and a "Force close" dialog
     * terminates the app.
     */
    SILENT,
    /**
     * A status bar notification is triggered when the application crashes, the
     * Force close dialog is not displayed. When the user selects the
     * notification, a dialog is displayed asking him if he is ok to send a
     * report.
     */
    NOTIFICATION,
    /**
     * A simple Toast is triggered when the application crashes, the Force close
     * dialog is not displayed.
     */
    TOAST,
    /**
     * Direct dialog: a report confirmation dialog is displayed right after the crash.
     * Will replace {@link #NOTIFICATION} mode.
     */
    DIALOG
}