package org.acra.sender;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.config.ACRAConfiguration;

/**
 * Constructs a {@link HttpSender} with no report field mappings.
 */
public final class HttpSenderFactory implements ReportSenderFactory {

    @Nullable
    @Override
    public ReportSender create(Context context, @NonNull ACRAConfiguration config) {
        return new HttpSender(config, config.httpMethod(), config.reportType(), null);
    }
}
