package org.acra.sender;

import android.content.Context;
import org.acra.config.ACRAConfig;

/**
 * Constructs an {@link EmailIntentSender}.
 */
public final class EmailIntentSenderFactory implements ReportSenderFactory {

    @Override
    public ReportSender create(Context context, ACRAConfig config) {
        return new EmailIntentSender(context, config);
    }
}
