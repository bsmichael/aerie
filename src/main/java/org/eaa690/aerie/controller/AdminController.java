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

import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.service.EmailService;
import org.eaa690.aerie.service.MailChimpService;
import org.eaa690.aerie.service.SMSService;
import org.eaa690.aerie.service.SlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AdminController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/admin"
})
public class AdminController {

    /**
     * EmailService.
     */
    private EmailService emailService;

    /**
     * SMSService.
     */
    private SMSService smsService;

    /**
     * SlackService.
     */
    private SlackService slackService;

    /**
     * MailChimpService.
     */
    private MailChimpService mailChimpService;

    /**
     * Sets EmailService.
     *
     * @param value EmailService
     */
    @Autowired
    public void setEmailService(final EmailService value) {
        emailService = value;
    }

    /**
     * Sets SMSService.
     *
     * @param value SMSService
     */
    @Autowired
    public void setSMSService(final SMSService value) {
        smsService = value;
    }

    /**
     * Sets SlackService.
     *
     * @param value SlackService
     */
    @Autowired
    public void setSlackService(final SlackService value) {
        slackService = value;
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
     * Sends a renew membership email to the provided address.
     *
     * @param Member member
     */
    @PostMapping(path = {"/email/renew-membership"})
    public void testRenewMembershipEmail(@RequestBody Member member) {
        emailService.sendRenewMembershipMsg(member);
    }

    /**
     * Sends a new membership email to the provided address.
     *
     * @param Member member
     */
    @PostMapping(path = {"/email/new-membership"})
    public void testNewMembershipEmail(@RequestBody Member member) {
        emailService.sendNewMembershipMsg(member);
    }

    /**
     * Sends a renew membership SMS to the provided address.
     *
     * @param Member member
     */
    @PostMapping(path = {"/sms/renew-membership"})
    public void testRenewMembershipSMS(@RequestBody Member member) {
        smsService.sendRenewMembershipMsg(member);
    }

    /**
     * Sends a new membership SMS to the provided address.
     *
     * @param Member member
     */
    @PostMapping(path = {"/sms/new-membership"})
    public void testNewMembershipSMS(@RequestBody Member member) {
        smsService.sendNewMembershipMsg(member);
    }

    /**
     * Sends a renew membership Slack message to the provided address.
     *
     * @param Member member
     */
    @PostMapping(path = {"/slack/renew-membership"})
    public void testRenewMembershipSlack(@RequestBody Member member) {
        slackService.sendRenewMembershipMsg(member);
    }

    /**
     * Sends a new membership Slack message to the provided address.
     *
     * @param Member member
     */
    @PostMapping(path = {"/slack/new-membership"})
    public void testNewMembershipSlack(@RequestBody Member member) {
        slackService.sendNewMembershipMsg(member);
    }

    /**
     * Adds a person to the member audience in Mail Chimp.
     *
     * @param Member member
     */
    @PostMapping(path = {"/mailchimp/add-member"})
    public void addOrUpdateMemberToMailChimp(@RequestBody Member member) throws ResourceNotFoundException {
        mailChimpService.addOrUpdateMember(member.getFirstName(), member.getLastName(), member.getEmail());
    }

    /**
     * Adds a person to the non-member audience in Mail Chimp.
     *
     * @param Member member
     */
    @PostMapping(path = {"/mailchimp/add-non-member"})
    public void addOrUpdateNonMemberToMailChimp(@RequestBody Member member) throws ResourceNotFoundException {
        mailChimpService.addOrUpdateNonMember(member.getFirstName(), member.getLastName(), member.getEmail());
    }
}