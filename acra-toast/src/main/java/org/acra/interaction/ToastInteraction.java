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

package org.acra.interaction;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.auto.service.AutoService;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.ToastConfiguration;
import org.acra.plugins.HasConfigPlugin;
import org.acra.util.ToastSender;

import java.io.File;

/**
 * @author F43nd1r
 * @since 04.06.2017
 */
@AutoService(ReportInteraction.class)
public class ToastInteraction extends HasConfigPlugin implements ReportInteraction {

    public ToastInteraction() {
        super(ToastConfiguration.class);
    }

    @Override
    public boolean performInteraction(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull File reportFile) {
        Looper.prepare();
        ToastConfiguration pluginConfig = ConfigUtils.getPluginConfiguration(config, ToastConfiguration.class);
        ToastSender.sendToast(context, pluginConfig.text(), pluginConfig.length());
        final Looper looper = Looper.myLooper();
        if (looper != null) {
            new Handler(looper).postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    looper.quitSafely();
                } else {
                    looper.quit();
                }
            }, getLengthInMs(pluginConfig.length()) + 100);
            Looper.loop();
        }
        return true;
    }

    private int getLengthInMs(int toastDuration) {
        switch (toastDuration) {
            case Toast.LENGTH_LONG:
                return 3500;
            case Toast.LENGTH_SHORT:
                return 2000;
            default:
                return 0;
        }
    }
}
