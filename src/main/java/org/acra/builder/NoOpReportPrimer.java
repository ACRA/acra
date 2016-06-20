package org.acra.builder;

import android.content.Context;

/**
 * Does not perform any priming for the current report.
 *
 * @since 4.8.0
 */
public final class NoOpReportPrimer implements ReportPrimer {

    @Override
    public void primeReport(Context context, ReportBuilder builder) {
        // Nothing to do.
    }
}
