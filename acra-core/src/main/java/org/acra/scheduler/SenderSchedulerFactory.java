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

package org.acra.scheduler;

import android.content.Context;
import android.support.annotation.NonNull;
import org.acra.config.CoreConfiguration;
import org.acra.plugins.Plugin;

/**
 * @author F43nd1r
 * @since 20.04.18
 */
public interface SenderSchedulerFactory extends Plugin {


    /**
     * @param context a context.
     * @param config  Configuration to use when sending reports.
     * @return Fully configured instance of the relevant SenderScheduler.
     */
    @NonNull
    SenderScheduler create(@NonNull Context context, @NonNull CoreConfiguration config);
}
