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
package org.acra.file

import android.content.Context
import java.io.File

/**
 * Locates crash reports.
 *
 * @author William Ferguson
 * @since 4.8.0
 */
class ReportLocator(private val context: Context) {
    val unapprovedFolder: File
        get() = context.getDir(UNAPPROVED_FOLDER_NAME, Context.MODE_PRIVATE)
    val unapprovedReports: Array<File>
        get() = unapprovedFolder.listFiles() ?: emptyArray()
    val approvedFolder: File
        get() = context.getDir(APPROVED_FOLDER_NAME, Context.MODE_PRIVATE)
    val approvedReports: Array<File>
            get() = approvedFolder.listFiles()?.sortedBy { it.lastModified() }?.toTypedArray() ?: emptyArray()

    companion object {
        // Folders under the app folder.
        private const val UNAPPROVED_FOLDER_NAME = "ACRA-unapproved"
        private const val APPROVED_FOLDER_NAME = "ACRA-approved"
    }
}