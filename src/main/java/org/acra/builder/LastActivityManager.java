package org.acra.builder;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.dialog.BaseCrashReportDialog;

import java.lang.ref.WeakReference;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for tracking the last Activity other than any CrashReport dialog that was created.
 *
 * @since 4.8.0
 */
public final class LastActivityManager {

    @NonNull
    private WeakReference<Activity> lastActivityCreated = new WeakReference<Activity>(null);

    public LastActivityManager(@NonNull Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            // ActivityLifecycleCallback only available for API14+
            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityCreated " + activity.getClass());
                    if (!(activity instanceof BaseCrashReportDialog)) {
                        // Ignore CrashReportDialog because we want the last
                        // application Activity that was started so that we can explicitly kill it off.
                        lastActivityCreated = new WeakReference<Activity>(activity);
                    }
                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityStarted " + activity.getClass());
                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityResumed " + activity.getClass());
                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityPaused " + activity.getClass());
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityStopped " + activity.getClass());
                    synchronized (this){
                        notify();
                    }
                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, Bundle outState) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivitySaveInstanceState " + activity.getClass());
                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityDestroyed " + activity.getClass());
                }
            });
        }
    }

    @Nullable
    public Activity getLastActivity() {
        return lastActivityCreated.get();
    }

    public void clearLastActivity() {
        lastActivityCreated.clear();
    }

    public void waitForActivityStop(int timeOutInMillis){
        synchronized (this) {
            try {
                wait(timeOutInMillis);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
