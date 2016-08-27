package org.acra;

import android.os.Bundle;

import static org.acra.ACRA.LOG_TAG;


/**
 * Old crash report dialog.
 *
 * @deprecated since 4.8.0 use {@link org.acra.dialog.CrashReportDialog} instead
 **/
public final class CrashReportDialog extends org.acra.dialog.CrashReportDialog {

    @Override
    protected void buildAndShowDialog(Bundle savedInstanceState){
        ACRA.log.w(LOG_TAG, "org.acra.CrashReportDialog has been deprecated. Please use org.acra.dialog.CrashReportDialog instead");
        super.buildAndShowDialog(savedInstanceState);
    }
}