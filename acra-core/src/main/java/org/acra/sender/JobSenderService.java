package org.acra.sender;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.os.PersistableBundle;
import androidx.annotation.RequiresApi;
import org.acra.config.CoreConfiguration;
import org.acra.util.BundleWrapper;
import org.acra.util.IOUtils;

/**
 * Job service sending reports. has to run in the :acra process
 *
 * @author Lukas
 * @since 31.12.2018
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
public class JobSenderService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        PersistableBundle extras = params.getExtras();
        CoreConfiguration config = IOUtils.deserialize(CoreConfiguration.class, extras.getString(LegacySenderService.EXTRA_ACRA_CONFIG));
        if (config != null) {
            new Thread(() -> {
                new SendingConductor(this, config).sendReports(false, BundleWrapper.wrap(extras));
                jobFinished(params, false);
            }).start();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
