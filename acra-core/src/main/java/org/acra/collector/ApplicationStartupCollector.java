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

package org.acra.collector;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.CoreConfiguration;

/**
 * A collector that is also called at startup
 *
 * @author F43nd1r
 * @since 29.09.2017
 */

@SuppressWarnings("WeakerAccess")
public interface ApplicationStartupCollector extends Collector {
    /**
     * collect startup data
     *
     * @param context a context
     * @param config  the config
     */
    void collectApplicationStartUp(@NonNull Context context, @NonNull CoreConfiguration config);
}
