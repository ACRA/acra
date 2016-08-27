package org.acra.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.acra.ACRAConstants;
import org.acra.config.ACRAConfiguration;

/**
 * Activity which implements the base functionality for a CrashReportDialog.
 * <p>
 * Activities which extend from this class can override init to create a custom view.
 * <p>
 * The methods sendCrash(comment, userEmail) and cancelReports() can be used to send or cancel
 * sending of reports respectively.
 * <p>
 * This Activity will be instantiated with 3 (or 4) arguments:
 * <ol>
 * <li>{@link ACRAConstants#EXTRA_REPORT_FILE_NAME}</li>
 * <li>{@link ACRAConstants#EXTRA_REPORT_EXCEPTION}</li>
 * <li>{@link ACRAConstants#EXTRA_REPORT_CONFIG}</li>
 * <li>{@link ACRAConstants#EXTRA_FORCE_CANCEL} (optional)</li>
 * </ol>
 */

public abstract class BaseCrashReportDialog extends Activity implements ICrashReportDialog {
    private final CrashReportDelegate delegate;

    public BaseCrashReportDialog() {
        delegate = new CrashReportDelegate(this);
    }

    /**
     * NB if you were previously creating and showing your dialog in this method,
     * you should move that code to {@link #init(Bundle)}.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (delegate.loadFromIntent(getIntent())) {
            init(savedInstanceState);
        } else {
            finish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelReports() {
        delegate.cancelReports();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendCrash(@Nullable String comment, @Nullable String userEmail) {
        delegate.sendCrash(comment, userEmail);
    }

    @Override
    public ACRAConfiguration getConfig() {
        return delegate.getConfig();
    }

    @Override
    public Throwable getException() {
        return delegate.getException();
    }
}
