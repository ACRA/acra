package org.acra.scheduler

import android.app.Activity
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import org.acra.log.debug
import org.acra.log.warn

/**
 * @author Lukas
 * @since 31.12.2018
 */
class RestartingService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        val className = params.extras.getString(RestartingAdministrator.EXTRA_LAST_ACTIVITY)
        debug { "Restarting activity $className..." }
        if (className != null) {
            try {
                @Suppress("UNCHECKED_CAST") val activityClass = Class.forName(className) as Class<out Activity>
                val intent = Intent(this, activityClass)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(RestartingAdministrator.EXTRA_ACTIVITY_RESTART_AFTER_CRASH, true)
                startActivity(intent)
                debug { "$className was successfully restarted" }
            } catch (e: ClassNotFoundException) {
                warn(e) { "Unable to find activity class$className" }
            }
        }
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }
}