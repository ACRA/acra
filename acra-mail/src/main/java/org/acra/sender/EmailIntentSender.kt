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
package org.acra.sender

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import org.acra.ACRAConstants
import org.acra.attachment.AcraContentProvider
import org.acra.attachment.DefaultAttachmentProvider
import org.acra.config.CoreConfiguration
import org.acra.config.MailSenderConfiguration
import org.acra.config.getPluginConfiguration
import org.acra.data.CrashReportData
import org.acra.log.warn
import org.acra.util.IOUtils.writeStringToFile
import org.acra.util.InstanceCreator
import java.io.File
import java.io.IOException
import kotlin.collections.ArrayList

/**
 * Send reports through an email intent.
 *
 *
 * The user will be asked to chose his preferred email client if no default is set. Included report fields can be defined using
 * [org.acra.annotation.AcraCore.reportContent]. Crash receiving mailbox has to be
 * defined with [org.acra.annotation.AcraMailSender.mailTo].
 */
@Suppress("MemberVisibilityCanBePrivate")
class EmailIntentSender(private val config: CoreConfiguration) : ReportSender {
    private val mailConfig: MailSenderConfiguration = config.getPluginConfiguration()

    @Throws(ReportSenderException::class)
    override fun send(context: Context, errorContent: CrashReportData) {
        val subject = buildSubject(context)
        val reportText: String = try {
            config.reportFormat.toFormattedString(errorContent, config.reportContent, "\n", "\n\t", false)
        } catch (e: Exception) {
            throw ReportSenderException("Failed to convert Report to text", e)
        }
        val (body, attachments) = getBodyAndAttachments(context, reportText)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            sendWithSelector(subject, body, attachments, context)
        } else {
            sendLegacy(subject, body, attachments, context)
        }
    }

    private fun sendLegacy(subject: String, body: String, attachments: List<Uri>, context: Context) {
        val pm = context.packageManager
        //we have to resolve with sendto, because send is supported by non-email apps
        val resolveIntent = buildResolveIntent()
        val resolveActivity = resolveIntent.resolveActivity(pm)
        if (resolveActivity != null) {
            if (attachments.isEmpty()) {
                //no attachments, send directly
                context.startActivity(buildFallbackIntent(subject, body))
            } else {
                val attachmentIntent = buildAttachmentIntent(subject, body, attachments)
                val altAttachmentIntent = Intent(attachmentIntent).apply { type = "*/*" } // gmail will only match with type set
                val initialIntents = buildInitialIntents(pm, resolveIntent, attachmentIntent)
                val packageName = getPackageName(resolveActivity, initialIntents)
                attachmentIntent.setPackage(packageName)
                altAttachmentIntent.setPackage(packageName)
                when {
                    packageName == null -> {
                        //let user choose email client
                        for (intent in initialIntents) {
                            grantPermission(context, intent, intent.getPackage(), attachments)
                        }
                        showChooser(context, initialIntents.toMutableList())
                    }

                    attachmentIntent.resolveActivity(pm) != null -> {
                        //use default email client
                        grantPermission(context, attachmentIntent, packageName, attachments)
                        context.startActivity(attachmentIntent)
                    }

                    altAttachmentIntent.resolveActivity(pm) != null -> {
                        //use default email client
                        grantPermission(context, altAttachmentIntent, packageName, attachments)
                        context.startActivity(altAttachmentIntent)
                    }

                    else -> {
                        warn { "No email client supporting attachments found. Attachments will be ignored" }
                        context.startActivity(buildFallbackIntent(subject, body))
                    }
                }
            }
        } else {
            throw ReportSenderException("No email client found")
        }
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private fun sendWithSelector(subject: String, body: String, attachments: List<Uri>, context: Context) {
        val intent = if (attachments.size == 1) {
            buildSingleAttachmentIntent(subject, body, attachments.first())
        } else {
            buildAttachmentIntent(subject, body, attachments)
        }
        intent.selector = buildResolveIntent()
        grantPermission(context, intent, null, attachments)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                sendLegacy(subject, body, attachments, context)
            }catch (e2: ActivityNotFoundException) {
                throw ReportSenderException("No email client found", e2).apply { addSuppressed(e) }
            }
        }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    override fun requiresForeground(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * Finds the package name of the default email client supporting attachments
     *
     * @param resolveActivity the resolved activity
     * @param initialIntents  a list of intents to be used when
     * @return package name of the default email client, or null if more than one app match
     */
    private fun getPackageName(resolveActivity: ComponentName, initialIntents: List<Intent>): String? {
        var packageName: String? = resolveActivity.packageName
        if (packageName == "android") {
            if (initialIntents.size > 1) {
                //multiple activities support the intent and no default is set
                packageName = null
            } else if (initialIntents.size == 1) {
                //only one of them supports attachments, use that one
                packageName = initialIntents[0].getPackage()
            }
        }
        return packageName
    }

    /**
     * Builds an email intent with attachments
     *
     * @param subject         the message subject
     * @param body            the message body
     * @param attachments     the attachments
     * @return email intent
     */
    protected fun buildAttachmentIntent(subject: String, body: String?, attachments: List<Uri>): Intent {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mailConfig.mailTo))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments.toCollection(ArrayList()))
        intent.putExtra(Intent.EXTRA_TEXT, body)
        return intent
    }

    /**
     * Builds an email intent with one attachment
     *
     * @param subject         the message subject
     * @param body            the message body
     * @param attachment     the attachment
     * @return email intent
     */
    protected fun buildSingleAttachmentIntent(subject: String, body: String?, attachment: Uri): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mailConfig.mailTo))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_STREAM, attachment)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        return intent
    }

    /**
     * Builds an intent used to resolve email clients
     *
     * @return email intent
     */
    protected fun buildResolveIntent(): Intent {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    protected fun buildFallbackIntent(subject: String, body: String): Intent {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:${mailConfig.mailTo}?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        return intent
    }

    private fun buildInitialIntents(pm: PackageManager, resolveIntent: Intent, emailIntent: Intent): List<Intent> {
        val resolveInfoList = pm.queryIntentActivities(resolveIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val initialIntents: MutableList<Intent> = ArrayList()
        for (info in resolveInfoList) {
            val packageSpecificIntent = Intent(emailIntent)
            packageSpecificIntent.setPackage(info.activityInfo.packageName)
            if (packageSpecificIntent.resolveActivity(pm) != null) {
                initialIntents.add(packageSpecificIntent)
            }
        }
        return initialIntents
    }

    private fun showChooser(context: Context, initialIntents: MutableList<Intent>) {
        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, initialIntents.removeAt(0))
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents.toTypedArray())
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun grantPermission(context: Context, intent: Intent, packageName: String?, attachments: List<Uri>) {
        if (packageName == null) {
            for (resolveInfo in context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
                grantPermission(context, intent, resolveInfo.activityInfo.packageName, attachments)
            }
        } else {
            for (uri in attachments) {
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    /**
     * Creates the message subject
     *
     * @param context a context
     * @return the message subject
     */
    protected fun buildSubject(context: Context): String {
        val subject: String? = mailConfig.subject
        return if (subject?.isNotEmpty() == true) {
            subject
        } else context.packageName + " Crash Report"
    }

    /**
     * Returns body and attachments according to report text and configuration
     *
     * @param context a context
     * @param reportText the report content
     * @return body and attachments to be sent
     */
    protected fun getBodyAndAttachments(context: Context, reportText: String): Pair<String, List<Uri>> {
        val bodyPrefix: String? = mailConfig.body
        val body = when {
            mailConfig.reportAsFile -> bodyPrefix ?: ""
            bodyPrefix?.isNotEmpty() == true -> "$bodyPrefix\n$reportText"
            else -> reportText
        }
        val attachments = mutableListOf<Uri>()
        attachments.addAll(InstanceCreator.create(config.attachmentUriProvider) { DefaultAttachmentProvider() }.getAttachments(context, config))
        if (mailConfig.reportAsFile) {
            val report = createAttachmentFromString(context, mailConfig.reportFileName, reportText)
            if (report != null) {
                attachments.add(report)
            }
        }
        return body to attachments
    }

    /**
     * Creates a temporary file with the given content and name, to be used as an email attachment
     *
     * @param context a context
     * @param name    the name
     * @param content the content
     * @return a content uri for the file
     */
    protected fun createAttachmentFromString(context: Context, name: String, content: String): Uri? {
        val cache = File(context.cacheDir, name)
        try {
            writeStringToFile(cache, content)
            return AcraContentProvider.getUriForFile(context, cache)
        } catch (ignored: IOException) {
        }
        return null
    }

    companion object {
        const val DEFAULT_REPORT_FILENAME = "ACRA-report" + ACRAConstants.REPORTFILE_EXTENSION
    }

}