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


import android.util.Log;

/**
 * Responsible for delegating calls to the Android logging system.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
public final class AndroidLogDelegate implements ACRALog {
    @Override
    public int v(String tag, String msg) {
        return Log.v(tag, msg);
    }
    @Override
    public int v(String tag, String msg, Throwable tr) {
        return Log.v(tag, msg, tr);
    }
    @Override
    public int d(String tag, String msg) {
        return Log.d(tag, msg);
    }
    @Override
    public int d(String tag, String msg, Throwable tr) {
        return Log.d(tag, msg, tr);
    }
    @Override
    public int i(String tag, String msg) {
        return Log.i(tag, msg);
    }
    @Override
    public int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }
    @Override
    public int w(String tag, String msg) {
        return Log.w(tag, msg);
    }
    @Override
    public int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }
    //public native  boolean isLoggable(java.lang.String tag, int level);
    @Override
    public int w(String tag, Throwable tr) {
        return Log.w(tag, tr);
    }
    @Override
    public int e(String tag, String msg) {
        return Log.e(tag, msg);
    }
    @Override
    public int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }
    @Override
    public String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }
    //public native  int println(int priority, java.lang.String tag, java.lang.String msg);
}
