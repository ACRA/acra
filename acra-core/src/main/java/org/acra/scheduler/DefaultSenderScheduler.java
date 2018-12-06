/*
 * Copyright (c) 2018
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

package org.acra.scheduler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Base64;
import androidx.work.*;
import androidx.work.impl.utils.futures.SettableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.sender.SenderService;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static org.acra.ACRA.LOG_TAG;

/**
 * Simply schedules sending instantly
 *
 * @author F43nd1r
 * @since 18.04.18
 */
public class DefaultSenderScheduler implements SenderScheduler {
    private final Context context;
    private final CoreConfiguration config;

    public DefaultSenderScheduler(@NonNull Context context, @NonNull CoreConfiguration config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public void scheduleReportSending(boolean onlySendSilentReports) {
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "About to start SenderService");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream outputStream = new ObjectOutputStream(out)) {
            outputStream.writeObject(config);

            OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(ReportSenderJob.class)
                    .setInputData(new Data.Builder()
                            .putString(SenderService.EXTRA_ACRA_CONFIG, Base64.encodeToString(out.toByteArray(), Base64.DEFAULT))
                            .putBoolean(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, onlySendSilentReports)
                            .build())
                    //set a delay so no greedy scheduling is done. Otherwise the job may be killed with the process
                    .setInitialDelay(1, TimeUnit.MILLISECONDS);
            configureWorkRequest(builder);
            //run in a new thread to suppress WorkManager main thread errors
            Thread thread = new Thread(() -> {
                try {
                    WorkManager.getInstance().enqueue(builder.build()).getResult().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            thread.join();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void configureWorkRequest(@NonNull OneTimeWorkRequest.Builder builder) {
    }

    public static class ReportSenderJob extends ListenableWorker {

        public ReportSenderJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @SuppressLint("RestrictedApi")
        @NonNull
        @Override
        public ListenableFuture<Result> startWork() {
            SettableFuture<Result> future = SettableFuture.create();
            getBackgroundExecutor().execute(() -> {
            String s = getInputData().getString(SenderService.EXTRA_ACRA_CONFIG);
            if (s != null) {
                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(s, Base64.DEFAULT)))) {
                    Object o = inputStream.readObject();
                    if (o instanceof CoreConfiguration) {
                        future.setFuture(SenderService.sendReports(getApplicationContext(), (CoreConfiguration) o, getInputData().getBoolean(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, false)));
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                future.set(Result.failure());
            }

            });
            return future;
        }
    }
}
