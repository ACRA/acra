package org.acra.file;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;

import java.io.File;
import java.util.Arrays;

import static org.acra.ACRA.LOG_TAG;

/**
 * Deletes unsent reports.
 */
public final class BulkReportDeleter {

    @NonNull
    private final ReportLocator reportLocator;

    public BulkReportDeleter(@NonNull Context context) {
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
