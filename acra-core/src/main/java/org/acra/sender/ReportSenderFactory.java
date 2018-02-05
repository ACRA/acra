/*
 * Copyright (c) 2017 the ACRA team
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

package org.acra.sender;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import org.acra.config.CoreConfiguration;

/**
 * Factory for creating and configuring a {@link ReportSender} instance.
 * Implementations must have a no argument constructor.
 * <p>
 * Each configured ReportSenderFactory is created within the {@link SenderService}
 * and is used to construct and configure a single {@link ReportSender}.
 * <p>
 * Created by William on 4-JAN-2016.
 */
@Keep
public interface ReportSenderFactory {

    /**
     * @param context a context.
     * @param config  Configuration to use when sending reports.
     * @return Fully configured instance of the relevant ReportSender.
     */
    @NonNull
    ReportSender create(@NonNull Context context, @NonNull CoreConfiguration config);

    /**
     * controls if this instance is active
     *
     * @param config the current config
     * @return if this instance should be called
     */
    default boolean enabled(@NonNull CoreConfiguration config) {
        return true;
    }
}
