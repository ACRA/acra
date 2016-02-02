package org.acra.builder;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import org.acra.ACRA;
import org.acra.collector.Compatibility;
import org.acra.dialog.BaseCrashReportDialog;
import org.acra.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksCompat;
import org.acra.jraf.android.util.activitylifecyclecallbackscompat.ApplicationHelper;

import java.lang.ref.WeakReference;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for tracking the last Activity other than any CrashReport dialog that was created.
 *
 * @since 4.8.0
 */
public final class LastActivityManager {

    private WeakReference<Activity> lastActivityCreated = new WeakReference<Activity>(null);

    public LastActivityManager(Application application) {
        if (Compatibility.getAPILevel() >= Compatibility.VERSION_CODES.ICE_CREAM_SANDWICH) {

            // ActivityLifecycleCallback only available for API14+
            ApplicationHelper.registerActivityLifecycleCallbacks(application, new ActivityLifecycleCallbacksCompat() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityCreated " + activity.getClass());
                    if (!(activity instanceof BaseCrashReportDialog)) {
                        // Ignore CrashReportDialog because we want the last
                        // application Activity that was started so that we can explicitly kill it off.
                        lastActivityCreated = new WeakReference<Activity>(activity);
                    }
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityStarted " + activity.getClass());
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityResumed " + activity.getClass());
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityPaused " + activity.getClass());
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityStopped " + activity.getClass());
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivitySaveInstanceState " + activity.getClass());
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "onActivityDestroyed " + activity.getClass());
                }
            });
        }
    }

    public Activity getLastActivity() {
        return lastActivityCreated.get();
    }

    public void clearLastActivity() {
        lastActivityCreated.clear();
    }
}
