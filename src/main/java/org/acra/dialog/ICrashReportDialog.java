package org.acra.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.acra.config.ACRAConfiguration;

/**
 * Created on 03.08.2016.
 *
 * @author F43nd1r
 */
public interface ICrashReportDialog {
    /**
     * Responsible for creating and showing the crash report dialog.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link Activity#onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    void init(@Nullable Bundle savedInstanceState);

    /**
     * Cancel any pending crash reports.
     */
    void cancelReports();

    /**
     * Send crash report given user's comment and email address. If none should be empty strings
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the client.
     */
    void sendCrash(@Nullable String comment, @Nullable String userEmail);

    ACRAConfiguration getConfig();

    Throwable getException();
}
