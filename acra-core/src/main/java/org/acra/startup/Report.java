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

import java.io.File;

/**
 * @author lukas
 * @since 15.09.18
 */
public class Report {
    private final File file;
    private final boolean approved;
    private boolean delete;
    private boolean approve;

    public Report(File file, boolean approved) {
        this.file = file;
        this.approved = approved;
        delete = false;
        approve = false;
    }

    public File getFile() {
        return file;
    }

    public boolean isApproved() {
        return approved;
    }

    public void delete() {
        delete = true;
    }

    public void approve() {
        approve = true;
    }

    boolean isDelete() {
        return delete;
    }

    boolean isApprove() {
        return approve;
    }
}
