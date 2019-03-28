/*
 *  Copyright 2017
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.sender;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;

import static org.acra.ACRA.LOG_TAG;

public class LegacySenderService extends Service {

    public static final String EXTRA_ONLY_SEND_SILENT_REPORTS = "onlySendSilentReports";
    public static final String EXTRA_ACRA_CONFIG = "acraConfig";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(EXTRA_ACRA_CONFIG)) {
            final boolean onlySendSilentReports = intent.getBooleanExtra(EXTRA_ONLY_SEND_SILENT_REPORTS, false);
            final CoreConfiguration config = (CoreConfiguration) intent.getSerializableExtra(EXTRA_ACRA_CONFIG);
            if (config != null) {
                new Thread(() -> {
                    new SendingConductor(this, config).sendReports(onlySendSilentReports, false);
                    stopSelf();
                }).start();
            }
        } else {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "SenderService was started but no valid intent was delivered, will now quit");
        }
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
