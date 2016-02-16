package org.acra.config;

import android.app.Application;
import android.support.annotation.NonNull;

import org.acra.annotation.ReportsCrashes;

/**
 * Creates an {@link ACRAConfiguration} for the Application.
 *
 * @deprecated since 4.8.1 - use {@link ConfigurationBuilder} instead.
 */
public final class ACRAConfigurationFactory {
    /**
     * @param app   Your Application class.
     * @return new {@link ACRAConfiguration} instance with values initialized from the {@link ReportsCrashes} annotation.
     *
     * @deprecated since 4.8.1 use {@link ConfigurationBuilder#build} instead.
     */
    @NonNull
    @SuppressWarnings( "unused" )
    public ACRAConfiguration create(@NonNull Application app) {
        return new ConfigurationBuilder(app).build();
    }
}
