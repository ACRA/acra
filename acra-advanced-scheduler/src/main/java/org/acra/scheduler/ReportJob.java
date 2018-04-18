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

import android.support.annotation.NonNull;
import android.util.Base64;
import com.evernote.android.job.Job;
import org.acra.config.CoreConfiguration;
import org.acra.sender.SenderService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author F43nd1r
 * @since 18.04.18
 */
public class ReportJob extends Job {
    public static final String TAG = "report";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        String serializedConfig = params.getExtras().getString(SenderService.EXTRA_ACRA_CONFIG, null);
        boolean sendOnlySilentReports = params.getExtras().getBoolean(SenderService.EXTRA_ONLY_SEND_SILENT_REPORTS, false);
        if (serializedConfig != null) {
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(serializedConfig, Base64.DEFAULT)))) {
                CoreConfiguration config = (CoreConfiguration) in.readObject();
                new DefaultSenderScheduler().scheduleReportSending(getContext(), config, sendOnlySilentReports);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return Result.FAILURE;
    }
}
