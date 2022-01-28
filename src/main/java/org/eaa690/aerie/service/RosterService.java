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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.config.MembershipProperties;
import org.eaa690.aerie.config.CommonConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberData;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.MembershipReport;
import org.eaa690.aerie.model.Message;
import org.eaa690.aerie.model.MessageRepository;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.roster.RosterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Logs into EAA's roster management system, downloads the EAA 690 records as an
 * Excel spreadsheet. Then parses the spreadsheet for member details, and
 * inserts (or updates) member data in a local MySQL database.
 */
@Slf4j
public class RosterService {

    /**
     * Date Format.
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMMMM dd, yyyy");

    /**
     * Date Format.
     */
    private final SimpleDateFormat ymdsdf = new SimpleDateFormat("yyyy-MM-dd");

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
     * MessageRepository.
     */
    @Autowired
    private MessageRepository messageRepository;

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
     * TrackingService.
     */
    @Autowired
    private TrackingService trackingService;

    /**
     * EmailService.
     */
    @Autowired
    private EmailService emailService;

    /**
     * SlackService.
     */
    @Autowired
    private SlackService slackService;

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
     * Sets TrackingService. Note: mostly used for unit test mocks
     *
     * @param tService TrackingService
     */
    @Autowired
    public void setTrackingService(final TrackingService tService) {
        trackingService = tService;
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
     * Sets SlackService. Note: mostly used for unit test mocks
     *
     * @param sService SlackService
     */
    @Autowired
    public void setSlackService(final SlackService sService) {
        slackService = sService;
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
     * Sets RosterManager. Note: mostly used for unit test mocks
     *
     * @param rManager RosterManager
     */
    @Autowired
    public void setRosterManager(final RosterManager rManager) {
        rosterManager = rManager;
    }

    /**
     * Updates local database with data from EAA Roster Management database.
     */
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
     * Sends membership renewal messages.
     */
    public void sendMembershipRenewalMessages() {
        getExpiringMembers().forEach(this::sendRenewMembershipMsg);
    }

    /**
     * Gets the list of members who are about to expire.
     *
     * @return list of expiring members
     */
    public List<Member> getNonNationalMembers() {
        final List<Member> membersList = new ArrayList<>();
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .filter(this::isCurrentChapterMember)
                        .filter(m -> !isCurrentNationalEAAMember(m))
                        .forEach(membersList::add));
        return membersList;
    }

    /**
     * Evaluates if the provided member is a current national member.
     *
     * @param member to be evaluated
     * @return membership currency
     */
    public boolean isCurrentNationalEAAMember(final Member member) {
        if (member.getEaaExpiration() == null) {
            return Boolean.FALSE;
        }
        return member.getEaaExpiration().after(new Date());
    }

    /**
     * Evaluates if the provided member is a current chapter member.
     *
     * @param member to be evaluated
     * @return membership currency
     */
    public boolean isCurrentChapterMember(final Member member) {
        if (member.getExpiration() == null) {
            return Boolean.FALSE;
        }
        return member.getExpiration().after(new Date());
    }

    /**
     * Gets the list of members who are about to expire.
     *
     * @return list of expiring members
     */
    public List<Member> getExpiringMembers() {
        final List<Member> membersList = new ArrayList<>();
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(m -> m.getExpiration()
                                .after(Date.from(Instant.now().minus(CommonConstants.THREE_HUNDRED_THIRTY,
                                        ChronoUnit.DAYS))))
                        .filter(m -> m.getExpiration()
                                .before(Date.from(Instant.now().plus(CommonConstants.THIRTY, ChronoUnit.DAYS))))
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .forEach(membersList::add));
        return membersList;
    }

    /**
     * Gets the list of members who are current.
     *
     * @return list of current members
     */
    public List<Member> getCurrentMembers() {
        final List<Member> membersList = new ArrayList<>();
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .filter(this::isCurrentChapterMember)
                        .forEach(membersList::add));
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(member -> member.getMemberType() == MemberType.Lifetime)
                        .forEach(membersList::add));
        return membersList;
    }

    /**
     * Gets the list of members who are about to expire.
     *
     * @return list of expiring members
     */
    public List<Member> getExpiredMembers() {
        final List<Member> membersList = new ArrayList<>();
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(m -> m.getExpiration().before(Date.from(Instant.now())))
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .forEach(membersList::add));
        return membersList;
    }

    /**
     * Gets the list of members who are new (past 30 days).
     *
     * @return list of new members
     */
    public List<Member> getNewMembersPastMonth() {
        final List<Member> membersList = new ArrayList<>();
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(m -> {
                            try {
                                return ymdsdf.parse(m.getJoined())
                                        .after(Date.from(Instant.now().minus(CommonConstants.THIRTY, ChronoUnit.DAYS)));
                            } catch (ParseException e) {
                                log.warn("Unable to parse date: {}", e.getMessage(), e);
                                return false;
                            }
                        })
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .forEach(membersList::add));
        return membersList;
    }

    /**
     * Gets the list of members who are new (past 1 year).
     *
     * @return list of new members
     */
    public List<Member> getNewMembersPastYear() {
        final List<Member> membersList = new ArrayList<>();
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(m -> {
                            try {
                                return ymdsdf.parse(m.getJoined())
                                        .after(Date.from(Instant.now()
                                                .minus(CommonConstants.THREE_HUNDRED_THIRTY, ChronoUnit.DAYS)));
                            } catch (ParseException e) {
                                return false;
                            }
                        })
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .forEach(membersList::add));
        return membersList;
    }

    /**
     * Sends membership renewal message to a specific member.
     *
     * @param member Member
     */
    public void sendRenewMembershipMsg(final Member member) {
        final Message message = messageRepository.save(new Message().sent(Instant.now()));
        final String renewMembershipUrl = jotFormService.buildRenewMembershipUrl(member);
        final Context context = new Context();
        context.setVariable("member", member);
        context.setVariable("expiration", sdf.format(member.getExpiration()));
        context.setVariable("unsubscribeUrl", buildUnsubscribeUrl(member));
        context.setVariable("trackingUrl",
                trackingService.generateTrackingLink(member.getRosterId(), message.getId()));
        context.setVariable("url", "<a href=\"" + renewMembershipUrl + "\">" + renewMembershipUrl + "</a>");
        if (member.getEmail() != null && !"".equals(member.getEmail())) {
            final String body = templateEngine.process("email/renewing-member", context);
            emailService.sendEmailMessage(message
                            .to(member.getEmail())
                            .subject(membershipProperties.getRenewSubject())
                            .body(body),
                    membershipProperties.getUsername(),
                    membershipProperties.getPassword());
        }
        if (member.getSlack() != null && !"".equals(member.getSlack())) {
            final String body = templateEngine.process("slack/renewing-member", context);
            slackService.sendSlackMessage(message.to(member.getSlack()).body(body));
        }
    }

    /**
     * Builds email unsubscribe link.
     *
     * @param member Member
     * @return URL
     */
    public String buildUnsubscribeUrl(final Member member) {
        return membershipProperties.getHost() + "/unsubscribe/" + member.getRosterId() + "/email";
    }

    /**
     * Sends membership renewal message to a specific member.
     *
     * @param member Member
     */
    public void sendNewMembershipMsg(final Member member) {
        final Message message = messageRepository.save(new Message().sent(Instant.now()));
        if (member.getEmail() != null && !"".equals(member.getEmail())) {
            final Context context = new Context();
            context.setVariable("member", member);
            context.setVariable("unsubscribeUrl", buildUnsubscribeUrl(member));
            context.setVariable("trackingUrl",
                    trackingService.generateTrackingLink(member.getRosterId(), message.getId()));
            context.setVariable("expiration", sdf.format(member.getExpiration()));
            final String body = templateEngine.process("email/new-member", context);
            emailService.sendEmailMessage(message
                            .to(member.getEmail())
                            .subject(membershipProperties.getNewSubject())
                            .body(body),
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
     * Retrieves the members affiliated with the provided first name.
     *
     * @param firstName First name
     * @return Member list
     */
    public List<Member> getMembersByFirstName(final String firstName) {
        return memberRepository.findByFirstName(firstName).orElseGet(ArrayList::new);
    }

    /**
     * Retrieves the members affiliated with the provided last name.
     *
     * @param lastName Last name
     * @return Member list
     */
    public List<Member> getMembersByLastName(final String lastName) {
        return memberRepository.findByLastName(lastName).orElseGet(ArrayList::new);
    }

    /**
     * Gets a member by email address.
     *
     * @param email address
     * @return Member
     */
    public Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }

    /**
     * Retrieves list of members matching provided criteria.
     *
     * @param firstName First name
     * @param lastName Last name
     * @return MemberData list
     */
    public List<MemberData> findByName(final String firstName, final String lastName) {
        final List<Member> firstNameMembers = getMembersByFirstName(firstName);
        final List<Member> lastNameMembers = getMembersByLastName(lastName);
        final List<MemberData> members = new ArrayList<>();
        if (firstNameMembers.isEmpty() && lastNameMembers.isEmpty()) {
            return members;
        }
        if (lastNameMembers.isEmpty()) {
            members.addAll(firstNameMembers
                    .stream()
                    .map(m -> new MemberData(m.getId(),
                            m.getRosterId(),
                            m.getFirstName() + " " + m.getLastName(),
                            m.getExpiration(),
                            m.getEaaExpiration(),
                            m.getYouthProtection(),
                            m.getBackgroundCheck(),
                            m.getRfid()))
                    .collect(Collectors.toList()));
        }
        if (firstNameMembers.isEmpty()) {
            members.addAll(lastNameMembers
                    .stream()
                    .map(m -> new MemberData(m.getId(),
                            m.getRosterId(),
                            m.getFirstName() + " " + m.getLastName(),
                            m.getExpiration(),
                            m.getEaaExpiration(),
                            m.getYouthProtection(),
                            m.getBackgroundCheck(),
                            m.getRfid()))
                    .collect(Collectors.toList()));
        }
        if (!firstNameMembers.isEmpty() && !lastNameMembers.isEmpty()) {
            members.addAll(firstNameMembers.stream()
                    .filter(lastNameMembers::contains)
                    .map(m -> new MemberData(m.getId(),
                            m.getRosterId(),
                            m.getFirstName() + " " + m.getLastName(),
                            m.getExpiration(),
                            m.getEaaExpiration(),
                            m.getYouthProtection(),
                            m.getBackgroundCheck(),
                            m.getRfid()))
                    .collect(Collectors.toList()));
        }
        return members;
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

    /**
     * Disables member email enabled flag.
     *
     * @param rosterId roster ID
     */
    public void disableMemberEmail(final Long rosterId) {
        try {
            final Member member = getMemberByRosterID(rosterId);
            member.setEmailEnabled(Boolean.FALSE);
            memberRepository.save(member);
        } catch (ResourceNotFoundException e) {
            log.error("Unable to find member for ID: {}", rosterId);
        }
    }

    /**
     * Enables member email enabled flag.
     *
     * @param rosterId roster ID
     */
    public void enableMemberEmail(final Long rosterId) {
        try {
            final Member member = getMemberByRosterID(rosterId);
            member.setEmailEnabled(Boolean.TRUE);
            memberRepository.save(member);
        } catch (ResourceNotFoundException e) {
            log.error("Unable to find member for ID: {}", rosterId);
        }
    }

    /**
     * Adds a member.
     *
     * @param member Member
     * @return Member
     */
    public Member addMember(final Member member) {
        return member;
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
