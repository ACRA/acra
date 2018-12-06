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

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;
import androidx.work.Result;
import androidx.work.impl.utils.futures.SettableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;
import org.acra.file.CrashReportFileNameParser;
import org.acra.file.ReportLocator;
import org.acra.plugins.PluginLoader;
import org.acra.util.InstanceCreator;
import org.acra.util.ToastSender;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

public class SenderService extends Service {

    public static final String EXTRA_ONLY_SEND_SILENT_REPORTS = "onlySendSilentReports";
    public static final String EXTRA_ACRA_CONFIG = "acraConfig";
    private static final int KEY_SEND_REPORTS = 1;
    private static final int KEY_REPORTS_SENT = 2;

    private final ReportLocator locator;
    private final Messenger messenger;

    @SuppressLint("RestrictedApi")
    public static ListenableFuture<Result> sendReports(Context context, CoreConfiguration config, boolean onlySendSilentReports) {
        SettableFuture<Result> future = SettableFuture.create();
        context.bindService(new Intent(context, SenderService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Messenger messenger = new Messenger(service);
                Messenger replyTo = new Messenger(new Handler(msg -> {
                    if (msg.what == KEY_REPORTS_SENT) {
                        future.set(Result.success());
                        context.unbindService(this);
                        return true;
                    }
                    return false;
                }));
                Message message = new Message();
                message.what = KEY_SEND_REPORTS;
                Bundle data = new Bundle();
                data.putSerializable(EXTRA_ACRA_CONFIG, config);
                data.putBoolean(EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports);
                message.setData(data);
                message.replyTo = replyTo;
                try {
                    messenger.send(message);
                } catch (RemoteException ignored) {
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                if(!future.isDone()) {
                    future.set(Result.failure());
                }
            }
        }, Context.BIND_AUTO_CREATE);
        return future;
    }

    public SenderService() {
        locator = new ReportLocator(this);
        messenger = new Messenger(new Handler(msg -> {
            if (msg.what == KEY_SEND_REPORTS) {
                Bundle data = msg.getData();
                Serializable config = data.getSerializable(EXTRA_ACRA_CONFIG);
                if (config instanceof CoreConfiguration) {
                    sendReports((CoreConfiguration) config, data.getBoolean(EXTRA_ONLY_SEND_SILENT_REPORTS));
                    Message response = new Message();
                    response.what = KEY_REPORTS_SENT;
                    try {
                        msg.replyTo.send(response);
                    } catch (RemoteException ignored) {
                    }
                    return true;
                }
            }
            return false;
        }));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private void sendReports(@NonNull CoreConfiguration config, boolean onlySendSilentReports) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start sending reports from SenderService");
        try {
            final List<ReportSender> senderInstances = getSenderInstances(config);

            // Get approved reports
            final File[] reports = locator.getApprovedReports();

            final ReportDistributor reportDistributor = new ReportDistributor(this, config, senderInstances);

            // Iterate over approved reports and send via all Senders.
            int reportsSentCount = 0; // Use to rate limit sending
            final CrashReportFileNameParser fileNameParser = new CrashReportFileNameParser();
            boolean anyNonSilent = false;
            for (final File report : reports) {
                final boolean isNonSilent = !fileNameParser.isSilent(report.getName());
                if (onlySendSilentReports && isNonSilent) {
                    continue;
                }
                anyNonSilent |= isNonSilent;

                if (reportsSentCount >= ACRAConstants.MAX_SEND_REPORTS) {
                    break; // send only a few reports to avoid overloading the network
                }

                if (reportDistributor.distribute(report)) {
                    reportsSentCount++;
                }
            }
            final String toast;
            if (anyNonSilent && (toast = reportsSentCount > 0 ? config.reportSendSuccessToast() : config.reportSendFailureToast()) != null) {
                new Handler(Looper.getMainLooper()).post(() -> ToastSender.sendToast(this, toast, Toast.LENGTH_LONG));
            }
        } catch (Exception e) {
            ACRA.log.e(LOG_TAG, "", e);
        }

        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Finished sending reports from SenderService");
    }

    @NonNull
    private List<ReportSender> getSenderInstances(@NonNull CoreConfiguration config) {
        List<Class<? extends ReportSenderFactory>> factoryClasses = config.reportSenderFactoryClasses();
        List<ReportSenderFactory> factories = !factoryClasses.isEmpty() ? new InstanceCreator().create(factoryClasses) : config.pluginLoader()
                .loadEnabled(this, config, ReportSenderFactory.class);
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "reportSenderFactories : " + factories);
        final List<ReportSender> reportSenders = new ArrayList<>();
        for (ReportSenderFactory factory : factories) {
            final ReportSender sender = factory.create(this.getApplication(), config);
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Adding reportSender : " + sender);
            reportSenders.add(sender);
        }

        if (reportSenders.isEmpty()) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "No ReportSenders configured - adding NullSender");
            reportSenders.add(new NullSender());
        }
        return reportSenders;
    }
}
