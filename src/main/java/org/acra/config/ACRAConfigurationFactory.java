package org.acra.config;

import android.app.Application;
import org.acra.annotation.ReportsCrashes;

/**
 * Creates an {@link ACRAConfiguration} for the Application.
 */
public final class ACRAConfigurationFactory {
    /**
     * @param app   Your Application class.
     * @return new {@link ACRAConfiguration} instance with values initialized from the {@link ReportsCrashes} annotation.
     */
    @SuppressWarnings( "unused" )
    public ACRAConfiguration create(Application app) {
        return new ACRAConfiguration(app.getClass().getAnnotation(ReportsCrashes.class));
    }
}
