package org.acra.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.widget.Toast;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportPersister;
import org.acra.sender.SenderServiceStarter;
import org.acra.util.ToastSender;

import java.io.File;
import java.io.IOException;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

/**
 * Activity which implements the base functionality for a CrashReportDialog
 * Activities which extend from this class can override onCreate() to create a custom view,
 * but they must call super.onCreate() at the beginning of the method.
 *
 * The methods sendCrash(comment, usrEmail) and cancelReports() can be used to send or cancel
 * sending of reports respectively.
 *
 * This Activity will be instantiated with 3 arguments:
 * <ol>
 *     <li>{@link ACRAConstants#EXTRA_REPORT_FILE_NAME}</li>
 *     <li>{@link ACRAConstants#EXTRA_REPORT_EXCEPTION}</li>
 *     <li>{@link ACRAConstants#EXTRA_REPORT_CONFIG}</li>
 * </ol>
 */
public abstract class BaseCrashReportDialog extends Activity {

    private File reportFile;
    private ACRAConfiguration config;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "CrashReportDialog extras=" + getIntent().getExtras());

        config = (ACRAConfiguration) getIntent().getSerializableExtra(ACRAConstants.EXTRA_REPORT_CONFIG);

        final boolean forceCancel = getIntent().getBooleanExtra(ACRAConstants.EXTRA_FORCE_CANCEL, false);
        if (forceCancel) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Forced reports deletion.");
            cancelReports();
            finish();
            return;
        }

        reportFile = (File) getIntent().getSerializableExtra(ACRAConstants.EXTRA_REPORT_FILE);
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Opening CrashReportDialog for " + reportFile);
        if (reportFile == null) {
            finish();
        }
    }


    /**
     * Cancel any pending crash reports.
     */
    @CallSuper
    protected void cancelReports() {
        new BulkReportDeleter(getApplicationContext()).deleteReports(false, 0);
    }


    /**
     * Send crash report given user's comment and email address. If none should be empty strings
     * @param comment       Comment (may be null) provided by the user.
     * @param userEmail     Email address (may be null) provided by the client.
     */
    @CallSuper
    protected void sendCrash(@Nullable String comment, @Nullable String userEmail) {
        final CrashReportPersister persister = new CrashReportPersister();
        try {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Add user comment to " + reportFile);
            final CrashReportData crashData = persister.load(reportFile);
            crashData.put(USER_COMMENT, comment == null ? "" : comment);
            crashData.put(USER_EMAIL, userEmail == null ? "" : userEmail);
            persister.store(crashData, reportFile);
        } catch (IOException e) {
            ACRA.log.w(LOG_TAG, "User comment not added: ", e);
        }

        // Start the report sending task
        final SenderServiceStarter starter = new SenderServiceStarter(getApplicationContext(), config);
        starter.startService(false, true);

        // Optional Toast to thank the user
        final int toastId = config.resDialogOkToast();
        if (toastId != 0) {
            ToastSender.sendToast(getApplicationContext(), toastId, Toast.LENGTH_LONG);
        }
    }
}
