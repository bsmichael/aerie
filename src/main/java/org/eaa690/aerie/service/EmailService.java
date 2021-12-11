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

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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
import java.util.Date;
import java.util.Properties;

@Service
public class EmailService {

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    /**
     * Sends an email.
     *
     * @param to message recipient
     * @param subject of the message
     * @param body of the message
     * @param from sender
     * @param password for the sender's mailbox
     * @param img Image to be included in message, null if no image is to be included
     */
    public void sendEmailMessage(final String to,
                                 final String subject,
                                 final String body,
                                 final String from,
                                 final String password,
                                 final String img) throws ResourceNotFoundException {
        if (!Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.EMAIL_ENABLED_KEY).getValue())) {
            return;
        }
        final Properties props = new Properties();
        props.put("mail.smtp.host", "mail.eaa690.org");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        final Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        };
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
            if (img == null) {
                msg.setText(body, "UTF-8");
            } else {
                msg.setContent(buildMultipartMessage(body, img));
            }
            LOGGER.info("Sending email with subject[" + subject + "] to [" + to + "] from [" + from + "]");
            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Multipart buildMultipartMessage(final String body, final String img) throws MessagingException {
        final Multipart multipart = new MimeMultipart();

        final BodyPart messageBodyPart1 = new MimeBodyPart();
        final DataSource source = new FileDataSource(img);
        messageBodyPart1.setDataHandler(new DataHandler(source));
        messageBodyPart1.setFileName(img);
        messageBodyPart1.setHeader("Content-ID", "image_id");
        multipart.addBodyPart(messageBodyPart1);

        final BodyPart messageBodyPart2 = new MimeBodyPart();
        messageBodyPart2.setContent("<img src='cid:image_id'>", "text/html");
        multipart.addBodyPart(messageBodyPart2);

        final BodyPart messageBodyPart3 = new MimeBodyPart();
        messageBodyPart3.setText(body);
        multipart.addBodyPart(messageBodyPart3);

        return multipart;
    }
}
