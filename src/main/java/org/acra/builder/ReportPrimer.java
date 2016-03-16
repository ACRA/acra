package org.acra.builder;

import android.content.Context;

import java.util.Map;

/**
 * Primes a {@link ReportBuilder} with any extra data to record for the current crash report.
 *
 * ReportPrimer is configured declaratively via {@link org.acra.annotation.ReportsCrashes#reportPrimerClass()}.
 * The ReportPrimer class MUST have a no arg constructor and is created when ACRA is initialised.
 *
 * Created by William on 9 Jan 2016.
 * @since 4.8.0
 */
public interface ReportPrimer {

    /**
     * Update builder via {@link ReportBuilder#customData(Map)} or {@link ReportBuilder#customData(String, String)}
     * with any extra dta application to just this crash.
     *
     * Builder is fully constructed when this method is called, so it can be introspected for details of the crash.
     *
     * Note that this method will only be called if ACRA is currently enabled.
     *
     * @param context   Application context from which to retrieve resources.
     * @param builder   Full configured {@link ReportBuilder} for the current crash report.
     */
    void primeReport(Context context, ReportBuilder builder);
}
