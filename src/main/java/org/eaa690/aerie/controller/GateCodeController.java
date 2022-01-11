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

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.model.GateCode;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.SlackCommand;
import org.eaa690.aerie.service.GateCodeService;
import org.eaa690.aerie.service.JotFormService;
import org.eaa690.aerie.service.RosterService;
import org.eaa690.aerie.service.SlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * GateCodeController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/gatecodes"
})
@Slf4j
public class GateCodeController {

    /**
     * GateCodeService.
     */
    private GateCodeService gateCodeService;

    /**
     * RosterService.
     */
    private RosterService rosterService;

    /**
     * JotFormService.
     */
    private JotFormService jotFormService;

    /**
     * SlackService.
     */
    private org.eaa690.aerie.service.SlackService slackService;

    /**
     * Sets GateCodeService.
     *
     * @param value GateCodeService
     */
    @Autowired
    public void setGateCodeService(final GateCodeService value) {
        gateCodeService = value;
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
     * Sets RosterService.
     *
     * @param value RosterService
     */
    @Autowired
    public void setRosterService(final RosterService value) {
        rosterService = value;
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
     * Constructor.
     *
     * @param gcService GateCodeService
     */
    public GateCodeController(final GateCodeService gcService) {
        gateCodeService = gcService;
    }

    /**
     * Slack command to retrieve gate code.
     * Note: called via /gatecode within Slack
     *
     * @param message as received from Slack
     * @return user message
     */
    @PostMapping(path = {
            "/slack"
    })
    public String slashGateCode(@RequestBody final String message) {
        log.info("Received message from Slack: {}", message);
        final SlackCommand slackCommand = slackService.getSlackCommand(message);
        final Optional<Member> memberOpt = rosterService
                .getAllMembers()
                .stream()
                .filter(m -> slackCommand.getUserId().equalsIgnoreCase(slackService.getUserIDForUsername(m.getSlack())))
                .findFirst();
        if (memberOpt.isPresent()) {
            final Member member = memberOpt.get();
            if (member.getExpiration().after(new Date())) {
                final GateCode gateCode = gateCodeService.getCurrentGateCode();
                return "{\n" + "  \"response_type\": \"ephemeral\",\n"
                        + "  \"text\": \"" + gateCode.getDisplayText() + "\"\n" + "}";
            } else {
                return "{\n" + "  \"response_type\": \"ephemeral\",\n"
                        + "  \"text\": \"The gate code is provided to chapter members only.  "
                        + "Please renew your membership by going to "
                        + jotFormService.buildRenewMembershipUrl(member)
                        + "\"\n" + "}";
            }
        }
        return "{\n" + "  \"response_type\": \"ephemeral\",\n"
                + "  \"text\": \"The gate code is provided to chapter members only.  "
                + "To become a chapter member, go to "
                + jotFormService.getNewMembershipUrl()
                + "\"\n" + "}";
    }

    /**
     * Adds a gate code.
     *
     * @param gateCode GateCode
     */
    @PostMapping(path = {
            "/add"
    })
    public void add(@RequestBody final GateCode gateCode) {
        gateCodeService.setGateCode(gateCode);
    }

    /**
     * Gets all Gate codes.
     *
     * @return list of GateCode
     */
    @GetMapping()
    public List<GateCode> getAll() {
        return gateCodeService.getAll();
    }

}
