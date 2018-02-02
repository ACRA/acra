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

package org.acra.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for wrapping calls to PackageManager to ensure that they always complete without throwing RuntimeExceptions.
 * Depending upon the state of the application it is possible that
 *     <ul>
 *         <li>Context has no PackageManager.</li>
 *         <li>PackageManager returned by Context throws RuntimeException("Package manager has died")
 *             because it cannot contact the remote PackageManager service.
 *         </li>
 *     </ul>
 * I suspect that PackageManager death is caused during app installation.
 * But we need to make ACRA bullet proof, so it's better to handle the condition safely so that the error report itself doesn't fail.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
public final class PackageManagerWrapper {

    @NonNull
    private final Context context;

    public PackageManagerWrapper(@NonNull Context context) {
        this.context = context;
    }

    /**
     * @param permission Manifest.permission to check whether it has been granted.
     * @return true if the permission has been granted to the app, false if it hasn't been granted or the PackageManager could not be contacted.
     */
    public boolean hasPermission(@NonNull String permission) {
        final PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }

        try {
            return pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable e) {
            // To catch RuntimeException("Package manager has died") that can occur on some version of Android,
            // when the remote PackageManager is unavailable. I suspect this sometimes occurs when the App is being reinstalled.
            return false;
        }
    }

    /**
     * @return PackageInfo for the current application or null if the PackageManager could not be contacted.
     */
    @Nullable
    public PackageInfo getPackageInfo() {
        final PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }

        try {
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            ACRA.log.w(LOG_TAG, "Failed to find PackageInfo for current App : " + context.getPackageName());
            return null;
        } catch (Throwable e) {
            // To catch RuntimeException("Package manager has died") that can occur on some version of Android,
            // when the remote PackageManager is unavailable. I suspect this sometimes occurs when the App is being reinstalled.
            return null;
        }
    }
}
