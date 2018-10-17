/*
 * Copyright (c) 2018
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

package org.acra.plugins;

import android.support.annotation.NonNull;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.util.Predicate;

import java.util.*;

import static org.acra.ACRA.LOG_TAG;

/**
 * Utility to load {@link Plugin}s
 *
 * @author F43nd1r
 * @since 18.04.18
 */
public class ServicePluginLoader implements PluginLoader {

    @Override
    public <T extends Plugin> List<T> load(@NonNull Class<T> clazz) {
        return loadInternal(clazz, plugin -> true);
    }

    @Override
    public <T extends Plugin> List<T> loadEnabled(@NonNull CoreConfiguration config, @NonNull Class<T> clazz) {
        return loadInternal(clazz, plugin -> plugin.enabled(config));
    }

    private <T extends Plugin> List<T> loadInternal(@NonNull Class<T> clazz, Predicate<T> shouldLoadPredicate) {
        List<T> plugins = new ArrayList<>();
        //noinspection ForLoopReplaceableByForEach
        final ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz, getClass().getClassLoader());
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "ServicePluginLoader loading services from ServiceLoader : " + serviceLoader);

        for (final Iterator<T> iterator = serviceLoader.iterator(); iterator.hasNext(); ) {
            try {
                final T plugin = iterator.next();
                if (shouldLoadPredicate.apply(plugin)) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(ACRA.LOG_TAG, "Loaded " + clazz.getSimpleName() + " of type " + plugin.getClass().getName());
                    plugins.add(plugin);
                } else {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Ignoring disabled " + clazz.getSimpleName() + " of type " + plugin.getClass().getSimpleName());
                }
            } catch (ServiceConfigurationError e) {
                ACRA.log.e(ACRA.LOG_TAG, "Unable to load " + clazz.getSimpleName(), e);
            }
        }
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Found services (" + plugins + ") for class : " + clazz);
        return plugins;
    }
}
