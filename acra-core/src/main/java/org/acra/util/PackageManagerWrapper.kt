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
package org.acra.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import org.acra.log.warn

/**
 * Responsible for wrapping calls to PackageManager to ensure that they always complete without throwing RuntimeExceptions.
 * Depending upon the state of the application it is possible that
 *
 *  * Context has no PackageManager.
 *  * PackageManager returned by Context throws RuntimeException("Package manager has died")
 * because it cannot contact the remote PackageManager service.
 *
 *
 * I suspect that PackageManager death is caused during app installation.
 * But we need to make ACRA bullet proof, so it's better to handle the condition safely so that the error report itself doesn't fail.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
class PackageManagerWrapper(private val context: Context) {
    /**
     * @param permission Manifest.permission to check whether it has been granted.
     * @return true if the permission has been granted to the app, false if it hasn't been granted or the PackageManager could not be contacted.
     */
    fun hasPermission(permission: String): Boolean {
        val pm = context.packageManager ?: return false
        return try {
            pm.checkPermission(permission, context.packageName) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            // To catch RuntimeException("Package manager has died") that can occur on some version of Android,
            // when the remote PackageManager is unavailable. I suspect this sometimes occurs when the App is being reinstalled.
            false
        }
    }

    /**
     * @return PackageInfo for the current application or null if the PackageManager could not be contacted.
     */
    fun getPackageInfo(): PackageInfo? {
        val pm = context.packageManager ?: return null
        return try {
            pm.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            warn { "Failed to find PackageInfo for current App : ${context.packageName}" }
            null
        } catch (e: Exception) {
            // To catch RuntimeException("Package manager has died") that can occur on some version of Android,
            // when the remote PackageManager is unavailable. I suspect this sometimes occurs when the App is being reinstalled.
            null
        }
    }
}