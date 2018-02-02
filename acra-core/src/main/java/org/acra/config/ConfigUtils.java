/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.config;

import android.support.annotation.NonNull;

/**
 * Allows easy access to Plugin configurations from the main configuration
 *
 * @author F43nd1r
 * @since 01.06.2017
 */
public final class ConfigUtils {

    @NonNull
    public static <T extends Configuration> T getPluginConfiguration(@NonNull CoreConfiguration config, @NonNull Class<T> c) {
        for (Configuration configuration : config.pluginConfigurations()) {
            if (c.isAssignableFrom(configuration.getClass())) {
                //noinspection unchecked
                return (T) configuration;
            }
        }
        throw new IllegalArgumentException(c.getName() + " is no registered configuration");
    }

}
