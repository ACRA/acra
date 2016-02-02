package org.acra.file;

import android.content.Context;
import org.acra.ACRA;
import org.acra.file.LastModifiedComparator;
import org.acra.file.ReportLocator;

import java.io.File;
import java.util.Arrays;

import static org.acra.ACRA.LOG_TAG;

/**
 * Deletes unsent reports.
 */
public final class BulkReportDeleter {

    private final ReportLocator reportLocator;

    public BulkReportDeleter(Context context) {
        this.reportLocator = new ReportLocator(context);
    }

    /**
     * @param approved  Whether to delete approved or unapproved reports.
     * @param nrToKeep  Number of latest reports to keep.
     */
    public void deleteReports(boolean approved, int nrToKeep) {
        final File[] files = approved ? reportLocator.getApprovedReports() : reportLocator.getUnapprovedReports();

        Arrays.sort(files, new LastModifiedComparator());

        for (int i = 0; i < files.length - nrToKeep; i++) {
            if (!files[i].delete()) {
                ACRA.log.w(LOG_TAG, "Could not delete report : " + files[i]);
            }
        }
    }
}
