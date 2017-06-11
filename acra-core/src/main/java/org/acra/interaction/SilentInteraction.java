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

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;

import java.io.File;

/**
 * @author F43nd1r
 * @since 02.06.2017
 */
@AutoService(ReportInteraction.class)
public class SilentInteraction implements ReportInteraction {

    @Override
    public boolean performInteraction(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull File reportFile) {
        return true;
    }
}
