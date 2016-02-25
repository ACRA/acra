/*
 *  Copyright 2010 Kevin Gaudin
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

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.collector.CrashReportData;

/**
 * A simple interface for defining various crash report senders.
 *
 * You can reuse {@link HttpSender} to send reports to your custom server-side report
 * collection script even if you expect (or prefer) specific names for each
 * report field as {@link HttpSender#send(Context, CrashReportData)}
 * can take a {@code Map<ReportField, String>} as an input to convert each field name to
 * your preferred POST parameter name.
 * 
 * @author Kevin Gaudin
 */
public interface ReportSender {

    /**
     * Send crash report data.
     *
     * Method will be called from the {@link SenderService}.
     *
     * @param context       Android Context in which to send the crash report.
     * @param errorContent  Stores key/value pairs for each report field.
     *                      A report field is identified by a {@link org.acra.ReportField} enum value.
     * @throws ReportSenderException
     *             If anything goes fatally wrong during the handling of crash
     *             data, you can (should) throw a {@link ReportSenderException}
     *             with a custom message.
     */
    void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException;
}
