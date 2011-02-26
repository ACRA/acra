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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION;

/**
 * Specifies all the different fields available in a crash report.
 * 
 * @author Normal
 * 
 */
public enum ReportField {
    /**
     * Application version code. This is the incremental integer version code
     * use to differentiate versions on the android market.
     * {@link PackageInfo#versionCode}
     */
    APP_VERSION_CODE,
    /**
     * Application version name. {@link PackageInfo#versionName}
     */
    APP_VERSION_NAME,
    /**
     * Application package name. {@link Context#getPackageName()}
     */
    PACKAGE_NAME,
    /**
     * Base path of the application's private file folder.
     * {@link Context#getFilesDir()}
     */
    FILE_PATH,
    /**
     * Device model name. {@link Build#MODEL}
     */
    PHONE_MODEL,
    /**
     * Device android version name. {@link VERSION#RELEASE}
     */
    ANDROID_VERSION,
    /**
     * Android build board. {@link Build#BOARD}
     */
    BOARD,
    /**
     * Device brand (manufacturer or carrier). {@link Build#BRAND}
     */
    BRAND,
    /**
     * Device industrial design name. {@link Build#DEVICE}
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
     * </ul> {@link Build#DISPLAY}
     */
    BUILD_DISPLAY_ID,
    /**
     * Android build fingerprint (unique id). {@link Build#FINGERPRINT}
     */
    FINGERPRINT,
    /**
     * Android build host. {@link Build#HOST}
     */
    BUILD_HOST,
    /**
     * Android build Id. {@link Build#ID}
     */
    BUILD_ID,
    /**
     * Device overall product code. {@link Build#PRODUCT}
     */
    PRODUCT,
    /**
     * Tags associated to the android build. {@link Build#TAGS}
     */
    BUILD_TAGS,
    /**
     * Android build time. {@link Build#TIME}
     */
    BUILD_TIME,
    /**
     * Android type of build ("user", "eng"...). {@link Build#TYPE}
     */
    BUILD_TYPE,
    /**
     * Android build user. {@link Build#USER}
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
     * {@link Configuration} fields state on the application start.
     */
    INITIAL_CONFIGURATION,
    /**
     * {@link Configuration} fields state on the application crash.
     */
    CRASH_CONFIGURATION,
    /**
     * Device display specs.
     */
    DISPLAY, USER_COMMENT, USER_CRASH_DATE, DUMPSYS_MEMINFO, DROPBOX, LOGCAT, EVENTSLOG, RADIOLOG, IS_SILENT, DEVICE_ID, USER_EMAIL;

}
