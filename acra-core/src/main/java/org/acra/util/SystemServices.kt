/*
 * Copyright (c) 2017
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

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.os.DropBoxManager
import android.telephony.TelephonyManager

/**
 * @author F43nd1r
 * @since 29.09.2017
 */
object SystemServices {
    @JvmStatic
    @Throws(ServiceNotReachedException::class)
    fun getTelephonyManager(context: Context): TelephonyManager = getService(context, Context.TELEPHONY_SERVICE) as TelephonyManager

    @JvmStatic
    @Throws(ServiceNotReachedException::class)
    fun getDropBoxManager(context: Context): DropBoxManager = getService(context, Context.DROPBOX_SERVICE) as DropBoxManager

    @JvmStatic
    @Throws(ServiceNotReachedException::class)
    fun getNotificationManager(context: Context): NotificationManager = getService(context, Context.NOTIFICATION_SERVICE) as NotificationManager

    @JvmStatic
    @Throws(ServiceNotReachedException::class)
    fun getActivityManager(context: Context): ActivityManager = getService(context, Context.ACTIVITY_SERVICE) as ActivityManager

    @Throws(ServiceNotReachedException::class)
    private fun getService(context: Context, id: String): Any = context.getSystemService(id) ?: throw ServiceNotReachedException("Unable to load SystemService $id")

    internal class ServiceNotReachedException(message: String?) : Exception(message)
}