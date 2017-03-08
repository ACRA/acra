package org.acra;

import android.app.Application;
import android.content.Context;

import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigurationBuilder;

/**
 * Users can extend this class instead of Application
 * Introduced to reduce duplicate code across projects
 */
@SuppressWarnings("unused")
public class ACRAApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this, buildConfiguration(new ConfigurationBuilder(this)));
    }

    /**
     * Override this to make changes to the configuration
     * @param configurationBuilder a configurationBuilder with values from the annotation (if any)
     * @return the final Configuration
     */
    protected ACRAConfiguration buildConfiguration(ConfigurationBuilder configurationBuilder) {
        return configurationBuilder.build();
    }
}
