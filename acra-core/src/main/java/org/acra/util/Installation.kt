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
package org.acra.util

import android.content.Context
import org.acra.log.warn
import java.io.File
import java.io.IOException
import java.util.*

/**
 *
 *
 * Creates a file storing a UUID on the first application start. This UUID can then be used as a identifier of this
 * specific application installation.
 *
 *
 *
 * This was taken from [ the
 * android developers blog.](http://android-developers.blogspot.com/2011/03/identifying-app-installations.html)
 *
 */
object Installation {
    private const val INSTALLATION = "ACRA-INSTALLATION"

    @JvmStatic
    @Synchronized
    fun id(context: Context): String {
        val installation = File(context.filesDir, INSTALLATION)
        return try {
            if (!installation.exists()) {
                installation.writeText(UUID.randomUUID().toString())
            }
            installation.readText()
        } catch (e: IOException) {
            warn(e) { "Couldn't retrieve InstallationId for " + context.packageName }
            "Couldn't retrieve InstallationId"
        } catch (e: RuntimeException) {
            warn(e) { "Couldn't retrieve InstallationId for " + context.packageName }
            "Couldn't retrieve InstallationId"
        }
    }
}