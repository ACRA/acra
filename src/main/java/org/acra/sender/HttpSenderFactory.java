package org.acra.sender;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.ACRAConfiguration;

/**
 * Constructs a {@link HttpSender} with no report field mappings.
 */
public final class HttpSenderFactory implements ReportSenderFactory {

    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull ACRAConfiguration config) {
        return new HttpSender(config, config.httpMethod(), config.reportType(), null);
    }
}
