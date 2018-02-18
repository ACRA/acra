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
package org.acra.file;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author F43nd1r
 * @since 4.9.1
 */
public enum Directory {
    /**
     * Legacy behaviour:
     * If the string starts with a path separator, this behaves like {@link #ROOT}.
     * Otherwise it behaves like {@link #FILES}.
     */
    FILES_LEGACY {
        @NonNull
        @Override
        public File getFile(@NonNull Context context, @NonNull String fileName) {
            return (fileName.startsWith("/") ? Directory.ROOT : Directory.FILES).getFile(context, fileName);
        }
    },
    /**
     * Directory returned by {@link Context#getFilesDir()}
     */
    FILES {
        @NonNull
        @Override
        public File getFile(@NonNull Context context, @NonNull String fileName) {
            return new File(context.getFilesDir(), fileName);
        }
    },
    /**
     * Directory returned by {@link Context#getExternalFilesDir(String)}
     */
    EXTERNAL_FILES {
        @NonNull
        @Override
        public File getFile(@NonNull Context context, @NonNull String fileName) {
            return new File(context.getExternalFilesDir(null), fileName);
        }
    },
    /**
     * Directory returned by {@link Context#getCacheDir()}
     */
    CACHE {
        @NonNull
        @Override
        public File getFile(@NonNull Context context, @NonNull String fileName) {
            return new File(context.getCacheDir(), fileName);
        }
    },
    /**
     * Directory returned by {@link Context#getExternalCacheDir()}
     */
    EXTERNAL_CACHE {
        @NonNull
        @Override
        public File getFile(@NonNull Context context, @NonNull String fileName) {
            return new File(context.getExternalCacheDir(), fileName);
        }
    },
    /**
     * Directory returned by {@link Context#getNoBackupFilesDir()}.
     * Will fall back to {@link Context#getFilesDir()} on API &lt; 21
     */
    NO_BACKUP_FILES {
        @NonNull
        @Override
        public File getFile(@NonNull Context context, @NonNull String fileName) {
            return new File(ContextCompat.getNoBackupFilesDir(context), fileName);
        }
    },
    /**
     * Directory returned by {@link Environment#getExternalStorageDirectory()}
     */
    EXTERNAL_STORAGE {
        @NonNull
        @Override
        public File getFile(@NonNull Context context, @NonNull String fileName) {
            return new File(Environment.getExternalStorageDirectory(), fileName);
        }
    },
    /**
     * Root Directory, paths in this directory are absolute paths
     */
    ROOT {
        @NonNull
        @Override
        public File getFile(@NonNull Context context, @NonNull String fileName) {
            String[] parts = fileName.split(Pattern.quote(File.separator), 2);
            if (parts.length == 1) return new File(fileName);
            final File[] roots = File.listRoots();
            for (File root : roots) {
                if (parts[0].equals(root.getPath().replace(File.separator, ""))) {
                    return new File(root, parts[1]);
                }
            }
            return new File(roots[0], fileName);
        }
    };

    @NonNull
    public abstract File getFile(@NonNull Context context, @NonNull String fileName);
}
