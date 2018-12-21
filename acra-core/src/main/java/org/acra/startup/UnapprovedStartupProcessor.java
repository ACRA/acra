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

package org.acra.startup;

import android.content.Context;
import android.support.annotation.NonNull;
import com.google.auto.service.AutoService;
import org.acra.config.CoreConfiguration;
import org.acra.file.LastModifiedComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lukas
 * @since 15.09.18
 */
@AutoService(StartupProcessor.class)
public class UnapprovedStartupProcessor implements StartupProcessor {
    @Override
    public void processReports(@NonNull Context context, @NonNull CoreConfiguration config, List<Report> reports) {
        if (config.deleteUnapprovedReportsOnApplicationStart()) {
            final List<Report> sort = new ArrayList<>();
            for (Report report : reports) {
                if (!report.isApproved()) {
                    sort.add(report);
                }
            }
            if (!sort.isEmpty()) {
                final LastModifiedComparator comparator = new LastModifiedComparator();
                Collections.sort(sort, (r1, r2) -> comparator.compare(r1.getFile(), r2.getFile()));
                if(config.deleteUnapprovedReportsOnApplicationStart()) {
                    for (int i = 0; i < sort.size() - 1; i++) {
                        sort.get(i).delete();
                    }
                }
                sort.get(sort.size() - 1).approve();
            }
        }
    }
}
