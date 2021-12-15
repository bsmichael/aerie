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
import org.eaa690.aerie.model.MemberData;
import org.eaa690.aerie.model.FindByRFIDResponse;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.RFIDRequest;
import org.eaa690.aerie.service.JotFormService;
import org.eaa690.aerie.service.MailChimpService;
import org.eaa690.aerie.service.RosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * RosterController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/roster"
})
public class RosterController {

    /**
     * RosterService.
     */
    private RosterService rosterService;

    /**
     * JotFormService.
     */
    private JotFormService jotFormService;

    /**
     * MailChimpService.
     */
    private MailChimpService mailChimpService;

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
     * Sets JotFormService.
     *
     * @param value JotFormService
     */
    @Autowired
    public void setJotFormService(final JotFormService value) {
        jotFormService = value;
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
     * Updates data from roster database.
     */
    @PostMapping(path = {
            "/update"
    })
    public void update() {
        rosterService.update();
    }

    /**
     * Retrieves and processes any JotForm submissions.
     */
    @PostMapping(path = {
            "/jotform"
    })
    public void jotFormSubmissions() {
        jotFormService.getSubmissions();
    }

    /**
     * Sends Membership Renewal Messages.
     *
     * @param rosterId Member Roster ID (-1 for all expiring members)
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/membership/renew/{rosterId}"})
    public void sendMembershipRenewalMessages(@PathVariable("rosterId") final Long rosterId)
            throws ResourceNotFoundException {
        if (rosterId == -1) {
            rosterService.sendMembershipRenewalMessages();
        } else {
            final Member member = rosterService.getMemberByRosterID(rosterId);
            rosterService.sendRenewMembershipMsg(member);
        }
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
     * Get member's data.
     *
     * @param rosterId Member's Roster ID
     * @return MemberData
     * @throws ResourceNotFoundException when no member data is found
     */
    @GetMapping(path = {"/{rosterId}/expiration"})
    public MemberData getMemberData(@PathVariable("rosterId") final Long rosterId)
            throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        final MemberData record = new MemberData();
        record.setId(member.getId());
        record.setExpirationDate(member.getExpiration());
        record.setRfid(member.getRfid());
        record.setName(member.getFirstName() + " " + member.getLastName());
        return record;
    }

    /**
     * Updates a member's RFID.
     *
     * @param memberId Member's Roster ID
     * @param rfidRequest RFIDRequest
     * @throws ResourceNotFoundException when no member data is found
     */
    @PutMapping(path = {"/{memberId}/rfid"})
    public void updateRFID(@PathVariable("memberId") final Long memberId, @RequestBody final RFIDRequest rfidRequest)
            throws ResourceNotFoundException {
        rosterService.updateMemberRFID(memberId, rfidRequest.getRfid());
    }

    /**
     * Retrieves a member's ID (and whether or not they are an admin) from the provided RFID.
     *
     * @param rfidRequest RFIDRequest
     * @return FindByRFIDResponse
     * @throws ResourceNotFoundException when RFID is not found
     */
    @PostMapping(path = {"/find-by-rfid"})
    public FindByRFIDResponse findByRFID(@RequestBody final RFIDRequest rfidRequest) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRFID(rfidRequest.getRfid());
        final FindByRFIDResponse rfidResponse = new FindByRFIDResponse();
        rfidResponse.setId(member.getId());
        rfidResponse.setAdmin(Boolean.FALSE); // TODO
        rfidResponse.setRosterId(member.getRosterId());
        return rfidResponse;
    }

    /**
     * Gets all member's RFID data.
     *
     * @return list of MemberData
     */
    @GetMapping(path = {"/all-rfid"})
    public List<MemberData> allMemberRFIDData() {
        final List<MemberData> records = new ArrayList<>();
        final List<Member> members = rosterService.getAllMembers();
        for (Member member : members) {
            final MemberData record = new MemberData();
            record.setId(member.getId());
            record.setExpirationDate(member.getExpiration());
            record.setRfid(member.getRfid());
            record.setName(member.getFirstName() + " " + member.getLastName());
            records.add(record);
        }
        return records;
    }
}
