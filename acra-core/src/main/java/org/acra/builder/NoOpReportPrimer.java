package org.acra.builder;

import android.content.Context;

import org.acra.config.CoreConfiguration;

/**
 * Does not perform any priming for the current report.
 *
 * @since 4.8.0
 */
public final class NoOpReportPrimer implements ReportPrimer {

    @Override
    public void primeReport(Context context, CoreConfiguration configuration, ReportBuilder builder) {
        // Nothing to do.
    }
}
