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

import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.service.RosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * SubscriptionController.
 */
@Controller
public class SubscriptionController {

    /**
     * RosterService.
     */
    private RosterService rosterService;

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
     * Constructor.
     *
     * @param rService RosterService
     */
    public SubscriptionController(final RosterService rService) {
        this.rosterService = rService;
    }

    /**
     * Start unsubscribe from email flow.
     *
     * @param model Model
     * @return subscribeemailstart
     */
    @GetMapping({"/subscribe/email"})
    public String subscribeEmailStart(final Model model) {
        return "subscribeemailstart";
    }

    /**
     * Confirm subscribe to email flow.
     *
     * @param model Model
     * @param member Member
     * @return subscribeemail
     */
    @PostMapping({"/subscribe/email/confirm"})
    public String subscribeEmail(final Model model, @RequestBody final Member member) {
        Member m = rosterService.getMemberByEmail(member.getEmail());
        if (m == null) {
            m = rosterService.addMember(member);
        }
        rosterService.enableMemberEmail(m.getRosterId());
        model.addAttribute("member", m);
        return "subscribeemail";
    }

    /**
     * Start unsubscribe from email flow.
     *
     * @param model Model
     * @param rosterId roster ID
     * @return unsubscribeemailstart
     */
    @GetMapping({"/unsubscribe/{rosterId}/email"})
    public String unsubscribeEmailStart(final Model model, @PathVariable("rosterId") final Long rosterId) {
        model.addAttribute("rosterId", rosterId);
        return "unsubscribeemailstart";
    }

    /**
     * Confirm unsubscribe from email flow.
     *
     * @param model Model
     * @param rosterId roster ID
     * @return unsubscribeemail
     */
    @PostMapping({"/unsubscribe/{rosterId}/email/confirm"})
    public String unsubscribeEmail(final Model model, @PathVariable("rosterId") final Long rosterId) {
        rosterService.disableMemberEmail(rosterId);
        return "unsubscribeemail";
    }
}
