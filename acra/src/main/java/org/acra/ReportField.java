/*
 *  Copyright 2010 Kevin Gaudin
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

import android.content.res.Configuration;

import org.acra.annotation.ReportsCrashes;

/**
 * Specifies all the different fields available in a crash report.
 * 
 * @author Normal
 * 
 */
public enum ReportField {
    /**
     * Report Identifier.
     */
    REPORT_ID,
    /**
     * Application version code. This is the incremental integer version code
     * used to differentiate versions on the android market.
     * 
     * @see android.content.pm.PackageInfo#versionCode
     */
    APP_VERSION_CODE,
    /**
     * Application version name.
     * 
     * @see android.content.pm.PackageInfo#versionName
     */
    APP_VERSION_NAME,
    /**
     * Application package name.
     * 
     * @see android.content.Context#getPackageName()
     */
    PACKAGE_NAME,
    /**
     * Base path of the application's private file folder.
     * 
     * @see android.content.Context#getFilesDir()
     */
    FILE_PATH,
    /**
     * Device model name.
     * 
     * @see android.os.Build#MODEL
     */
    PHONE_MODEL,
    /**
     * Device android version name.
     * 
     * @see android.os.Build.VERSION#RELEASE
     */
    ANDROID_VERSION,
    /**
     * Android Build details.
     * 
     * @see android.os.Build
     */
    BUILD {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * Device brand (manufacturer or carrier).
     * 
     * @see android.os.Build#BRAND
     */
    BRAND,
    /**
     * Device overall product code.
     * 
     * @see android.os.Build#PRODUCT
     */
    PRODUCT,
    /**
     * Estimation of the total device memory size based on filesystem stats.
     */
    TOTAL_MEM_SIZE,
    /**
     * Estimation of the available device memory size based on filesystem stats.
     */
    AVAILABLE_MEM_SIZE,
    /**
     * Contains key = value pairs defined by the application developer during
     * the application build.
     */
    BUILD_CONFIG {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * Contains key = value pairs defined by the application developer during
     * the application execution.
     */
    CUSTOM_DATA {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * The Holy Stack Trace.
     */
    STACK_TRACE,
    /**
     * A hash of the stack trace, taking only method names into account.<br>
     * Line numbers are stripped out before computing the hash. This can help you
     * uniquely identify stack traces.
     */
    STACK_TRACE_HASH,
    /**
     * {@link Configuration} fields state on the application start.
     * 
     * @see Configuration
     */
    INITIAL_CONFIGURATION {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * {@link Configuration} fields state on the application crash.
     * 
     * @see Configuration
     */
    CRASH_CONFIGURATION {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * Device display specifications.
     * 
     * @see android.view.WindowManager#getDefaultDisplay()
     */
    DISPLAY {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * Comment added by the user in the CrashReportDialog displayed in
     * {@link ReportingInteractionMode#NOTIFICATION} mode.
     */
    USER_COMMENT,
    /**
     * User date on application start.
     */
    USER_APP_START_DATE,
    /**
     * User date immediately after the crash occurred.
     */
    USER_CRASH_DATE,
    /**
     * Memory state details for the application process.
     */
    DUMPSYS_MEMINFO,
    /**
     * Content of the android.os.DropBoxManager (introduced in API level 8).
     * Requires READ_LOGS permission.
     */
    DROPBOX,
    /**
     * Logcat default extract. Requires READ_LOGS permission.
     */
    LOGCAT,
    /**
     * Logcat eventslog extract. Requires READ_LOGS permission.
     */
    EVENTSLOG,
    /**
     * Logcat radio extract. Requires READ_LOGS permission.
     */
    RADIOLOG,
    /**
     * True if the report has been explicitly sent silently by the developer.
     */
    IS_SILENT,
    /**
     * Device unique ID (IMEI). Requires READ_PHONE_STATE permission.
     */
    DEVICE_ID,
    /**
     * Installation unique ID. This identifier allow you to track a specific
     * user application installation without using any personal data.
     */
    INSTALLATION_ID,
    /**
     * User email address. Can be provided by the user in the
     * {@link ACRA#PREF_USER_EMAIL_ADDRESS} SharedPreference.
     */
    USER_EMAIL,
    /**
     * Features declared as available on this device by the system.
     */
    DEVICE_FEATURES {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * External storage state and standard directories.
     */
    ENVIRONMENT {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * System settings.
     */
    SETTINGS_SYSTEM {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * Secure settings (applications can't modify them).
     */
    SETTINGS_SECURE {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * Global settings, introduced in Android 4.2 (API level 17) to centralize settings for multiple users.
     */
    SETTINGS_GLOBAL {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * SharedPreferences contents
     */
    SHARED_PREFERENCES {
        @Override
        public boolean containsKeyValuePairs() {
            return true;
        }
    },
    /**
     * Content of your own application log file. To be configured with
     * {@link ReportsCrashes#applicationLogFile()} to define the path/name of
     * the log file and {@link ReportsCrashes#applicationLogFileLines()} to set
     * the number of lines you want to be retrieved.
     */
    APPLICATION_LOG,
    /**
     * Since Android API Level 16 (Android 4.1 - Jelly Beans), retrieve the list
     * of supported Media codecs and their capabilities (color format, profile
     * and level).
     */
    MEDIA_CODEC_LIST,
    /**
     * Retrieves details of the failing thread (id, name, group name).
     */
    THREAD_DETAILS,
    /**
     * Retrieves the user IP address(es).
     */
    USER_IP;

    /**
     * Whether this field is a collection of key/value pairs.
     * 
     * @return true if the field contains a string with a key/value pair on each
     *         line, key and value separated by an equal sign
     * 
     */
    public boolean containsKeyValuePairs() {
        return false;
    }
}
