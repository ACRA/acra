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

package org.acra.util;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.os.DropBoxManager;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

/**
 * @author F43nd1r
 * @since 29.09.2017
 */

public final class SystemServices {
    private SystemServices() {
    }

    @NonNull
    public static TelephonyManager getTelephonyManager(@NonNull Context context) throws ServiceNotReachedException {
        return (TelephonyManager) getService(context, Context.TELEPHONY_SERVICE);
    }

    @NonNull
    public static DropBoxManager getDropBoxManager(@NonNull Context context) throws ServiceNotReachedException {
        return (DropBoxManager) getService(context, Context.DROPBOX_SERVICE);
    }

    @NonNull
    public static NotificationManager getNotificationManager(@NonNull Context context) throws ServiceNotReachedException {
        return (NotificationManager) getService(context, Context.NOTIFICATION_SERVICE);
    }

    @NonNull
    public static ActivityManager getActivityManager(@NonNull Context context) throws ServiceNotReachedException {
        return (ActivityManager) getService(context, Context.ACTIVITY_SERVICE);
    }

    @NonNull
    private static Object getService(@NonNull Context context, @NonNull String id) throws ServiceNotReachedException {
        final Object service = context.getSystemService(id);
        if (service == null) {
            throw new ServiceNotReachedException("Unable to load SystemService " + id);
        }
        return service;
    }

    static class ServiceNotReachedException extends Exception {
        ServiceNotReachedException(String message) {
            super(message);
        }
    }
}
