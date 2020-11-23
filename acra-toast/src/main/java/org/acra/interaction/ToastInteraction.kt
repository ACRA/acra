/*
 * Copyright (c) 2017 the ACRA team
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
package org.acra.interaction

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.auto.service.AutoService
import org.acra.config.ConfigUtils.getPluginConfiguration
import org.acra.config.CoreConfiguration
import org.acra.config.ToastConfiguration
import org.acra.plugins.HasConfigPlugin
import org.acra.util.ToastSender.sendToast
import java.io.File

/**
 * @author F43nd1r
 * @since 04.06.2017
 */
@AutoService(ReportInteraction::class)
class ToastInteraction : HasConfigPlugin(ToastConfiguration::class.java), ReportInteraction {
    override fun performInteraction(context: Context, config: CoreConfiguration, reportFile: File): Boolean {
        Looper.prepare()
        val pluginConfig = getPluginConfiguration(config, ToastConfiguration::class.java)
        sendToast(context, pluginConfig.text, pluginConfig.length)
        val looper = Looper.myLooper()
        if (looper != null) {
            Handler(looper).postDelayed({
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    looper.quitSafely()
                } else {
                    looper.quit()
                }
            }, getLengthInMs(pluginConfig.length) + 100.toLong())
            Looper.loop()
        }
        return true
    }

    private fun getLengthInMs(toastDuration: Int): Int {
        return when (toastDuration) {
            Toast.LENGTH_LONG -> 3500
            Toast.LENGTH_SHORT -> 2000
            else -> 0
        }
    }
}