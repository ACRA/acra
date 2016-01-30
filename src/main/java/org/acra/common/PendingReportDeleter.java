package org.acra.common;

import android.content.Context;
import org.acra.ACRA;
import org.acra.file.CrashReportFileNameParser;

import java.io.File;
import java.util.Arrays;

import static org.acra.ACRA.LOG_TAG;

/**
 * Deletes pending reports.
 */
public final class PendingReportDeleter {

    private final Context context;
    private final boolean deleteApprovedReports;
    private final boolean deleteNonApprovedReports;
    private final int nrLatestReportsToKeep;

    /**
     * Delete pending reports.
     *
     * @param context                   Application context.
     * @param deleteApprovedReports     True to delete approved and silent reports.
     * @param deleteNonApprovedReports  True to delete non approved/silent reports.
     * @param nrLatestReportsToKeep     Number of pending reports to retain.
     */
    public PendingReportDeleter(Context context, boolean deleteApprovedReports, boolean deleteNonApprovedReports, int nrLatestReportsToKeep) {
        this.context = context;
        this.deleteApprovedReports = deleteApprovedReports;
        this.deleteNonApprovedReports = deleteNonApprovedReports;
        this.nrLatestReportsToKeep = nrLatestReportsToKeep;
    }

    public void execute() {
        final CrashReportFinder reportFinder = new CrashReportFinder(context);

        // Assumption is that sorting on the filename will order from oldest to newest so that we keep latest (if that is desired).
        final String[] filesList = reportFinder.getCrashReportFiles();
        Arrays.sort(filesList);

        final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();
        for (int i = 0; i < filesList.length - nrLatestReportsToKeep; i++) {
            final String fileName = filesList[i];
            final boolean isReportApproved = fileNameParser.isApproved(fileName);
            if ((isReportApproved && deleteApprovedReports) || (!isReportApproved && deleteNonApprovedReports)) {
                final File fileToDelete = new File(context.getFilesDir(), fileName);
                ACRA.log.d(LOG_TAG, "Deleting file " + fileName);
                if (!fileToDelete.delete()) {
                    ACRA.log.e(LOG_TAG, "Could not delete report : " + fileToDelete);
                }
            }
        }
    }

}
