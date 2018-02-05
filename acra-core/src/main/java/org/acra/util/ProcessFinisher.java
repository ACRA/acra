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

package org.acra.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.builder.LastActivityManager;
import org.acra.config.CoreConfiguration;
import org.acra.sender.SenderService;

import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Takes care of cleaning up a process and killing it.
 *
 * @author F43nd1r
 * @since 4.9.2
 */

public final class ProcessFinisher {
    private final Context context;
    private final CoreConfiguration config;
    private final LastActivityManager lastActivityManager;

    public ProcessFinisher(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull LastActivityManager lastActivityManager) {
        this.context = context;
        this.config = config;
        this.lastActivityManager = lastActivityManager;
    }

    public void endApplication() {
        stopServices();
        killProcessAndExit();
    }

    public void finishLastActivity(@Nullable Thread uncaughtExceptionThread) {
        // Trying to solve https://github.com/ACRA/acra/issues/42#issuecomment-12134144
        // Determine the current/last Activity that was started and close
        // it. Activity#finish (and maybe it's parent too).
        final Activity lastActivity = lastActivityManager.getLastActivity();
        if (lastActivity != null) {
            final boolean isMainThread = uncaughtExceptionThread == lastActivity.getMainLooper().getThread();
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finishing the last Activity prior to killing the Process");
            final Runnable finisher = () -> {
                lastActivity.finish();
                if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finished " + lastActivity.getClass());
            };
            if (isMainThread) {
                finisher.run();
            } else {
                lastActivity.runOnUiThread(finisher);
            }

            // A crashed activity won't continue its lifecycle. So we only wait if something else crashed
            if (!isMainThread) {
                lastActivityManager.waitForActivityStop(100);
            }
            lastActivityManager.clearLastActivity();
        }
    }

    private void stopServices() {
        if (config.stopServicesOnCrash()) {
            try {
                final ActivityManager activityManager = SystemServices.getActivityManager(context);
                final List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
                final int pid = Process.myPid();
                for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
                    if (serviceInfo.pid == pid && !SenderService.class.getName().equals(serviceInfo.service.getClassName())) {
                        try {
                            final Intent intent = new Intent();
                            intent.setComponent(serviceInfo.service);
                            context.stopService(intent);
                        } catch (SecurityException e) {
                            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Unable to stop Service " + serviceInfo.service.getClassName() + ". Permission denied");
                        }
                    }
                }
            } catch (SystemServices.ServiceNotReachedException e) {
                ACRA.log.e(LOG_TAG, "Unable to stop services", e);
            }
        }
    }

    private void killProcessAndExit() {
        Process.killProcess(Process.myPid());
        System.exit(10);
    }
}
