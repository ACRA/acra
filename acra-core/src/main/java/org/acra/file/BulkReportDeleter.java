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

package org.acra.file;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;

import java.io.File;
import java.util.Arrays;

import static org.acra.ACRA.LOG_TAG;

/**
 * Deletes unsent reports.
 */
public final class BulkReportDeleter {

    @NonNull
    private final ReportLocator reportLocator;

    public BulkReportDeleter(@NonNull Context context) {
        this.reportLocator = new ReportLocator(context);
    }

    /**
     * @param approved  Whether to delete approved or unapproved reports.
     * @param nrToKeep  Number of latest reports to keep.
     */
    public void deleteReports(boolean approved, int nrToKeep) {
        final File[] files = approved ? reportLocator.getApprovedReports() : reportLocator.getUnapprovedReports();

        Arrays.sort(files, new LastModifiedComparator());

        for (int i = 0; i < files.length - nrToKeep; i++) {
            if (!files[i].delete()) {
                ACRA.log.w(LOG_TAG, "Could not delete report : " + files[i]);
            }
        }
    }
}
