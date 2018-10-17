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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author lukas
 * @since 01.07.18
 */
public class SimplePluginLoader implements PluginLoader {

    private final Class<? extends Plugin>[] plugins;

    @SafeVarargs
    public SimplePluginLoader(@NonNull Class<? extends Plugin>... plugins) {
        this.plugins = plugins;
    }

    @Override
    public <T extends Plugin> List<T> load(@NonNull Class<T> clazz) {
        List<T> list = new ArrayList<>();
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "SimplePluginLoader loading services from plugin classes : " + plugins);
        for (Class<? extends Plugin> plugin : plugins) {
            if (clazz.isAssignableFrom(plugin)) {
                try {
                    //noinspection unchecked
                    list.add((T) plugin.newInstance());
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Loaded plugin from class : " + plugin);
                } catch (Exception e) {
                    if (ACRA.DEV_LOGGING) ACRA.log.w(LOG_TAG, "Could not load plugin from class : " + plugin, e);
                }
            }
        }
        return list;
    }

    @Override
    public <T extends Plugin> List<T> loadEnabled(@NonNull CoreConfiguration config, @NonNull Class<T> clazz) {
        List<T> list = load(clazz);
        //noinspection Java8CollectionRemoveIf
        for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); ) {
            T plugin = iterator.next();
            if (!plugin.enabled(config)) {
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Removing disabled plugin : " + plugin);
                iterator.remove();
            }
        }
        return list;
    }
}
