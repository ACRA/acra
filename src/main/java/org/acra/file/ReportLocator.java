package org.acra.file;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.Arrays;

/**
 * Locates crash reports.
 *
 * @author William Ferguson
 * @since 4.8.0
 */
public final class ReportLocator {

    // Folders under the app folder.
    private static final String UNAPPROVED_FOLDER_NAME = "ACRA-unapproved";
    private static final String APPROVED_FOLDER_NAME = "ACRA-approved";

    private final Context context;

    public ReportLocator(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    public File getUnapprovedFolder() {
        return context.getDir(UNAPPROVED_FOLDER_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public File[] getUnapprovedReports() {
        final File[] reports = getUnapprovedFolder().listFiles();
        if (reports == null) {
            return new File[0];
        }
        return reports;
    }

    @NonNull
    public File getApprovedFolder() {
        return context.getDir(APPROVED_FOLDER_NAME, Context.MODE_PRIVATE);
    }

    /**
     * @return Approved reports sorted by creation time.
     */
    @NonNull
    public File[] getApprovedReports() {
        final File[] reports = getApprovedFolder().listFiles();
        if (reports == null) {
            return new File[0];
        }
        Arrays.sort(reports, new LastModifiedComparator());
        return reports;
    }
}
