package org.acra.plugins;

import android.support.annotation.NonNull;
import org.acra.config.CoreConfiguration;

/**
 * Marker interface for ACRA plugins that can be disabled
 *
 * @author F43nd1r
 * @since 18.04.2018
 */
public interface AllowsDisablePlugin extends Plugin {


    /**
     * controls if this instance is active
     *
     * @param config the current config
     * @return if this instance should be called
     */
    boolean enabled(@NonNull CoreConfiguration config);
}
