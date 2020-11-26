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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import org.acra.ACRAConstants
import org.acra.attachment.AcraContentProvider
import org.acra.attachment.DefaultAttachmentProvider
import org.acra.config.ConfigUtils.getPluginConfiguration
import org.acra.config.CoreConfiguration
import org.acra.config.MailSenderConfiguration
import org.acra.data.CrashReportData
import org.acra.log.warn
import org.acra.sender.ReportSenderException
import org.acra.util.IOUtils.writeStringToFile
import org.acra.util.InstanceCreator
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Send reports through an email intent.
 *
 *
 * The user will be asked to chose his preferred email client if no default is set. Included report fields can be defined using
 * [org.acra.annotation.AcraCore.reportContent]. Crash receiving mailbox has to be
 * defined with [org.acra.annotation.AcraMailSender.mailTo].
 */
@Suppress("MemberVisibilityCanBePrivate")
open class EmailIntentSender(private val config: CoreConfiguration) : ReportSender {
    private val mailConfig: MailSenderConfiguration = getPluginConfiguration(config, MailSenderConfiguration::class.java)

    @Throws(ReportSenderException::class)
    override fun send(context: Context, errorContent: CrashReportData) {
        val pm = context.packageManager
        val subject = buildSubject(context)
        val reportText: String
        reportText = try {
            config.reportFormat.toFormattedString(errorContent, config.reportContent, "\n", "\n\t", false)
        } catch (e: Exception) {
            throw ReportSenderException("Failed to convert Report to text", e)
        }
        val bodyPrefix: String = mailConfig.body
        val body = if (bodyPrefix.isNotEmpty()) """
            |$bodyPrefix
            |$reportText
            """.trimMargin() else reportText
        val attachments = ArrayList<Uri>()
        val contentAttached = fillAttachmentList(context, reportText, attachments)

        //we have to resolve with sendto, because send is supported by non-email apps
        val resolveIntent = buildResolveIntent(subject, body)
        val resolveActivity = resolveIntent.resolveActivity(pm)
        if (resolveActivity != null) {
            if (attachments.size == 0) {
                //no attachments, send directly
                context.startActivity(resolveIntent)
            } else {
                val attachmentIntent = buildAttachmentIntent(subject, if (contentAttached) bodyPrefix else body, attachments)
                val initialIntents = buildInitialIntents(pm, resolveIntent, attachmentIntent)
                val packageName = getPackageName(resolveActivity, initialIntents)
                attachmentIntent.setPackage(packageName)
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
                    else -> {
                        warn { "No email client supporting attachments found. Attachments will be ignored" }
                        context.startActivity(resolveIntent)
                    }
                }
            }
        } else {
            throw ReportSenderException("No email client found")
        }
    }

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
            //multiple activities support the intent and no default is set
            if (initialIntents.size > 1) {
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
    protected fun buildAttachmentIntent(subject: String, body: String?, attachments: ArrayList<Uri>): Intent {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(mailConfig.mailTo))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.type = "message/rfc822"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        return intent
    }

    /**
     * Builds an intent used to resolve email clients and to send reports without attachments or as fallback if no attachments are supported
     *
     * @param subject the message subject
     * @param body    the message body
     * @return email intent
     */
    protected fun buildResolveIntent(subject: String, body: String): Intent {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            //flags do not work on extras prior to lollipop, so we have to grant read permissions manually
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
        val subject: String = mailConfig.subject
        return if (subject.isNotEmpty()) {
            subject
        } else context.packageName + " Crash Report"
    }

    /**
     * Adds all attachment uris into the given list
     *
     * @param context     a context
     * @param reportText        the report content
     * @param attachments the target list
     * @return if the attachments contain the content
     */
    protected fun fillAttachmentList(context: Context, reportText: String, attachments: MutableList<Uri>): Boolean {
        attachments.addAll(InstanceCreator.create(config.attachmentUriProvider) { DefaultAttachmentProvider() }.getAttachments(context, config))
        if (mailConfig.reportAsFile) {
            val report = createAttachmentFromString(context, mailConfig.reportFileName, reportText)
            if (report != null) {
                attachments.add(report)
                return true
            }
        }
        return false
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