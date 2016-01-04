package org.acra.sender;

import android.content.Context;

/**
 * Constructs an {@link EmailIntentSender}.
 */
public final class EmailIntentSenderFactory implements ReportSenderFactory {

    @Override
    public ReportSender create(Context context) {
        return new EmailIntentSender(context);
    }
}
