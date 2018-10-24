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

import android.content.Context;
import android.support.annotation.NonNull;
import org.acra.config.CoreConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public <T extends Plugin> List<T> load(@NonNull Context context, @NonNull Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (Class<? extends Plugin> plugin : plugins) {
            if (clazz.isAssignableFrom(plugin)) {
                try {
                    //noinspection unchecked
                    list.add((T) plugin.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    @Override
    public <T extends Plugin> List<T> loadEnabled(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull Class<T> clazz) {
        List<T> list = load(context, clazz);
        //noinspection Java8CollectionRemoveIf
        for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); ) {
            if (!iterator.next().enabled(config)) {
                iterator.remove();
            }
        }
        return list;
    }
}
