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

package org.acra.smtp_mail.sender;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.acra.ReportField;
import org.acra.attachment.DefaultAttachmentProvider;
import org.acra.data.CrashReportData;
import org.acra.config.ConfigUtils;
import org.acra.config.CoreConfiguration;
import org.acra.config.SmtpMailSenderConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.smtp_mail.util.UriDataSource;
import org.acra.util.InstanceCreator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * @author F43nd1r
 * @since 18.10.2017
 */

public class SmtpEmailSender implements ReportSender {
    public static final int DEFAULT_PORT = 587;
    @NonNull
    private final CoreConfiguration config;

    public SmtpEmailSender(@NonNull CoreConfiguration config) {
        this.config = config;
    }

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException {
        final SmtpMailSenderConfiguration senderConfig = ConfigUtils.getPluginConfiguration(config, SmtpMailSenderConfiguration.class);
        final Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", senderConfig.host());
        properties.setProperty("mail.smtp.auth", String.valueOf(senderConfig.auth()));
        properties.setProperty("mail.smtp.port", String.valueOf(senderConfig.port()));
        properties.setProperty("mail.smtp.starttls.enable", String.valueOf(senderConfig.starttlsEnable()));
        final Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderConfig.senderAddress(), senderConfig.senderPassword());
            }
        });
        try {
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderConfig.senderAddress()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(senderConfig.mailTo()));
            message.setSubject(context.getPackageName() + " Crash Report");

            final MailcapCommandMap commandMap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            commandMap.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            commandMap.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            commandMap.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            commandMap.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            commandMap.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

            final Multipart multipart = new MimeMultipart();
            final BodyPart reportBodyPart = new MimeBodyPart();
            if (senderConfig.reportAsFile()) {
                reportBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(errorContent.toJSON(), "text/plain")));
                reportBodyPart.setFileName("ACRA-report.stacktrace");
            } else {
                reportBodyPart.setText(buildBody(errorContent));
            }
            multipart.addBodyPart(reportBodyPart);


            final InstanceCreator instanceCreator = new InstanceCreator();
            final List<Uri> uris = instanceCreator.create(config.attachmentUriProvider(), new DefaultAttachmentProvider()).getAttachments(context, config);
            for (Uri uri : uris) {
                final BodyPart attachmentBodyPart = new MimeBodyPart();
                attachmentBodyPart.setDataHandler(new DataHandler(new UriDataSource(context, uri)));
                multipart.addBodyPart(attachmentBodyPart);
            }

            message.setContent(multipart);
            //Sending email
            Transport.send(message);

        } catch (MessagingException | IOException e) {
            throw new ReportSenderException("Failed to send mail", e);
        }
    }

    /**
     * Creates the message body
     *
     * @param errorContent the report content
     * @return the message body
     */
    @NonNull
    protected String buildBody(@NonNull CrashReportData errorContent) {
        final Set<ReportField> fields = config.reportContent();

        final StringBuilder builder = new StringBuilder();
        final Map<String, String> content = errorContent.toStringMap("\n\t");
        for (ReportField field : fields) {
            builder.append(field.toString()).append('=').append(content.remove(field.toString())).append('\n');
        }
        for (Map.Entry<String, String> entry : content.entrySet()) {
            builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        return builder.toString();
    }
}
