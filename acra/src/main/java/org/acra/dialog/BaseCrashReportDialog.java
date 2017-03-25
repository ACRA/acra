package org.acra.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportPersister;
import org.acra.sender.SenderServiceStarter;
import org.acra.util.ToastSender;
import org.json.JSONException;

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
 * <li>{@link ACRAConstants#EXTRA_REPORT_FILE}</li>
 * <li>{@link ACRAConstants#EXTRA_REPORT_EXCEPTION}</li>
 * <li>{@link ACRAConstants#EXTRA_REPORT_CONFIG}</li>
 * <li>{@link ACRAConstants#EXTRA_FORCE_CANCEL} (optional)</li>
 * </ol>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseCrashReportDialog extends FragmentActivity {

    private File reportFile;
    private ACRAConfiguration config;
    private Throwable exception;

    /**
     * NB if you were previously creating and showing your dialog in this method,
     * you should move that code to {@link #init(Bundle)}.
     *
     * @param savedInstanceState    If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        preInit(savedInstanceState);
        super.onCreate(savedInstanceState);


        if (ACRA.DEV_LOGGING) {
            ACRA.log.d(LOG_TAG, "CrashReportDialog extras=" + getIntent().getExtras());
        }

        final Serializable sConfig = getIntent().getSerializableExtra(ACRAConstants.EXTRA_REPORT_CONFIG);
        final Serializable sReportFile = getIntent().getSerializableExtra(ACRAConstants.EXTRA_REPORT_FILE);
        final Serializable sException = getIntent().getSerializableExtra(ACRAConstants.EXTRA_REPORT_EXCEPTION);
        final boolean forceCancel = getIntent().getBooleanExtra(ACRAConstants.EXTRA_FORCE_CANCEL, false);

        if (forceCancel) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Forced reports deletion.");
            cancelReports();
            finish();
        } else if ((sConfig instanceof ACRAConfiguration) && (sReportFile instanceof File) && ((sException instanceof Throwable) || sException == null)) {
            config = (ACRAConfiguration) sConfig;
            reportFile = (File) sReportFile;
            exception = (Throwable) sException;
            init(savedInstanceState);
        } else {
            ACRA.log.w(LOG_TAG, "Illegal or incomplete call of BaseCrashReportDialog.");
            finish();
        }
    }

    /**
     * Handle any necessary pre-onCreate() setup here.
     *
     * @param savedInstanceState    If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    protected void preInit(@Nullable Bundle savedInstanceState) {
    }

    /**
     * Responsible for creating and showing the crash report dialog.
     *
     * @param savedInstanceState    If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    protected void init(@Nullable Bundle savedInstanceState) {
    }


    /**
     * Cancel any pending crash reports.
     */
    protected final void cancelReports() {
        new BulkReportDeleter(getApplicationContext()).deleteReports(false, 0);
    }


    /**
     * Send crash report given user's comment and email address. If none should be empty strings
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the client.
     */
    protected final void sendCrash(@Nullable String comment, @Nullable String userEmail) {
        final CrashReportPersister persister = new CrashReportPersister();
        try {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Add user comment to " + reportFile);
            final CrashReportData crashData = persister.load(reportFile);
            crashData.putString(USER_COMMENT, comment == null ? "" : comment);
            crashData.putString(USER_EMAIL, userEmail == null ? "" : userEmail);
            persister.store(crashData, reportFile);
        } catch (IOException e) {
            ACRA.log.w(LOG_TAG, "User comment not added: ", e);
        } catch (JSONException e) {
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

    protected final ACRAConfiguration getConfig() {
        return config;
    }

    protected final Throwable getException() {
        return exception;
    }
}
