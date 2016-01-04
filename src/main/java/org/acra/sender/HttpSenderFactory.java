package org.acra.sender;

import android.content.Context;
import org.acra.ACRA;

/**
 * Constructs a {@link HttpSender} with no report field mappings.
 */
public final class HttpSenderFactory implements ReportSenderFactory {
    @Override
    public ReportSender create(Context context) {
        return new HttpSender(ACRA.getConfig().httpMethod(), ACRA.getConfig().reportType(), null);
    }
}
