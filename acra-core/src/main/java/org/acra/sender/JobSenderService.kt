package org.acra.sender

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import org.acra.config.CoreConfiguration
import org.acra.util.IOUtils

/**
 * Job service sending reports. has to run in the :acra process
 *
 * @author Lukas
 * @since 31.12.2018
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
class JobSenderService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        val extras = params.extras
        val config = IOUtils.deserialize(CoreConfiguration::class.java, extras.getString(LegacySenderService.EXTRA_ACRA_CONFIG))
        if (config != null) {
            Thread {
                SendingConductor(this, config).sendReports(false, Bundle(extras))
                jobFinished(params, false)
            }.start()
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }
}