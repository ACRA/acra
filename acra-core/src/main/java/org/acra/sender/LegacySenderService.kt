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
package org.acra.sender

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import org.acra.config.CoreConfiguration
import org.acra.log.debug
import org.acra.util.IOUtils

/**
 * Plain service sending reports. has to run in the :acra process.
 * Only used when no JobScheduler is available.
 *
 * @author Lukas
 */
class LegacySenderService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.hasExtra(EXTRA_ACRA_CONFIG)) {
            val config = IOUtils.deserialize(CoreConfiguration::class.java, intent.getStringExtra(EXTRA_ACRA_CONFIG))
            if (config != null) {
                Thread {
                    SendingConductor(this, config).sendReports(false, intent.extras ?: Bundle())
                    stopSelf()
                }.start()
            }
        } else {
            debug { "SenderService was started but no valid intent was delivered, will now quit" }
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        const val EXTRA_ONLY_SEND_SILENT_REPORTS = "onlySendSilentReports"
        const val EXTRA_ACRA_CONFIG = "acraConfig"
    }
}