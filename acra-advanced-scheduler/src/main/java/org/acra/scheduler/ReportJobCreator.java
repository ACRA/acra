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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
public class ReportJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        if (ReportJob.TAG.equals(tag)) {
            return new ReportJob();
        }
        return null;
    }
}
