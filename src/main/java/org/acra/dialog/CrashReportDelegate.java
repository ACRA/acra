package org.acra.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
 * Created on 03.08.2016.
 *
 * @author F43nd1r
 */
public class CrashReportDelegate implements ICrashReportDialog {

    private final Context context;
    private File reportFile;
    private ACRAConfiguration config;
    private Throwable exception;

    public CrashReportDelegate(Context context) {
        this.context = context;
    }

    /**
     * Initializes this instance with data from the intent
     *
     * @param intent Intent which started the dialog
     * @return if the activity should continue
     */
    public final boolean loadFromIntent(@NonNull Intent intent) {


        if (ACRA.DEV_LOGGING) {
            ACRA.log.d(LOG_TAG, "CrashReportDialog extras=" + intent.getExtras());
        }

        final Serializable sConfig = intent.getSerializableExtra(ACRAConstants.EXTRA_REPORT_CONFIG);
        final Serializable sReportFile = intent.getSerializableExtra(ACRAConstants.EXTRA_REPORT_FILE);
        final Serializable sException = intent.getSerializableExtra(ACRAConstants.EXTRA_REPORT_EXCEPTION);
        final boolean forceCancel = intent.getBooleanExtra(ACRAConstants.EXTRA_FORCE_CANCEL, false);

        if (forceCancel) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Forced reports deletion.");
            cancelReports();
            return false;
        } else if ((sConfig instanceof ACRAConfiguration) && (sReportFile instanceof File) && ((sException instanceof Throwable) || sException == null)) {
            config = (ACRAConfiguration) sConfig;
            reportFile = (File) sReportFile;
            exception = (Throwable) sException;
            return true;
        } else {
            ACRA.log.w(LOG_TAG, "Illegal or incomplete call of BaseCrashReportDialog.");
            return false;
        }
    }


    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        //no-op
    }

    /**
     * Cancel any pending crash reports.
     */
    @Override
    public final void cancelReports() {
        new BulkReportDeleter(context).deleteReports(false, 0);
    }


    /**
     * Send crash report given user's comment and email address. If none should be empty strings
     *
     * @param comment   Comment (may be null) provided by the user.
     * @param userEmail Email address (may be null) provided by the client.
     */
    @Override
    public final void sendCrash(@Nullable String comment, @Nullable String userEmail) {
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
        final SenderServiceStarter starter = new SenderServiceStarter(context, config);
        starter.startService(false, true);

        // Optional Toast to thank the user
        final int toastId = config.resDialogOkToast();
        if (toastId != 0) {
            ToastSender.sendToast(context, toastId, Toast.LENGTH_LONG);
        }
    }

    @Override
    public final ACRAConfiguration getConfig() {
        return config;
    }

    @Override
    public final Throwable getException() {
        return exception;
    }
}
