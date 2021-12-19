/*
 *  Copyright (C) 2021 Gwinnett County Experimental Aircraft Association
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eaa690.aerie.service;

import lombok.Getter;
import lombok.Setter;
import org.eaa690.aerie.config.EmailProperties;
import org.eaa690.aerie.model.MessageRepository;
import org.eaa690.aerie.ssl.PasswordAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;

/**
 * Email Service.
 */
public class EmailService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    /**
     * EmailProperties.
     */
    @Autowired
    private EmailProperties emailProperties;

    /**
     * MessageRepository.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Sets EmailProperties.
     * Note: mostly used for unit test mocks
     *
     * @param value EmailProperties
     */
    @Autowired
    public void setEmailProperties(final EmailProperties value) {
        emailProperties = value;
    }

    /**
     * Sets MessageRepository.
     * Note: mostly used for unit test mocks
     *
     * @param value MessageRepository
     */
    @Autowired
    public void setMessageRepository(final MessageRepository value) {
        messageRepository = value;
    }

    /**
     * Email Enabled.
     */
    @Getter
    @Setter
    private boolean enabled = true;

    /**
     * Sends an email.
     *
     * @param to message recipient
     * @param subject of the message
     * @param body of the message
     * @param from sender
     * @param password for the sender's mailbox
     */
    public void sendEmailMessage(final String to,
                                 final String subject,
                                 final String body,
                                 final String from,
                                 final String password) {
        final Properties props = new Properties();
        props.put("mail.smtp.host", emailProperties.getHost());
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", emailProperties.getSmtpPort());
        final Authenticator auth = new PasswordAuthenticator(from, password);
        final Session session = Session.getDefaultInstance(props, auth);
        try {
            final MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress(from, from));
            msg.setReplyTo(InternetAddress.parse(from, false));
            msg.setSubject(subject, "UTF-8");
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setContent(buildMultipartMessage(body));
            if (enabled) {
                LOGGER.info("Sending email with subject [" + subject + "] to [" + to + "] from [" + from + "]");
                messageRepository.save(new org.eaa690.aerie.model.Message(Instant.now(), to, subject));
                Transport.send(msg);
            }
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private Multipart buildMultipartMessage(final String body) throws MessagingException {
        final Multipart multipart = new MimeMultipart();

        final BodyPart messageBodyPart1 = new MimeBodyPart();
        final String letterHeader = emailProperties.getLetterhead();
        final DataSource source = new FileDataSource(letterHeader);
        messageBodyPart1.setDataHandler(new DataHandler(source));
        messageBodyPart1.setFileName(letterHeader);
        messageBodyPart1.setHeader("Content-ID", "image_id");
        multipart.addBodyPart(messageBodyPart1);

        final BodyPart messageBodyPart2 = new MimeBodyPart();
        messageBodyPart2.setContent(body, "text/html");
        multipart.addBodyPart(messageBodyPart2);

        return multipart;
    }
}
