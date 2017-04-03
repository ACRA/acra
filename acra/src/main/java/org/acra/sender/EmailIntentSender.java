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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.attachment.DefaultAttachmentProvider;
import org.acra.collections.ImmutableSet;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.file.CrashReportPersister;
import org.acra.model.Element;
import org.acra.util.InstanceCreator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.acra.ACRA.LOG_TAG;

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
        final PackageManager pm = context.getPackageManager();

        final String subject = context.getPackageName() + " Crash Report";
        final String body = buildBody(errorContent);
        final InstanceCreator instanceCreator = new InstanceCreator();
        final ArrayList<Uri> attachments = instanceCreator.create(config.attachmentUriProvider(), new DefaultAttachmentProvider()).getAttachments(context, config);
        boolean contentAttached = false;
        if (config.reportAsFile()) {
            final File cache = new File(context.getCacheDir(), "ACRA-report" + ACRAConstants.REPORTFILE_EXTENSION);
            try {
                new CrashReportPersister().store(errorContent, cache);
                attachments.add(Uri.parse("content://" + context.getPackageName() + ".acra/root" + cache.getPath()));
                contentAttached = true;
            } catch (IOException ignored) {
            }
        }

        final Intent resolveIntent = new Intent(android.content.Intent.ACTION_SENDTO);
        resolveIntent.setData(Uri.fromParts("mailto", config.mailTo(), null));
        resolveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resolveIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        resolveIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        final ComponentName componentName = resolveIntent.resolveActivity(pm);
        if (componentName != null) {
            if (attachments.size() == 0) {
                context.startActivity(resolveIntent);
            } else {
                String packageName = componentName.getPackageName();
                final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{config.mailTo()});
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                if (!contentAttached) emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                emailIntent.setType("message/rfc822");
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
                if (packageName.equals("android")) {
                    //multiple activities support the intent and no default is set
                    final List<Intent> initialIntents = buildInitialIntents(context, pm, resolveIntent, emailIntent, attachments);
                    if (initialIntents.size() > 1) {
                        showChooser(context, initialIntents);
                        return;
                    } else if (initialIntents.size() == 1) {
                        //only one of them supports attachments, use that one
                        packageName = initialIntents.get(0).getPackage();
                    }
                }
                emailIntent.setPackage(packageName);
                if (emailIntent.resolveActivity(pm) != null) {
                    grantPermission(context, emailIntent, packageName, attachments);
                    context.startActivity(emailIntent);
                } else {
                    ACRA.log.w(LOG_TAG, "No email client supporting attachments found. Attachments will be ignored");
                    context.startActivity(resolveIntent);
                }
            }
        } else {
            throw new ReportSenderException("No email client found");
        }
    }

    private List<Intent> buildInitialIntents(@NonNull Context context, @NonNull PackageManager pm, @NonNull Intent resolveIntent, @NonNull Intent emailIntent, @NonNull List<Uri> attachments) {
        final List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(resolveIntent, PackageManager.MATCH_DEFAULT_ONLY);
        final List<Intent> initialIntents = new ArrayList<Intent>();
        for (ResolveInfo info : resolveInfoList) {
            final Intent packageSpecificIntent = new Intent(emailIntent);
            packageSpecificIntent.setPackage(info.activityInfo.packageName);
            grantPermission(context, emailIntent, info.activityInfo.packageName, attachments);
            if (packageSpecificIntent.resolveActivity(pm) != null) {
                initialIntents.add(packageSpecificIntent);
            }
        }
        return initialIntents;
    }

    private void showChooser(@NonNull Context context, @NonNull List<Intent> initialIntents) {
        final Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, initialIntents.remove(0));
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents.toArray(new Intent[initialIntents.size()]));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    private void grantPermission(@NonNull Context context, Intent intent, String packageName, List<Uri> attachments) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            //flags do not work on extras prior to lollipop, so we have to grant read permissions manually
            for (Uri uri : attachments) {
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    private String buildBody(@NonNull CrashReportData errorContent) {
        Set<ReportField> fields = config.reportContent();
        if (fields.isEmpty()) {
            fields = new ImmutableSet<ReportField>(ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS);
        }

        final StringBuilder builder = new StringBuilder();
        for (ReportField field : fields) {
            builder.append(field.toString()).append('=');
            final Element value = errorContent.get(field);
            if (value != null) {
                builder.append(TextUtils.join("\n\t", value.flatten()));
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
