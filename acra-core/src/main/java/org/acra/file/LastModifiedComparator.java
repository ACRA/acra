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

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Comparator;

/**
 * Orders files from oldest to newest based on their last modified date.
 */
final class LastModifiedComparator implements Comparator<File> {
    @Override
    public int compare(@NonNull File lhs, @NonNull File rhs) {
        final long l = lhs.lastModified();
        final long r = rhs.lastModified();
        return l < r ? -1 : (l == r ? 0 : 1);
    }
}
