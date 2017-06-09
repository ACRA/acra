package org.acra.interaction;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.auto.service.AutoService;

import org.acra.ACRAConstants;
import org.acra.builder.ReportBuilder;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigUtils;
import org.acra.config.ToastConfiguration;
import org.acra.util.ToastSender;

import java.io.File;

/**
 * @author F43nd1r
 * @since 04.06.2017
 */
@AutoService(ReportInteraction.class)
public class ToastInteraction implements ReportInteraction {
    @Override
    public boolean performInteraction(@NonNull Context context, @NonNull ACRAConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull File reportFile) {
        Looper.prepare();
        ToastSender.sendToast(context, ConfigUtils.getSenderConfiguration(config, ToastConfiguration.class).resToastText(), Toast.LENGTH_LONG);
        final Looper looper = Looper.myLooper();
        if(looper != null) {
            new Handler(looper).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        looper.quitSafely();
                    } else {
                        looper.quit();
                    }
                }
            }, ACRAConstants.TOAST_WAIT_DURATION);
            Looper.loop();
        }
        return true;
    }
}
