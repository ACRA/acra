package org.acra;

/**
 * Responsible for determining the state of a Crash Report based on its file name.
 * <p/>
 * @author William Ferguson
 * @since 4.3.0
 */
final class CrashReportFileNameParser {

    /**
     * Guess that a report is silent from its file name.
     *
     * @param reportFileName    Name of the report to check whether it should be sent silently.
     * @return True if the report has been declared explicitly silent using {@link ErrorReporter#handleSilentException(Throwable)}.
     */
    public boolean isSilent(String reportFileName) {
        return reportFileName.contains(ACRAConstants.SILENT_SUFFIX);
    }

    /**
     * Returns true if the report is considered as approved.
     * <p>
          This includes:
     * </p>
     * <ul>
     * <li>Reports which were pending when the user agreed to send a report in the NOTIFICATION mode Dialog.</li>
     * <li>Explicit silent reports</li>
     * </ul>
     *
     * @param reportFileName    Name of report to check whether it is approved to be sent.
     * @return True if a report can be sent.
     */
    public boolean isApproved(String reportFileName) {
        return isSilent(reportFileName) || reportFileName.contains(ACRAConstants.APPROVED_SUFFIX);
    }
}
