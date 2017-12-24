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

package org.acra.log;

import android.support.annotation.Nullable;

/**
 * Responsible for providing ACRA classes with a platform neutral way of logging.
 * <p>
 *     One reason for using this mechanism is to allow ACRA classes to use a logging system,
 *     but be able to execute in a test environment outside of an Android JVM.
 * </p>
 * @author William Ferguson
 * @since 4.3.0
 */
public interface ACRALog {
    int v(java.lang.String tag, java.lang.String msg);
    int v(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    int d(java.lang.String tag, java.lang.String msg);
    int d(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    int i(java.lang.String tag, java.lang.String msg);
    int i(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    int w(java.lang.String tag, java.lang.String msg);
    int w(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    //public native  boolean isLoggable(java.lang.String tag, int level);
    int w(java.lang.String tag, java.lang.Throwable tr);
    int e(java.lang.String tag, java.lang.String msg);
    int e(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    @Nullable
    java.lang.String getStackTraceString(java.lang.Throwable tr);
    //public native  int println(int priority, java.lang.String tag, java.lang.String msg);
}
