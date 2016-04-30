package org.acra.dialog;

import android.app.Activity;
import android.os.Bundle;
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
import java.io.Serializable;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

/**
 * Activity which implements the base functionality for a CrashReportDialog.
 *
 * Activities which extend from this class can override init to create a custom view.
 *
 * The methods sendCrash(comment, userEmail) and cancelReports() can be used to send or cancel
 * sending of reports respectively.
 *
 * This Activity will be instantiated with 3 (or 4) arguments:
 * <ol>
 * <li>{@link ACRAConstants#EXTRA_REPORT_FILE_NAME}</li>
 * <li>{@link ACRAConstants#EXTRA_REPORT_EXCEPTION}</li>
 * <li>{@link ACRAConstants#EXTRA_REPORT_CONFIG}</li>
 * <li>{@link ACRAConstants#EXTRA_FORCE_CANCEL} (optional)</li>
 * </ol>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseCrashReportDialog extends Activity {

    private final DialogHelper helper;

    protected BaseCrashReportDialog() {
        helper = new DialogHelper(this);
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (helper.loadFrom(getIntent())) {
                init(savedInstanceState);
            } else {
                finish();
            }
        } catch (InvalidIntentException e) {
            ACRA.log.w(LOG_TAG, e.getMessage());
            finish();
        }
    }

    protected void init(@Nullable Bundle savedInstanceState) {
    }


    /**
     * Cancel any pending crash reports.
     */
    protected final void cancelReports() {
        helper.cancelReports();
    }


    /**
     * Send crash report given user's comment and email address. If none should be empty strings
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the client.
     */
    protected final void sendCrash(@Nullable String comment, @Nullable String userEmail) {
        helper.sendCrash(comment, userEmail);
    }


    /**
     * @return ACRAs configuration
     */
    protected final ACRAConfiguration getConfig() {
        return helper.getConfig();
    }

    /**
     * @return the exception, if any
     */
    protected final Throwable getException() {
        return helper.getException();
    }
}
