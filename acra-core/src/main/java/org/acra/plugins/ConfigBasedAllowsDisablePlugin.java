package org.acra.plugins;

import android.support.annotation.NonNull;
import org.acra.config.ConfigUtils;
import org.acra.config.Configuration;
import org.acra.config.CoreConfiguration;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
public abstract class ConfigBasedAllowsDisablePlugin implements AllowsDisablePlugin {
    private final Class<? extends Configuration> configClass;

    public ConfigBasedAllowsDisablePlugin(Class<? extends Configuration> configClass) {
        this.configClass = configClass;
    }

    @Override
    public final boolean enabled(@NonNull CoreConfiguration config) {
        return ConfigUtils.getPluginConfiguration(config, configClass).enabled();
    }
}
