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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eaa690.aerie.config.MembershipProperties;
import org.eaa690.aerie.config.CommonConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.MembershipReport;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.roster.RosterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Logs into EAA's roster management system, downloads the EAA 690 records as an
 * Excel spreadsheet. Then parses the spreadsheet for member details, and
 * inserts (or updates) member data in a local MySQL database.
 */
public class RosterService {

    /**
     * Date Format.
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMMMM dd, yyyy");

    /**
     * MembershipProperties.
     */
    @Autowired
    private MembershipProperties membershipProperties;

    /**
     * Template Engine.
     */
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * MemberRepository.
     */
    @Autowired
    private MemberRepository memberRepository;

    /**
     * RosterManager.
     */
    @Autowired
    private RosterManager rosterManager;

    /**
     * JotFormService.
     */
    @Autowired
    private JotFormService jotFormService;

    /**
     * EmailService.
     */
    @Autowired
    private EmailService emailService;

    /**
     * Sets MembershipProperties.
     * Note: mostly used for unit test mocks
     *
     * @param value MembershipProperties
     */
    @Autowired
    public void setMembershipProperties(final MembershipProperties value) {
        membershipProperties = value;
    }

    /**
     * Sets TemplateEngine.
     * Note: mostly used for unit test mocks
     *
     * @param value TemplateEngine
     */
    @Autowired
    public void setTemplateEngine(final TemplateEngine value) {
        templateEngine = value;
    }

    /**
     * Sets JotFormService. Note: mostly used for unit test mocks
     *
     * @param jfService JotFormService
     */
    @Autowired
    public void setJotFormService(final JotFormService jfService) {
        jotFormService = jfService;
    }

    /**
     * Sets EmailService. Note: mostly used for unit test mocks
     *
     * @param eService EmailService
     */
    @Autowired
    public void setEmailService(final EmailService eService) {
        emailService = eService;
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
     * Sets RosterManager. Note: mostly used for unit test mocks
     *
     * @param rManager RosterManager
     */
    @Autowired
    public void setRosterManager(final RosterManager rManager) {
        rosterManager = rManager;
    }

    /**
     * Updates every 6 hours.
     *
     * second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * *")
    public void update() {
        rosterManager.getAllEntries()
                .forEach(member -> {
                    memberRepository.findByRosterId(member.getRosterId())
                            .ifPresent(value -> member.setId(value.getId()));
                    if (member.getCreatedAt() == null) {
                        member.setCreatedAt(new Date());
                    }
                    member.setUpdatedAt(new Date());
                    memberRepository.save(member);
                });
    }

    /**
     * Sends membership renewal messages on a scheduled basis.
     *
     * second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 9 1,10,20 * *")
    public void sendMembershipRenewalMessages() {
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(m -> m.getExpiration()
                                .after(Date.from(Instant.now().minus(CommonConstants.THREE_HUNDRED_SIXTY_FIVE,
                                        ChronoUnit.DAYS))))
                        .filter(m -> m.getExpiration()
                                .before(Date.from(Instant.now().plus(CommonConstants.THIRTY, ChronoUnit.DAYS))))
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .forEach(this::sendRenewMembershipMsg));
    }

    /**
     * Sends membership renewal message to a specific member.
     *
     * @param member Member
     */
    public void sendRenewMembershipMsg(final Member member) {
        if (member.getEmail() != null && !"".equals(member.getEmail())) {
            final Context context = new Context();
            context.setVariable("member", member);
            context.setVariable("expiration", sdf.format(member.getExpiration()));
            final String renewMembershipUrl = jotFormService.buildRenewMembershipUrl(member);
            context.setVariable("url", "<a href=\"" + renewMembershipUrl + "\">" + renewMembershipUrl + "</a>");
            final String body = templateEngine.process("email/renewing-member", context);
            emailService.sendEmailMessage(member.getEmail(),
                    membershipProperties.getRenewSubject(),
                    body,
                    membershipProperties.getUsername(),
                    membershipProperties.getPassword());
        }
    }

    /**
     * Sends membership renewal message to a specific member.
     *
     * @param member Member
     */
    public void sendNewMembershipMsg(final Member member) {
        if (member.getEmail() != null && !"".equals(member.getEmail())) {
            final Context context = new Context();
            context.setVariable("member", member);
            context.setVariable("expiration", sdf.format(member.getExpiration()));
            final String body = templateEngine.process("email/new-member", context);
            emailService.sendEmailMessage(member.getEmail(),
                    membershipProperties.getNewSubject(),
                    body,
                    membershipProperties.getUsername(),
                    membershipProperties.getPassword());
        }
    }

    /**
     * Retrieves the member affiliated with the provided RFID.
     *
     * @param rfid RFID
     * @return Member
     * @throws ResourceNotFoundException when no member matches
     */
    public Member getMemberByRFID(final String rfid) throws ResourceNotFoundException {
        final Optional<Member> member = memberRepository.findByRfid(rfid);
        if (member.isPresent()) {
            return member.get();
        }
        throw new ResourceNotFoundException("No member found matching RFID=" + rfid);
    }

    /**
     * Retrieves the member affiliated with the provided ID.
     *
     * @param id Member ID
     * @return Member
     * @throws ResourceNotFoundException when no member matches
     */
    public Member getMemberByRosterID(final Long id) throws ResourceNotFoundException {
        Optional<Member> member = memberRepository.findByRosterId(id);
        if (member.isPresent()) {
            return member.get();
        }
        throw new ResourceNotFoundException("No member found matching ID=" + id);
    }

    /**
     * Gets all members.
     *
     * @return list of Member
     */
    public List<Member> getAllMembers() {
        return memberRepository.findAll().orElse(null);
    }

    /**
     * Updates a member's RFID to the provided value.
     *
     * @param id   Member Roster ID
     * @param rfid new RFID value
     * @throws ResourceNotFoundException when no member matches
     */
    public void updateMemberRFID(final Long id, final String rfid) throws ResourceNotFoundException {
        final Member member = getMemberByRosterID(id);
        member.setRfid(rfid);
        memberRepository.save(member);
    }

    /**
     * Generates a MembershipReport.
     *
     * @return MembershipReport
     */
    public MembershipReport getMembershipReport() {
        final Date today = new Date();
        final Date thirtyDays = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        final MembershipReport membershipReport = new MembershipReport();
        final List<Member> allMembers = memberRepository.findAll().orElse(new ArrayList<>());
        setActiveCounts(today, membershipReport, allMembers);
        setExpiredCounts(today, membershipReport, allMembers);
        setWillExpire30DaysCounts(today, thirtyDays, membershipReport, allMembers);
        membershipReport.setLifetimeMemberCount(
                allMembers.stream().filter(m -> MemberType.Lifetime == m.getMemberType()).count());
        membershipReport.setHonoraryMemberCount(
                allMembers.stream().filter(m -> MemberType.Honorary == m.getMemberType()).count());
        membershipReport.setProspectMemberCount(
                allMembers.stream().filter(m -> MemberType.Prospect == m.getMemberType()).count());
        membershipReport.setNonMemberCount(
                allMembers.stream().filter(m -> MemberType.NonMember == m.getMemberType()).count());
        return membershipReport;
    }

    private void setActiveCounts(final Date today, final MembershipReport membershipReport,
                                 final List<Member> allMembers) {
        membershipReport.setRegularMemberCount(
                allMembers.stream().filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration())).count());
        membershipReport.setFamilyMembershipCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration())).count());
        membershipReport.setFamilyMemberCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .map(Member::getNumOfFamily)
                        .filter(Objects::nonNull)
                        .reduce(0L, Long::sum));
        membershipReport.setStudentMemberCount(
                allMembers.stream().filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration())).count());
    }

    private void setWillExpire30DaysCounts(final Date today, final Date thirtyDays,
                                           final MembershipReport membershipReport,
                                           final List<Member> allMembers) {
        membershipReport.setRegularMemberWillExpire30DaysCount(
                allMembers.stream().filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipWillExpire30DaysCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMemberWillExpire30DaysCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .map(Member::getNumOfFamily)
                        .filter(Objects::nonNull)
                        .reduce(0L, Long::sum));
        membershipReport.setStudentMemberWillExpire30DaysCount(
                allMembers.stream().filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .count());
    }

    private void setExpiredCounts(final Date today, final MembershipReport membershipReport,
                                  final List<Member> allMembers) {
        membershipReport.setRegularMemberExpiredCount(
                allMembers.stream().filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.after(m.getExpiration())).count());
        membershipReport.setFamilyMembershipExpiredCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.after(m.getExpiration())).count());
        membershipReport.setFamilyMemberExpiredCount(allMembers.stream()
                .filter(m -> MemberType.Family == m.getMemberType())
                .filter(m -> Status.ACTIVE == m.getStatus())
                .filter(m -> today.after(m.getExpiration()))
                .map(Member::getNumOfFamily)
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum));
        membershipReport.setStudentMemberExpiredCount(
                allMembers.stream().filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.after(m.getExpiration())).count());
    }

}
