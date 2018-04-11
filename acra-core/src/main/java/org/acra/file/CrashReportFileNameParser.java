/*
 *  Copyright 2012 Kevin Gaudin
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
package org.acra.file;

import android.support.annotation.NonNull;

import org.acra.ACRAConstants;
import org.acra.ErrorReporter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Responsible for determining the state of a Crash Report based on its file name.
 *
 * @author William Ferguson &amp; F43nd1r
 * @since 4.3.0
 */
public final class CrashReportFileNameParser {

    /**
     * Guess that a report is silent from its file name.
     *
     * @param reportFileName Name of the report to check whether it should be sent silently.
     * @return True if the report has been declared explicitly silent using {@link ErrorReporter#handleSilentException(Throwable)}.
     */
    public boolean isSilent(@NonNull String reportFileName) {
        return reportFileName.contains(ACRAConstants.SILENT_SUFFIX);
    }

    /**
     * Returns true if the report is considered as approved.
     * <p>
     * This includes:
     * </p>
     * <ul>
     * <li>Reports which were pending when the user agreed to send a report in the NOTIFICATION mode Dialog.</li>
     * <li>Explicit silent reports</li>
     * </ul>
     *
     * @param reportFileName Name of report to check whether it is approved to be sent.
     * @return True if a report can be sent.
     * @deprecated use {@link ReportLocator#getApprovedReports()} and {@link ReportLocator#getUnapprovedReports()} instead
     */
    @Deprecated
    public boolean isApproved(@NonNull String reportFileName) {
        return isSilent(reportFileName) || reportFileName.contains(ACRAConstants.APPROVED_SUFFIX);
    }

    /**
     * Gets the timestamp of a report from its name
     *
     * @param reportFileName Name of the report to get the timestamp from.
     * @return timestamp of the report
     */
    @NonNull
    public Calendar getTimestamp(@NonNull String reportFileName) {
        final String timestamp = reportFileName.replace(ACRAConstants.REPORTFILE_EXTENSION, "").replace(ACRAConstants.SILENT_SUFFIX, "");
        final Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat(ACRAConstants.DATE_TIME_FORMAT_STRING, Locale.ENGLISH).parse(timestamp));
        } catch (ParseException ignored) {
        }
        return calendar;
    }
}
