package org.acra.sender;

import android.content.Context;
import org.acra.config.ACRAConfig;

/**
 * Constructs a {@link HttpSender} with no report field mappings.
 */
public final class HttpSenderFactory implements ReportSenderFactory {

    @Override
    public ReportSender create(Context context, ACRAConfig config) {
        return new HttpSender(config, config.httpMethod(), config.reportType(), null);
    }
}
