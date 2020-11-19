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
package org.acra.log

import android.util.Log

/**
 * Responsible for delegating calls to the Android logging system.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
class AndroidLogDelegate : ACRALog {
    override fun v(tag: String, msg: String): Int = Log.v(tag, msg)

    override fun v(tag: String, msg: String, tr: Throwable): Int = Log.v(tag, msg, tr)

    override fun d(tag: String, msg: String): Int = Log.d(tag, msg)

    override fun d(tag: String, msg: String, tr: Throwable): Int = Log.d(tag, msg, tr)

    override fun i(tag: String, msg: String): Int = Log.i(tag, msg)

    override fun i(tag: String, msg: String, tr: Throwable): Int = Log.i(tag, msg, tr)

    override fun w(tag: String, msg: String): Int = Log.w(tag, msg)

    override fun w(tag: String, msg: String, tr: Throwable): Int = Log.w(tag, msg, tr)

    override fun w(tag: String, tr: Throwable): Int = Log.w(tag, tr)

    override fun e(tag: String, msg: String): Int = Log.e(tag, msg)

    override fun e(tag: String, msg: String, tr: Throwable): Int = Log.e(tag, msg, tr)

    override fun getStackTraceString(tr: Throwable): String? = Log.getStackTraceString(tr)
}