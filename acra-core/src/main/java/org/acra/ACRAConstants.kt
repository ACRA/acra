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
package org.acra

/**
 * Responsible for collating those constants shared among the ACRA components.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
object ACRAConstants {
    const val REPORTFILE_EXTENSION = ".stacktrace"

    /**
     * Suffix to be added to report files when they have been approved by the
     * user in NOTIFICATION mode
     */
    const val APPROVED_SUFFIX = "-approved"

    /**
     * This key is used to store the silent state of a report sent by
     * handleSilentException().
     */
    @JvmField
    val SILENT_SUFFIX = "-" + ReportField.IS_SILENT

    /**
     * This is the maximum number of previously stored reports that we send
     * in one batch to avoid overloading the network.
     */
    const val MAX_SEND_REPORTS = 5

    /**
     * A special String value to allow the usage of a pseudo-null default value
     * in annotation parameters.
     */
    const val NULL_VALUE = "ACRA-NULL-STRING"

    const val DEFAULT_RES_VALUE = 0

    const val DEFAULT_STRING_VALUE = ""

    const val DEFAULT_LOG_LINES = 100

    const val DEFAULT_BUFFER_SIZE_IN_BYTES = 8192

    /**
     * Default list of [ReportField]s to be sent in reports. You can set
     * your own list with
     * [org.acra.annotation.AcraCore.reportContent].
     */
    @JvmField
    val DEFAULT_REPORT_FIELDS = arrayOf(ReportField.REPORT_ID, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.PACKAGE_NAME, ReportField.FILE_PATH,
            ReportField.PHONE_MODEL, ReportField.BRAND, ReportField.PRODUCT, ReportField.ANDROID_VERSION, ReportField.BUILD, ReportField.TOTAL_MEM_SIZE,
            ReportField.AVAILABLE_MEM_SIZE, ReportField.BUILD_CONFIG, ReportField.CUSTOM_DATA, ReportField.IS_SILENT, ReportField.STACK_TRACE, ReportField.INITIAL_CONFIGURATION,
            ReportField.CRASH_CONFIGURATION, ReportField.DISPLAY, ReportField.USER_COMMENT, ReportField.USER_EMAIL, ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE,
            ReportField.DUMPSYS_MEMINFO, ReportField.LOGCAT, ReportField.INSTALLATION_ID, ReportField.DEVICE_FEATURES, ReportField.ENVIRONMENT, ReportField.SHARED_PREFERENCES)

    const val DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"

    const val DEFAULT_CERTIFICATE_TYPE = "X.509"

    const val NOT_AVAILABLE = "N/A"

    const val UTF8 = "UTF-8"
}