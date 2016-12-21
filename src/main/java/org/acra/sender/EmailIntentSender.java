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
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.collections.ImmutableSet;
import org.acra.model.Element;

import java.util.Set;

/**
 * Send reports through an email intent.
 * <p>
 * The user will be asked to chose his preferred email client. Included report fields can be defined using
 * {@link org.acra.annotation.ReportsCrashes#customReportContent()}. Crash receiving mailbox has to be
 * defined with {@link ReportsCrashes#mailTo()}.
 */
public class EmailIntentSender implements ReportSender {

    private final ACRAConfiguration config;

    public EmailIntentSender(@NonNull ACRAConfiguration config) {
        this.config = config;
    }

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException {

        final String subject = context.getPackageName() + " Crash Report";
        final String body = buildBody(errorContent);

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.fromParts("mailto", config.mailTo(), null));
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        context.startActivity(emailIntent);
    }

    private String buildBody(@NonNull CrashReportData errorContent) {
        Set<ReportField> fields = config.getReportFields();
        if (fields.isEmpty()) {
            fields = new ImmutableSet<ReportField>(ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS);
        }

        final StringBuilder builder = new StringBuilder();
        for (ReportField field : fields) {
            builder.append(field.toString()).append('=');
            Element value = errorContent.get(field);
            if (value != null) {
                builder.append(TextUtils.join("\n\t", value.flatten()));
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
