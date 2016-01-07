package org.acra.sender;

import android.content.Context;
import org.acra.config.ACRAConfigX;

/**
 * Constructs an {@link EmailIntentSender}.
 */
public final class EmailIntentSenderFactory implements ReportSenderFactory {

    @Override
    public ReportSender create(Context context, ACRAConfigX config) {
        return new EmailIntentSender(context, config);
    }
}
