/*
 * Copyright (c) 2016
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
import android.os.Build
import android.os.Environment
import java.io.File
import java.util.regex.Pattern

/**
 * @author F43nd1r
 * @since 4.9.1
 */
enum class Directory {
    /**
     * Legacy behaviour:
     * If the string starts with a path separator, this behaves like [.ROOT].
     * Otherwise it behaves like [.FILES].
     */
    FILES_LEGACY {
        override fun getFile(context: Context, fileName: String): File {
            return (if (fileName.startsWith("/")) ROOT else FILES).getFile(context, fileName)
        }
    },

    /**
     * Directory returned by [Context.getFilesDir]
     */
    FILES {
        override fun getFile(context: Context, fileName: String): File {
            return File(context.filesDir, fileName)
        }
    },

    /**
     * Directory returned by [Context.getExternalFilesDir]
     */
    EXTERNAL_FILES {
        override fun getFile(context: Context, fileName: String): File {
            return File(context.getExternalFilesDir(null), fileName)
        }
    },

    /**
     * Directory returned by [Context.getCacheDir]
     */
    CACHE {
        override fun getFile(context: Context, fileName: String): File {
            return File(context.cacheDir, fileName)
        }
    },

    /**
     * Directory returned by [Context.getExternalCacheDir]
     */
    EXTERNAL_CACHE {
        override fun getFile(context: Context, fileName: String): File {
            return File(context.externalCacheDir, fileName)
        }
    },

    /**
     * Directory returned by [Context.getNoBackupFilesDir].
     * Will fall back to [Context.getFilesDir] on API &lt; 21
     */
    NO_BACKUP_FILES {
        override fun getFile(context: Context, fileName: String): File {
            val dir: File = if (Build.VERSION.SDK_INT >= 21) {
                context.noBackupFilesDir
            } else {
                File(context.applicationInfo.dataDir, "no_backup")
            }
            return File(dir, fileName)
        }
    },

    /**
     * Directory returned by [Environment.getExternalStorageDirectory]
     */
    EXTERNAL_STORAGE {
        @Suppress("DEPRECATION")
        override fun getFile(context: Context, fileName: String): File {
            return File(Environment.getExternalStorageDirectory(), fileName)
        }
    },

    /**
     * Root Directory, paths in this directory are absolute paths
     */
    ROOT {
        override fun getFile(context: Context, fileName: String): File {
            val parts = fileName.split(File.separator, limit = 2)
            if (parts.size == 1) return File(fileName)
            val roots = File.listRoots()
            for (root in roots) {
                if (parts[0] == root.path.replace(File.separator, "")) {
                    return File(root, parts[1])
                }
            }
            return File(roots[0], fileName)
        }
    };

    abstract fun getFile(context: Context, fileName: String): File
}