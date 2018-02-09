/*
 * Copyright (c) 2017
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
package org.acra.sender;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.attachment.AcraContentProvider;
import org.acra.attachment.DefaultAttachmentProvider;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.MailSenderConfiguration;
import org.acra.data.CrashReportData;
import org.acra.util.IOUtils;
import org.acra.util.InstanceCreator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Send reports through an email intent.
 * <p>
 * The user will be asked to chose his preferred email client if no default is set. Included report fields can be defined using
 * {@link org.acra.annotation.AcraCore#reportContent()}. Crash receiving mailbox has to be
 * defined with {@link org.acra.annotation.AcraMailSender#mailTo()}.
 */
@SuppressWarnings("WeakerAccess")
public class EmailIntentSender implements ReportSender {
    public static final String DEFAULT_REPORT_FILENAME = "ACRA-report" + ACRAConstants.REPORTFILE_EXTENSION;

    private final CoreConfiguration config;
    private final MailSenderConfiguration mailConfig;

    public EmailIntentSender(@NonNull CoreConfiguration config) {
        this.config = config;
        this.mailConfig = ConfigUtils.getPluginConfiguration(config, MailSenderConfiguration.class);
    }

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException {
        final PackageManager pm = context.getPackageManager();

        final String subject = buildSubject(context);
        final String body;
        try {
            body = config.reportFormat().toFormattedString(errorContent, config.reportContent(), "\n", "\n\t", false);
        } catch (Exception e) {
            throw new ReportSenderException("Failed to convert Report to text", e);
        }
        final ArrayList<Uri> attachments = new ArrayList<>();
        final boolean contentAttached = fillAttachmentList(context, body, attachments);

        //we have to resolve with sendto, because send is supported by non-email apps
        final Intent resolveIntent = buildResolveIntent(subject, body);
        final ComponentName resolveActivity = resolveIntent.resolveActivity(pm);
        if (resolveActivity != null) {
            if (attachments.size() == 0) {
                //no attachments, send directly
                context.startActivity(resolveIntent);
            } else {
                final Intent attachmentIntent = buildAttachmentIntent(subject, body, attachments, contentAttached);
                final List<Intent> initialIntents = buildInitialIntents(pm, resolveIntent, attachmentIntent);
                final String packageName = getPackageName(resolveActivity, initialIntents);
                attachmentIntent.setPackage(packageName);
                if (packageName == null) {
                    //let user choose email client
                    for (Intent intent : initialIntents) {
                        grantPermission(context, intent, intent.getPackage(), attachments);
                    }
                    showChooser(context, initialIntents);
                } else if (attachmentIntent.resolveActivity(pm) != null) {
                    //use default email client
                    grantPermission(context, attachmentIntent, packageName, attachments);
                    context.startActivity(attachmentIntent);
                } else {
                    ACRA.log.w(LOG_TAG, "No email client supporting attachments found. Attachments will be ignored");
                    context.startActivity(resolveIntent);
                }
            }
        } else {
            throw new ReportSenderException("No email client found");
        }
    }

    /**
     * Finds the package name of the default email client supporting attachments
     *
     * @param resolveActivity the resolved activity
     * @param initialIntents  a list of intents to be used when
     * @return package name of the default email client, or null if more than one app match
     */
    @Nullable
    private String getPackageName(@NonNull ComponentName resolveActivity, @NonNull List<Intent> initialIntents) {
        String packageName = resolveActivity.getPackageName();
        if (packageName.equals("android")) {
            //multiple activities support the intent and no default is set
            if (initialIntents.size() > 1) {
                packageName = null;
            } else if (initialIntents.size() == 1) {
                //only one of them supports attachments, use that one
                packageName = initialIntents.get(0).getPackage();
            }
        }
        return packageName;
    }

    /**
     * Builds an email intent with attachments
     *
     * @param subject         the message subject
     * @param body            the message body
     * @param attachments     the attachments
     * @param contentAttached if the body is already contained in the attachments
     * @return email intent
     */
    @NonNull
    protected Intent buildAttachmentIntent(@NonNull String subject, @NonNull String body, @NonNull ArrayList<Uri> attachments, boolean contentAttached) {
        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ConfigUtils.getPluginConfiguration(config, MailSenderConfiguration.class).mailTo()});
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.setType("message/rfc822");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
        if (!contentAttached) intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    /**
     * Builds an intent used to resolve email clients and to send reports without attachments or as fallback if no attachments are supported
     *
     * @param subject the message subject
     * @param body    the message body
     * @return email intent
     */
    @NonNull
    protected Intent buildResolveIntent(@NonNull String subject, @NonNull String body) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.fromParts("mailto", ConfigUtils.getPluginConfiguration(config, MailSenderConfiguration.class).mailTo(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    @NonNull
    private List<Intent> buildInitialIntents(@NonNull PackageManager pm, @NonNull Intent resolveIntent, @NonNull Intent emailIntent) {
        final List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(resolveIntent, PackageManager.MATCH_DEFAULT_ONLY);
        final List<Intent> initialIntents = new ArrayList<>();
        for (ResolveInfo info : resolveInfoList) {
            final Intent packageSpecificIntent = new Intent(emailIntent);
            packageSpecificIntent.setPackage(info.activityInfo.packageName);
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

    private void grantPermission(@NonNull Context context, @NonNull Intent intent, String packageName, @NonNull List<Uri> attachments) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            //flags do not work on extras prior to lollipop, so we have to grant read permissions manually
            for (Uri uri : attachments) {
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    /**
     * Creates the message subject
     *
     * @param context a context
     * @return the message subject
     */
    @NonNull
    protected String buildSubject(@NonNull Context context) {
        final String subject = mailConfig.subject();
        if (subject != null) {
            return subject;
        }
        return context.getPackageName() + " Crash Report";
    }

    /**
     * Adds all attachment uris into the given list
     *
     * @param context     a context
     * @param body        the report content
     * @param attachments the target list
     * @return if the attachments contain the content
     */
    protected boolean fillAttachmentList(@NonNull Context context, @NonNull String body, @NonNull List<Uri> attachments) {
        final InstanceCreator instanceCreator = new InstanceCreator();
        attachments.addAll(instanceCreator.create(config.attachmentUriProvider(), DefaultAttachmentProvider::new).getAttachments(context, config));
        if (mailConfig.reportAsFile()) {
            final Uri report = createAttachmentFromString(context, mailConfig.reportFileName(), body);
            if (report != null) {
                attachments.add(report);
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a temporary file with the given content and name, to be used as an email attachment
     *
     * @param context a context
     * @param name    the name
     * @param content the content
     * @return a content uri for the file
     */
    @Nullable
    protected Uri createAttachmentFromString(@NonNull Context context, @NonNull String name, @NonNull String content) {
        final File cache = new File(context.getCacheDir(), name);
        try {
            IOUtils.writeStringToFile(cache, content);
            return AcraContentProvider.getUriForFile(context, cache);
        } catch (IOException ignored) {
        }
        return null;
    }
}
