package org.acra.file;

import android.content.Context;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

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

    public ReportLocator(Context context) {
        this.context = context;
    }

    public File getUnapprovedFolder() {
        return context.getDir(UNAPPROVED_FOLDER_NAME, Context.MODE_PRIVATE);
    }

    public File[] getUnapprovedReports() {
        return getUnapprovedFolder().listFiles();
    }

    public File getApprovedFolder() {
        return context.getDir(APPROVED_FOLDER_NAME, Context.MODE_PRIVATE);
    }

    /**
     * @return Approved reports sorted by creation time.
     */
    public File[] getApprovedReports() {
        final File[] reports = getApprovedFolder().listFiles();
        Arrays.sort(reports, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return (int) (lhs.lastModified() - rhs.lastModified());
            }
        });
        return reports;
    }
}
