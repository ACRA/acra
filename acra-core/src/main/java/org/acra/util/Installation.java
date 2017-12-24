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

/*
 * Class copied from the Android Developers Blog:
 * http://android-developers.blogspot.com/2011/03/identifying-app-installations.html 
 */
package org.acra.util;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.acra.ACRA.LOG_TAG;

/**
 * <p>
 * Creates a file storing a UUID on the first application start. This UUID can then be used as a identifier of this
 * specific application installation.
 * </p>
 * <p>
 * This was taken from <a href="http://android-developers.blogspot.com/2011/03/identifying-app-installations.html"> the
 * android developers blog.</a>
 * </p>
 */
public final class Installation {
    private Installation() {
    }

    private static final String INSTALLATION = "ACRA-INSTALLATION";

    @NonNull
    public static synchronized String id(@NonNull Context context) {
        final File installation = new File(context.getFilesDir(), INSTALLATION);
        try {
            if (!installation.exists()) {
                IOUtils.writeStringToFile(installation, UUID.randomUUID().toString());
            }
            return new StreamReader(installation).read();
        } catch (IOException | RuntimeException e) {
            ACRA.log.w(LOG_TAG, "Couldn't retrieve InstallationId for " + context.getPackageName(), e);
            return "Couldn't retrieve InstallationId";
        }
    }
}
