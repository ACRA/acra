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

package org.acra.sender;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.config.CoreConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import static org.acra.ACRA.DEV_LOGGING;
import static org.acra.ACRA.LOG_TAG;

/**
 * Will auto-discover other ReportSenderFactories. If exactly one is found, delegates to that one. Otherwise returns a {@link NullSender}
 *
 * @author F43nd1r
 */
public final class DefaultReportSenderFactory implements ReportSenderFactory {

    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull CoreConfiguration config) {
        final List<ReportSenderFactory> factoryList = new ArrayList<>();
        //noinspection ForLoopReplaceableByForEach
        for (final Iterator<ReportSenderFactory> iterator = ServiceLoader.load(ReportSenderFactory.class, getClass().getClassLoader()).iterator(); iterator.hasNext(); ) {
            try {
                final ReportSenderFactory reportSenderFactory = iterator.next();
                if (reportSenderFactory.enabled(config)) {
                    factoryList.add(reportSenderFactory);
                } else if (ACRA.DEV_LOGGING) {
                    ACRA.log.d(LOG_TAG, "Ignoring disabled ReportSenderFactory of type " + reportSenderFactory.getClass().getSimpleName());
                }
            } catch (ServiceConfigurationError e) {
                ACRA.log.e(ACRA.LOG_TAG, "Unable to load ReportSenderFactory", e);
            }
        }
        if (factoryList.size() == 1) {
            if (DEV_LOGGING) ACRA.log.d(LOG_TAG, "Autodiscovered ReportSenderFactory of type " + factoryList.get(0).getClass().getSimpleName());
            return factoryList.get(0).create(context, config);
        } else if (factoryList.size() > 1) {
            ACRA.log.w(LOG_TAG, "Multiple ReportSenderFactories were discovered - please configure those you want to use. No reports will be sent");
        } else {
            ACRA.log.w(LOG_TAG, "No ReportSenderFactories were discovered. No reports will be sent");
        }
        return new NullSender();
    }
}
