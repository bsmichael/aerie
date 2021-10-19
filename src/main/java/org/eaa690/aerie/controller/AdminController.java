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

package org.eaa690.aerie.controller;

import com.ullink.slack.simpleslackapi.SlackBot;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackPersonaImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MessageType;
import org.eaa690.aerie.model.QueuedMessage;
import org.eaa690.aerie.service.CommunicationService;
import org.eaa690.aerie.service.MailChimpService;
import org.eaa690.aerie.service.RosterService;
import org.eaa690.aerie.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AdminController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/admin"
})
@Tag(name = "admin", description = "the Admin API")
public class AdminController {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(AdminController.class);

    /**
     * Send message string.
     */
    private static final String SEND_MSG_MESSAGE = "Sending %s %s to %s %s at %s";

    /**
     * RosterService.
     */
    private RosterService rosterService;

    /**
     * WeatherService.
     */
    private WeatherService weatherService;

    /**
     * EmailService.
     */
    private CommunicationService communicationService;

    /**
     * MailChimpService.
     */
    private MailChimpService mailChimpService;

    /**
     * SlackSession.
     */
    private SlackSession slackSession;

    /**
     * Sets RosterService.
     *
     * @param value RosterService
     */
    @Autowired
    public void setRosterService(final RosterService value) {
        rosterService = value;
    }

    /**
     * Sets WeatherService.
     *
     * @param value WeatherService
     */
    @Autowired
    public void setWeatherService(final WeatherService value) {
        weatherService = value;
    }

    /**
     * Sets EmailService.
     *
     * @param value EmailService
     */
    @Autowired
    public void setCommunicationService(final CommunicationService value) {
        communicationService = value;
    }

    /**
     * Sets MailChimpService.
     *
     * @param value MailChimpService
     */
    @Autowired
    public void setMailChimpService(final MailChimpService value) {
        mailChimpService = value;
    }

    /**
     * Updates Member Repository.
     */
    @PostMapping(path = {"/member/update"})
    public void updateMemberRespository() {
        rosterService.update();
    }

    /**
     * Sends Membership Renewal Messages.
     */
    @PostMapping(path = {"/member/renew"})
    public void sendMembershipRenewalMessages() {
        rosterService.sendMembershipRenewalMessages();
        communicationService.processQueue();
    }

    /**
     * Sets SlackSession.
     *
     * @param value SlackSession
     */
    @Autowired
    public void setSlackSession(final SlackSession value) {
        slackSession = value;
    }

    /**
     * Sends SMS Message to a member.
     *
     * @param rosterId RosterId of member.
     * @param textBody SMS Body to be sent.
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/sms/{rosterId}"})
    public void sendSMS(
            @PathVariable("rosterId") final Long rosterId,
            @RequestBody final String textBody) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        final QueuedMessage queuedMessage = new QueuedMessage();
        queuedMessage.setRecipientAddress(member.getCellPhone());
        queuedMessage.setMemberId(member.getId());
        queuedMessage.setBody(textBody);
        queuedMessage.setMessageType(MessageType.SMS);
        communicationService.queueMsg(queuedMessage);
    }

    /**
     * Gets queued email count.
     *
     * @return queue count
     */
    @Operation(summary = "Email queue count",
            description = "The current number of emails in the email queue waiting to be sent",
            tags = {"admin"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "successful operation")
    })
    @GetMapping(path = {"/email/queue-count"})
    public int getQueuedEmailCount() {
        return communicationService.getQueuedMsgCount();
    }

    /**
     * Sends a renew membership SMS, Email, and/or Slack message to the provided member.
     *
     * @param rosterId Member Roster ID
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/{rosterId}/renew-membership"})
    public void testRenewMembership(@PathVariable("rosterId") final Long rosterId) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        LOGGER.info(String.format(SEND_MSG_MESSAGE, "renew-membership", "sms",
                member.getFirstName(), member.getLastName(), member.getCellPhone()));
        communicationService.sendRenewMembershipMsg(member);
    }

    /**
     * Sends a new membership SMS, Email, and/or Slack message to the provided member.
     *
     * @param rosterId Member Roster ID
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/{rosterId}/new-membership"})
    public void testNewMembership(@PathVariable("rosterId") final Long rosterId) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        LOGGER.info(String.format(SEND_MSG_MESSAGE, "new-membership", "sms",
                member.getFirstName(), member.getLastName(), member.getCellPhone()));
        communicationService.sendNewMembershipMsg(member);
    }

    /**
     * Processes membership renewals and send out notices to members close to or needed to renew their membership.
     * Note: Typically this is run automatically every day at 6am, noon, 6pm, and midnight.
     */
    @PostMapping(path = {"/roster/process-membership-renewals"})
    public void processMembershipRenewals() {
        rosterService.sendMembershipRenewalMessages();
    }

    /**
     * Processes queued messages.
     * Note: Typically this is run automatically every 10 minutes.
     */
    @PostMapping(path = {"/process-message-queue"})
    public void processMessageQueue() {
        communicationService.processQueue();
    }

    /**
     * Processes response from Slack.
     * Note: Typically only Slack would call this routine.
     *
     * @param user User sending Slack message
     * @param textBody Body of message to be sent
     */
    @PostMapping(path = {"/slack/{slackUser}/response"})
    public void processSlackResponse(@PathVariable("slackUser") final String user,
                                     @RequestBody final String textBody) {
        SlackBot slackBot = SlackPersonaImpl.builder().id("unknown").userName("unknown").build();
        SlackUser slackUser = SlackPersonaImpl.builder().id(user).userName(user).build();
        SlackChannel slackChannel = SlackChannel.builder().id("random").name("random").build();
        String timestamp = "";
        SlackMessagePosted messagePosted = new SlackMessagePosted(textBody, slackBot, slackUser,
                slackChannel, timestamp, SlackMessagePosted.MessageSubType.MESSAGE_REPLIED);
        communicationService.onEvent(messagePosted, slackSession);
    }

    /**
     * Gets all Slack users.
     *
     * @return All slack users
     * @throws ResourceNotFoundException when member is not found
     */
    @Operation(summary = "All Slack users",
            description = "List of all Slack users",
            tags = {"admin"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    })
    @GetMapping(path = {"/slack/users"})
    public List<String> getAllSlackUsers() throws ResourceNotFoundException {
        return communicationService.allSlackUsers();
    }

    /**
     * Adds a person to the member audience in Mail Chimp.
     *
     * @param rosterId Member Roster ID
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/mailchimp/{rosterId}/add-member"})
    public void addOrUpdateMemberToMailChimp(@PathVariable("rosterId") final Long rosterId)
            throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        mailChimpService.addOrUpdateMember(member.getFirstName(), member.getLastName(), member.getEmail());
    }

    /**
     * Adds a person to the non-member audience in Mail Chimp.
     *
     * @param rosterId Member Roster ID
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/mailchimp/{rosterId}/add-non-member"})
    public void addOrUpdateNonMemberToMailChimp(@PathVariable("rosterId") final Long rosterId)
            throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        mailChimpService.addOrUpdateNonMember(member.getFirstName(), member.getLastName(), member.getEmail());
    }

    /**
     * Updates weather information from AviationWeather.gov.
     * Note: normally this is run automatically every 10 minutes
     */
    @PostMapping(path = {"/weather/update"})
    public void updateWeather() {
        weatherService.update();
    }
}
