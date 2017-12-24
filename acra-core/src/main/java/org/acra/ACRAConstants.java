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
package org.acra;

import static org.acra.ReportField.*;

/**
 * Responsible for collating those constants shared among the ACRA components.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
public final class ACRAConstants {
    private ACRAConstants() {
    }

    public static final String REPORTFILE_EXTENSION = ".stacktrace";

    /**
     * Suffix to be added to report files when they have been approved by the
     * user in NOTIFICATION mode
     */
    public static final String APPROVED_SUFFIX = "-approved";
    /**
     * This key is used to store the silent state of a report sent by
     * handleSilentException().
     */
    public static final String SILENT_SUFFIX = "-" + IS_SILENT;
    /**
     * This is the maximum number of previously stored reports that we send
     * in one batch to avoid overloading the network.
     */
    public static final int MAX_SEND_REPORTS = 5;

    /**
     * A special String value to allow the usage of a pseudo-null default value
     * in annotation parameters.
     */
    public static final String NULL_VALUE = "ACRA-NULL-STRING";

    public static final int DEFAULT_RES_VALUE = 0;

    public static final String DEFAULT_STRING_VALUE = "";

    public static final int DEFAULT_LOG_LINES = 100;

    public static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 8192;

    /**
     * Default list of {@link ReportField}s to be sent in reports. You can set
     * your own list with
     * {@link org.acra.annotation.AcraCore#reportContent()}.
     */
    public static final ReportField[] DEFAULT_REPORT_FIELDS = {REPORT_ID, APP_VERSION_CODE, APP_VERSION_NAME, PACKAGE_NAME, FILE_PATH, PHONE_MODEL, BRAND, PRODUCT, ANDROID_VERSION,
            BUILD, TOTAL_MEM_SIZE, AVAILABLE_MEM_SIZE, BUILD_CONFIG, CUSTOM_DATA, IS_SILENT, STACK_TRACE, INITIAL_CONFIGURATION, CRASH_CONFIGURATION, DISPLAY, USER_COMMENT, USER_EMAIL,
            USER_APP_START_DATE, USER_CRASH_DATE, DUMPSYS_MEMINFO, LOGCAT, INSTALLATION_ID, DEVICE_FEATURES, ENVIRONMENT, SHARED_PREFERENCES};

    public static final String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";

    public static final String DEFAULT_CERTIFICATE_TYPE = "X.509";

    public static final String NOT_AVAILABLE = "N/A";

    public static final String UTF8 = "UTF-8";
}
