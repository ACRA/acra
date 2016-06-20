package org.acra.sender;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.util.PackageManagerWrapper;

import static org.acra.ACRA.LOG_TAG;

/**
 * Will send reports by email if the 'mailTo' parameter is configured,
 * otherwise via HTTP if the 'formUri' parameter is configured and
 * internet permission has been granted.
 *
 * If neither 'formUri' or 'mailTo' has been configured, then a NullSender will be returned.
 */
public final class DefaultReportSenderFactory implements ReportSenderFactory {

    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull ACRAConfiguration config) {
        final PackageManagerWrapper pm = new PackageManagerWrapper(context);
        if (!"".equals(config.mailTo())) {
            // Try to send by mail. If a mailTo address is provided, do not add other senders.
            ACRA.log.w(LOG_TAG, context.getPackageName() + " reports will be sent by email (if accepted by user).");
            return new EmailIntentSenderFactory().create(context, config);
        } else if (!pm.hasPermission(Manifest.permission.INTERNET)) {
            // NB If the PackageManager has died then this will erroneously log
            // the error that the App doesn't have Internet (even though it does).
            // I think that is a small price to pay to ensure that ACRA doesn't
            // crash if the PackageManager has died.
            ACRA.log.e(LOG_TAG,
                    context.getPackageName()
                            + " should be granted permission "
                            + Manifest.permission.INTERNET
                            + " if you want your crash reports to be sent. If you don't want to add this permission to your application you can also enable sending reports by email. If this is your will then provide your email address in @AcraConfig(mailTo=\"your.account@domain.com\"");
            return new NullSender();
        } else if (config.formUri() != null && !"".equals(config.formUri())) {
            // If formUri is set, instantiate a sender for a generic HTTP POST form with default mapping.
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, context.getPackageName() + " reports will be sent by Http.");
            return new HttpSenderFactory().create(context, config);
        } else {
            return new NullSender();
        }
    }
}
