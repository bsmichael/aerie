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

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import org.eaa690.aerie.communication.AcceptsEmailPredicate;
import org.eaa690.aerie.communication.AcceptsSMSPredicate;
import org.eaa690.aerie.communication.AcceptsSlackPredicate;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.MessageType;
import org.eaa690.aerie.model.QueuedMessage;
import org.eaa690.aerie.model.QueuedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * CommunicationService.
 */
@Service
public class CommunicationService implements SlackMessagePostedListener {

    /**
     * SendGrid API.
     */
    @Autowired
    private SendGrid sendGrid;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationService.class);

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * SlackSession.
     */
    @Autowired
    private SlackSession slackSession;

    /**
     * JotFormService.
     */
    @Autowired
    private JotFormService jotFormService;

    /**
     * MemberRepository.
     */
    @Autowired
    private MemberRepository memberRepository;

    /**
     * QueuedMessageRepository.
     */
    @Autowired
    private QueuedMessageRepository queuedMessageRepository;

    /**
     * AcceptsEmailPredicate.
     */
    @Autowired
    private AcceptsEmailPredicate acceptsEmailPredicate;

    /**
     * AcceptsSMSPredicate.
     */
    @Autowired
    private AcceptsSMSPredicate acceptsSMSPredicate;

    /**
     * AcceptsSlackPredicate.
     */
    @Autowired
    private AcceptsSlackPredicate acceptsSlackPredicate;

    /**
     * Sets SlackSession. Note: mostly used for unit test mocks
     *
     * @param value SlackSession
     */
    @Autowired
    public void setSlackSession(final SlackSession value) {
        slackSession = value;
    }

    /**
     * Sets PropertyService. Note: mostly used for unit test mocks
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    /**
     * Sets JotFormService. Note: mostly used for unit test mocks
     *
     * @param value JotFormService
     */
    @Autowired
    public void setJotFormService(final JotFormService value) {
        jotFormService = value;
    }

    /**
     * Sets MemberRepository. Note: mostly used for unit test mocks
     *
     * @param mRepository MemberRepository
     */
    @Autowired
    public void setMemberRepository(final MemberRepository mRepository) {
        memberRepository = mRepository;
    }

    /**
     * Sets QueuedEmailRepository. Note: mostly used for unit test mocks
     *
     * @param qeRepository QueuedEmailRepository
     */
    @Autowired
    public void setQueuedMessageRepository(final QueuedMessageRepository qeRepository) {
        queuedMessageRepository = qeRepository;
    }

    /**
     * Queues message to be sent. Slack only messages are sent immediately.
     *
     * @param queuedMessage QueuedMessage
     */
    public void queueMsg(final QueuedMessage queuedMessage) {
        if (queuedMessage.getCreatedAt() == null) {
            queuedMessage.setCreatedAt(new Date());
        }
        if (queuedMessage.getUpdatedAt() == null) {
            queuedMessage.setUpdatedAt(new Date());
        }
        if (queuedMessage.getMessageType() == MessageType.Slack) {
            sendSlackMessage(queuedMessage);
        } else {
            queuedMessageRepository.save(queuedMessage);
        }
    }

    /**
     * Gets the count of the number of queued messages.
     *
     * @return queued message count
     */
    public int getQueuedMsgCount() {
        return queuedMessageRepository.findAll().map(List::size).orElse(0);
    }

    /**
     * Looks for any messages in the send queue, and sends up to X (see
     * configuration) messages per day.
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void processQueue() {
        final Optional<List<QueuedMessage>> allQueuedMessages = queuedMessageRepository.findAll();
        if (allQueuedMessages.isPresent()) {
            try {
                allQueuedMessages.get().stream()
                        .limit(Long.parseLong(propertyService.get(PropertyKeyConstants.SEND_GRID_LIMIT).getValue()))
                        .forEach(qe -> {
                            sendSlackMessage(qe);
                            sendSMSMessage(qe);
                            sendEmailMessage(qe);
                            queuedMessageRepository.delete(qe);
                        });
            } catch (ResourceNotFoundException e) {
                LOGGER.error("Error", e);
            }
        }
    }

    /**
     * Processes messages received by membership slack bot.
     *
     * @param event   SlackMessagePosted
     * @param session SlackSession
     */
    @Override
    public void onEvent(final SlackMessagePosted event, final SlackSession session) {
        // Ignore bot user messages
        if (session.sessionPersona().getId().equals(event.getSender().getId())) {
            return;
        }
        final String message = event.getMessageContent();
        final String user = event.getUser().getUserName();
        final String msg = String.format("Slack message received: user [%s]; message [%s]", user, message);
        LOGGER.info(msg);
    }

    /**
     * Gets all Slack users.
     *
     * @return list of users
     */
    public List<String> allSlackUsers() {
        final List<String> users = new ArrayList<>();
        slackSession.getUsers().forEach(user -> users.add(user.getRealName() + "|" + user.getUserName()));
        return users;
    }

    /**
     * Builds message to be sent to member.
     *
     * @param member Member
     * @param msgKey message key
     * @return message
     */
    private String getSMSOrSlackMessage(final Member member, final String msgKey) {
        try {
            final String expiration;
            if (member.getExpiration() != null) {
                expiration = ZonedDateTime.ofInstant(member.getExpiration().toInstant(), ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
            } else {
                expiration = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
            }
            return propertyService.get(msgKey).getValue().replaceAll("\\{\\{firstName\\}\\}", member.getFirstName())
                    .replaceAll("\\{\\{lastName\\}\\}", member.getLastName())
                    .replaceAll("\\{\\{expirationDate\\}\\}", expiration)
                    .replaceAll("\\{\\{url\\}\\}", jotFormService.buildRenewMembershipUrl(member));
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error", e);
        }
        return null;
    }

    /**
     * Sends a message via the Slack bot.
     *
     * @param queuedMessage QueuedMessage
     */
    private void sendSlackMessage(final QueuedMessage queuedMessage) {
        if (queuedMessage.getMessageType() == MessageType.Slack) {
            Optional<Member> memberOpt = memberRepository.findById(queuedMessage.getMemberId());
            if (memberOpt.isPresent() && acceptsSlackPredicate.test(memberOpt.get())) {
                LOGGER.info(String.format("Sending Slack message [%s] to [%s]", queuedMessage.getBody(),
                        queuedMessage.getRecipientAddress()));
                slackSession.sendMessageToUser(slackSession.findUserByUserName(queuedMessage.getRecipientAddress()),
                        queuedMessage.getBody(), null);
            }
        }
    }

    /**
     * Sends an SMS message via SendGrid.
     *
     * @param queuedMessage QueuedMessage
     */
    private void sendSMSMessage(final QueuedMessage queuedMessage) {
        if (queuedMessage.getMessageType() == MessageType.SMS) {
            final Optional<Member> memberOpt = memberRepository.findById(queuedMessage.getMemberId());
            if (memberOpt.isPresent()) {
                final Member member = memberOpt.get();
                if (acceptsSMSPredicate.test(member)) {
                    if (member.getCellPhoneProvider() != null) {
                        queuedMessage.setRecipientAddress(String.format("%s@%s", queuedMessage.getRecipientAddress(),
                            member.getCellPhoneProvider().getCellPhoneProviderEmailDomain()));
                        queuedMessage.setMessageType(MessageType.Email);
                        // Sets an arbitrary message key in order to comply with sending an email message
                        queuedMessage.setSubjectKey(
                            PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY);
                        sendEmailMessage(queuedMessage);
                    }
                    LOGGER.error(
                        "Could not send SMS Message because recipient does not have a cellphone provider selected!");
                }
            }
        }
    }

    /**
     * Sends an Email message via SendGrid.
     *
     * @param queuedMessage QueuedMessage
     */
    private void sendEmailMessage(final QueuedMessage queuedMessage) {
        if (queuedMessage.getMessageType() == MessageType.Email) {
            try {
                final Optional<Member> memberOpt = memberRepository.findById(queuedMessage.getMemberId());
                if (memberOpt.isPresent()) {
                    final Member member = memberOpt.get();
                    if (acceptsEmailPredicate.test(member)) {
                        Email from = new Email(
                                propertyService.get(PropertyKeyConstants.SEND_GRID_FROM_ADDRESS_KEY).getValue());
                        String subject = propertyService.get(queuedMessage.getSubjectKey()).getValue();
                        Email to = new Email(queuedMessage.getRecipientAddress());
                        if (Boolean.parseBoolean(
                                propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_ENABLED_KEY).getValue())) {
                            to = new Email(
                                    propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_RECIPIENT_KEY).getValue());
                        }

                        final Mail mail;
                        if (queuedMessage.getTemplateIdKey() != null) {
                            mail = new Mail();
                            mail.setSubject(subject);
                            mail.setTemplateId(propertyService.get(queuedMessage.getTemplateIdKey()).getValue());
                            mail.addPersonalization(personalize(member, to));
                            mail.setFrom(from);
                        } else {
                            mail = new Mail(from, subject, to, new Content("text/plain", queuedMessage.getBody()));
                        }

                        final Request request = new Request();
                        request.setMethod(Method.POST);
                        request.setEndpoint("mail/send");
                        request.setBody(mail.build());
                        Response response = sendGrid.api(request);
                        LOGGER.info(String.format("Response... statusCode [%s]; body [%s]; headers [%s]",
                                response.getStatusCode(), response.getBody(), response.getHeaders()));
                    }
                }
            } catch (IOException | ResourceNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }

    /**
     * Personalizes an email to the member.
     *
     * @param member Member
     * @param to     address
     * @return Personalization
     * @throws ResourceNotFoundException when property is not found
     */
    private Personalization personalize(final Member member, final Email to) throws ResourceNotFoundException {
        final Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addBcc(new Email(propertyService.get(PropertyKeyConstants.EMAIL_BCC_KEY).getValue()));
        personalization.addDynamicTemplateData("firstName", member.getFirstName());
        personalization.addDynamicTemplateData("lastName", member.getLastName());
        personalization.addDynamicTemplateData("url", jotFormService.buildRenewMembershipUrl(member));
        if (member.getExpiration() == null) {
            personalization.addDynamicTemplateData("expirationDate",
                    ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        } else {
            personalization.addDynamicTemplateData("expirationDate",
                    ZonedDateTime.ofInstant(member.getExpiration().toInstant(), ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        }
        return personalization;
    }

    /**
     * Sends new member message.
     *
     * @param member Member
     */
    public void sendNewMembershipMsg(final Member member) {
        if (member != null && member.getSlack() != null && member.isSlackEnabled()) {
            try {
                final QueuedMessage queuedSlackMessage = new QueuedMessage();
                queuedSlackMessage.setMemberId(member.getId());
                queuedSlackMessage.setBody(getSMSOrSlackMessage(member, PropertyKeyConstants.SLACK_NEW_MEMBER_MSG_KEY));
                queuedSlackMessage.setMessageType(MessageType.Slack);
                queuedSlackMessage.setRecipientAddress(getSlackName(member));
                queueMsg(queuedSlackMessage);

                final QueuedMessage queuedSMSMessage = new QueuedMessage();
                queuedSMSMessage.setMemberId(member.getId());
                queuedSMSMessage.setBody(getSMSOrSlackMessage(member, PropertyKeyConstants.SMS_NEW_MEMBER_MSG_KEY));
                queuedSMSMessage.setMessageType(MessageType.SMS);
                queuedSMSMessage.setRecipientAddress(member.getCellPhone());
                queueMsg(queuedSMSMessage);

                final QueuedMessage queuedEmailMessage = new QueuedMessage();
                queuedEmailMessage.setMemberId(member.getId());
                queuedEmailMessage.setSubjectKey(PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_SUBJECT_KEY);
                queuedEmailMessage.setTemplateIdKey(PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_TEMPLATE_ID);
                queuedEmailMessage.setRecipientAddress(member.getEmail());
                queuedEmailMessage.setMessageType(MessageType.Email);
                queueMsg(queuedEmailMessage);
            } catch (ResourceNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     */
    public void sendRenewMembershipMsg(final Member member) {
        try {
            final QueuedMessage queuedSlackMessage = new QueuedMessage();
            queuedSlackMessage.setMemberId(member.getId());
            queuedSlackMessage.
                setBody(getSMSOrSlackMessage(member, PropertyKeyConstants.SLACK_RENEW_MEMBER_MSG_KEY));
            queuedSlackMessage.setMessageType(MessageType.Slack);
            queuedSlackMessage.setRecipientAddress(getSlackName(member));
            queuedSlackMessage.setCreatedAt(new Date());
            queuedSlackMessage.setUpdatedAt(new Date());
            queueMsg(queuedSlackMessage);

            final QueuedMessage queuedSMSMessage = new QueuedMessage();
            queuedSMSMessage.setMemberId(member.getId());
            queuedSMSMessage.setBody(getSMSOrSlackMessage(member, PropertyKeyConstants.SMS_RENEW_MEMBER_MSG_KEY));
            queuedSMSMessage.setMessageType(MessageType.SMS);
            queuedSMSMessage.setRecipientAddress(member.getCellPhone());
            queuedSMSMessage.setCreatedAt(new Date());
            queuedSMSMessage.setUpdatedAt(new Date());
            queueMsg(queuedSMSMessage);

            final QueuedMessage queuedEmailMessage = new QueuedMessage();
            queuedEmailMessage.setMemberId(member.getId());
            queuedEmailMessage.setSubjectKey(PropertyKeyConstants.SEND_GRID_THIRD_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY);
            queuedEmailMessage.setTemplateIdKey(
                PropertyKeyConstants.SEND_GRID_THIRD_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID);
            queuedEmailMessage.setRecipientAddress(member.getEmail());
            queuedEmailMessage.setMessageType(MessageType.Email);
            queuedEmailMessage.setCreatedAt(new Date());
            queuedEmailMessage.setUpdatedAt(new Date());
            queueMsg(queuedEmailMessage);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * Gets Slack name, or overridden value, if in test mode.
     *
     * @param member Member
     * @return Slack name
     * @throws ResourceNotFoundException if properties are not found
     */
    private String getSlackName(final Member member) throws ResourceNotFoundException {
        String to = member.getSlack();
        if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SLACK_TEST_MODE_ENABLED_KEY).getValue())) {
            to = propertyService.get(PropertyKeyConstants.SLACK_TEST_MODE_RECIPIENT_KEY).getValue();
        }
        return to;
    }

}
