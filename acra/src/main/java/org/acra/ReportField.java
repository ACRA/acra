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

/**
 * Specifies all the different fields available in a crash report.
 * 
 * @author Normal
 * 
 */
public enum ReportField {
    /**
     * Application version code. This is the incremental integer version code
     * used to differentiate versions on the android market. {@see
     * PackageInfo#versionCode}
     */
    APP_VERSION_CODE,
    /**
     * Application version name. {@see PackageInfo#versionName}
     */
    APP_VERSION_NAME,
    /**
     * Application package name. {@see Context#getPackageName()}
     */
    PACKAGE_NAME,
    /**
     * Base path of the application's private file folder. {@see
     * Context#getFilesDir()}
     */
    FILE_PATH,
    /**
     * Device model name. {@see Build#MODEL}
     */
    PHONE_MODEL,
    /**
     * Device android version name. {@see VERSION#RELEASE}
     */
    ANDROID_VERSION,
    /**
     * Android build board. {@see Build#BOARD}
     */
    BOARD,
    /**
     * Device brand (manufacturer or carrier). {@see Build#BRAND}
     */
    BRAND,
    /**
     * Device industrial design name. {@see Build#DEVICE}
     */
    DEVICE,
    /**
     * Android build display ID. Content might vary. Examples found:
     * <ul>
     * <li>unknown (Motorola)</li>
     * <li>FRG22D (Motorola)</li>
     * <li>CUPCAKE (Motorola, Samsung)</li>
     * <li>FROYO.XWJJ3 (Samsung)</li>
     * <li>VZW (Motorola/Verizon)</li>
     * <li>Donut.V10a (LG Electronics)</li>
     * <li>1.56.651.2 (HTC/Sprint)</li>
     * <li>RK-1.2.9.eng.root.20101517.173848 (Archos)</li>
     * <li>FRF91 (HTC/T Mobile Uk)</li>
     * <li>ChevyNo1 -> Simply Stunning v2.0.2 ESE53 [3/9/2010] (Custom ROM)</li>
     * <li>...</li>
     * </ul> {@see Build#DISPLAY}
     */
    BUILD_DISPLAY_ID,
    /**
     * Android build fingerprint (unique id). {@see Build#FINGERPRINT}
     */
    FINGERPRINT,
    /**
     * Android build host. {@see Build#HOST}
     */
    BUILD_HOST,
    /**
     * Android build Id. {@see Build#ID}
     */
    BUILD_ID,
    /**
     * Device overall product code. {@see Build#PRODUCT}
     */
    PRODUCT,
    /**
     * Tags associated to the android build. {@see Build#TAGS}
     */
    BUILD_TAGS,
    /**
     * Android build time. {@see Build#TIME}
     */
    BUILD_TIME,
    /**
     * Android type of build ("user", "eng"...). {@see Build#TYPE}
     */
    BUILD_TYPE,
    /**
     * Android build user. {@see Build#USER}
     */
    BUILD_USER,
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
     * the application execution.
     */
    CUSTOM_DATA,
    /**
     * The Holy Stack Trace.
     */
    STACK_TRACE,
    /**
     * {@link Configuration} fields state on the application start. {@see
     * Configuration}
     */
    INITIAL_CONFIGURATION,
    /**
     * {@link Configuration} fields state on the application crash. {@see
     * Configuration}
     */
    CRASH_CONFIGURATION,
    /**
     * Device display specifications. {@see WindowManager#getDefaultDisplay()}
     */
    DISPLAY,
    /**
     * Comment added by the user in the {@link CrashReportDialog} displayed in
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
     * User email address. Can be provided by the user in the {@value ACRA#PREF_USER_EMAIL_ADDRESS} SharedPreference.
     */
    USER_EMAIL;

}
