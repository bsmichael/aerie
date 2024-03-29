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

import org.eaa690.aerie.service.RosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ReportController.
 */
@Controller
public class ReportController {

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
    public ReportController(final RosterService rService) {
        this.rosterService = rService;
    }

    /**
     * Membership Report.
     *
     * @param model Model
     * @return report
     */
    @GetMapping({"/reports/members"})
    public String membershipReport(final Model model) {
        model.addAttribute("report", rosterService.getMembershipReport());
        return "membershipreport";
    }

    /**
     * Non-National Membership Report.
     *
     * @param model Model
     * @return report
     */
    @GetMapping({"/reports/nonnational"})
    public String nonNationalMembershipReport(final Model model) {
        model.addAttribute("members", rosterService.getNonNationalMembers());
        return "nonnationalmembersreport";
    }

    /**
     * Member Search.
     *
     * @param firstName First name
     * @param lastName Last name
     * @param model Model
     * @return results
     */
    @GetMapping({"/members/search"})
    public String membershipReport(@RequestParam(name = "firstName", required = false) final String firstName,
                                   @RequestParam(name = "lastName", required = false) final String lastName,
                                   final Model model) {
        model.addAttribute("members", rosterService.findByName(firstName, lastName));
        return "membersearch";
    }

    /**
     * Expiring Report.
     *
     * @param model Model
     * @return report
     */
    @GetMapping({"/reports/members/expiring"})
    public String expiringReport(final Model model) {
        model.addAttribute("members", rosterService.getExpiringMembers());
        return "expiringreport";
    }

    /**
     * Expired Report.
     *
     * @param model Model
     * @return report
     */
    @GetMapping({"/reports/members/expired"})
    public String expiredReport(final Model model) {
        model.addAttribute("members", rosterService.getExpiredMembers());
        return "expiredreport";
    }

    /**
     * Current Report.
     *
     * @param model Model
     * @return report
     */
    @GetMapping({"/reports/members/current"})
    public String currentReport(final Model model) {
        model.addAttribute("members", rosterService.getCurrentMembers());
        return "currentreport";
    }

    /**
     * Expired Report.
     *
     * @param model Model
     * @return report
     */
    @GetMapping({"/reports/members/new"})
    public String newMembersReport(final Model model) {
        model.addAttribute("membersMonth", rosterService.getNewMembersPastMonth());
        model.addAttribute("membersYear", rosterService.getNewMembersPastYear());
        return "newmembersreport";
    }

}
