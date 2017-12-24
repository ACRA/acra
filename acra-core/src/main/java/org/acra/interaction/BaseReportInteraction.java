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

package org.acra.interaction;

import android.support.annotation.NonNull;

import org.acra.config.ConfigUtils;
import org.acra.config.Configuration;
import org.acra.config.CoreConfiguration;

/**
 * @author F43nd1r
 * @since 18.10.2017
 */

public abstract class BaseReportInteraction implements ReportInteraction {
    private final Class<? extends Configuration> configClass;

    public BaseReportInteraction(Class<? extends Configuration> configClass) {
        this.configClass = configClass;
    }

    @Override
    public final boolean enabled(@NonNull CoreConfiguration config) {
        return ConfigUtils.getPluginConfiguration(config, configClass).enabled();
    }
}
