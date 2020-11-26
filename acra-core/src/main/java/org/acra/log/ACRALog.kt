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

/**
 * Responsible for providing ACRA classes with a platform neutral way of logging.
 *
 *
 * One reason for using this mechanism is to allow ACRA classes to use a logging system,
 * but be able to execute in a test environment outside of an Android JVM.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
interface ACRALog {
    fun v(tag: String, msg: String): Int
    fun v(tag: String, msg: String, tr: Throwable): Int
    fun d(tag: String, msg: String): Int
    fun d(tag: String, msg: String, tr: Throwable): Int
    fun i(tag: String, msg: String): Int
    fun i(tag: String, msg: String, tr: Throwable): Int
    fun w(tag: String, msg: String): Int
    fun w(tag: String, msg: String, tr: Throwable): Int
    //public native  boolean isLoggable(java.lang.String tag, int level);
    fun w(tag: String, tr: Throwable): Int
    fun e(tag: String, msg: String): Int
    fun e(tag: String, msg: String, tr: Throwable): Int
    fun getStackTraceString(tr: Throwable): String?
    //public native  int println(int priority, java.lang.String tag, java.lang.String msg);
}