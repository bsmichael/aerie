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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.MembershipReport;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.Person;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.roster.RosterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Logs into EAA's roster management system, downloads the EAA 690 records as an
 * Excel spreadsheet. Then parses the spreadsheet for member details, and
 * inserts (or updates) member data in a local MySQL database.
 */
public class RosterService {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(RosterService.class);

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

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
     * SlackService.
     */
    @Autowired
    private SlackService slackService;

    /**
     * SMSService.
     */
    @Autowired
    private SMSService smsService;

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
     * Sets SlackService. Note: mostly used for unit test mocks
     *
     * @param sService SlackService
     */
    @Autowired
    public void setSlackService(final SlackService sService) {
        slackService = sService;
    }

    /**
     * Sets SMSService. Note: mostly used for unit test mocks
     *
     * @param sService SMSService
     */
    @Autowired
    public void setSMSService(final SMSService sService) {
        smsService = sService;
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
     * MapperFactory.
     */
    private final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

    /**
     * Updates every 6 hours.
     *
     * second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * *")
    public void update() {
        mapperFactory.classMap(Person.class, Member.class).byDefault();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        rosterManager.getAllEntries().stream().map(person -> mapper.map(person, Member.class))
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
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendMembershipRenewalMessages() {
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(m -> m.getExpiration()
                                .before(Date.from(Instant.now().plus(CommonConstants.THIRTY, ChronoUnit.DAYS))))
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .forEach(m -> {
                            try {
                                if (m.isEmailEnabled()) {
                                    emailService.sendEmailMessage(m.getEmail(),
                                            propertyService.get("SUBJECT").getValue(),
                                            personalizeBody(m, propertyService.get("BODY").getValue()),
                                            propertyService.get("FROM").getValue(),
                                            propertyService.get("PASSWORD").getValue(),
                                            propertyService.get("IMG").getValue());
                                }
                                if (m.isSlackEnabled()) {
                                    slackService
                                            .sendSlackMessage(m.getSlack(),
                                                    personalizeBody(m, propertyService.get("BODY").getValue()));
                                }
                                if (m.isSmsEnabled()) {
                                    smsService.sendSMSMessage(m.getCellPhone(),
                                            m.getCellPhoneProvider(),
                                            propertyService.get("SUBJECT").getValue(),
                                            personalizeBody(m, propertyService.get("BODY").getValue()),
                                            propertyService.get("FROM").getValue(),
                                            propertyService.get("PASSWORD").getValue());
                                }
                            } catch (ResourceNotFoundException rnfe) {
                                // Do something
                            }
                        }));
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
     * Saves member information to roster.
     *
     * @param member Member to be saved
     * @return saved member
     */
    public Member saveNewMember(final Member member) {
        LOGGER.info("Saving new member: " + member);
        mapperFactory.classMap(Member.class, Person.class);
        MapperFacade mapper = mapperFactory.getMapperFacade();
        rosterManager.savePerson(mapper.map(member, Person.class));
        return member;
    }

    /**
     * Saves member information to roster.
     *
     * @param member Member to be saved
     */
    public void saveRenewingMember(final Member member) {
        LOGGER.info("Saving renewing member: " + member);
        mapperFactory.classMap(Member.class, Person.class);
        MapperFacade mapper = mapperFactory.getMapperFacade();
        rosterManager.savePerson(mapper.map(member, Person.class));
    }

    /**
     * Generates a MembershipReport.
     *
     * @return MembershipReport
     */
    public MembershipReport getMembershipReport() {
        final Date today = new Date();
        final Date thirtyDays = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        final Date sevenDays = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
        final MembershipReport membershipReport = new MembershipReport();
        final List<Member> allMembers = memberRepository.findAll().orElse(new ArrayList<>());
        setActiveCounts(today, membershipReport, allMembers);
        setExpiredCounts(today, membershipReport, allMembers);
        setWillExpire30DaysCounts(today, thirtyDays, sevenDays, membershipReport, allMembers);
        setWillExpire7DaysCounts(today, sevenDays, membershipReport, allMembers);
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
                        .map(Member::getNumOfFamily).reduce(0L, Long::sum));
        membershipReport.setStudentMemberCount(
                allMembers.stream().filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration())).count());
    }

    private void setWillExpire7DaysCounts(final Date today, final Date sevenDays,
                                          final MembershipReport membershipReport, final List<Member> allMembers) {
        membershipReport.setRegularMemberWillExpire7DaysCount(
                allMembers.stream().filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration())).count());
        membershipReport.setFamilyMembershipWillExpire7DaysCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration())).count());
        membershipReport.setFamilyMemberWillExpire7DaysCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration()))
                        .map(Member::getNumOfFamily).reduce(0L, Long::sum));
        membershipReport.setStudentMemberWillExpire7DaysCount(
                allMembers.stream().filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration())).count());
    }

    private void setWillExpire30DaysCounts(final Date today, final Date thirtyDays, final Date sevenDays,
                                           final MembershipReport membershipReport, final List<Member> allMembers) {
        membershipReport.setRegularMemberWillExpire30DaysCount(
                allMembers.stream().filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration())).count());
        membershipReport.setFamilyMembershipWillExpire30DaysCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration())).count());
        membershipReport.setFamilyMemberWillExpire30DaysCount(
                allMembers.stream().filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration()))
                        .map(Member::getNumOfFamily).reduce(0L, Long::sum));
        membershipReport.setStudentMemberWillExpire30DaysCount(
                allMembers.stream().filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration())).count());
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
                .filter(m -> Status.ACTIVE == m.getStatus()).filter(m -> today.after(m.getExpiration()))
                .map(Member::getNumOfFamily).reduce(0L, Long::sum));
        membershipReport.setStudentMemberExpiredCount(
                allMembers.stream().filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.after(m.getExpiration())).count());
    }

    /**
     * Builds message to be sent to member.
     *
     * @param member Member
     * @param body message body
     * @return message
     */
    private String personalizeBody(final Member member, final String body) {
        final String expiration;
        if (member.getExpiration() != null) {
            expiration = ZonedDateTime.ofInstant(member.getExpiration().toInstant(), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        } else {
            expiration = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }
        return body.replaceAll("\\{\\{firstName\\}\\}", member.getFirstName())
                .replaceAll("\\{\\{lastName\\}\\}", member.getLastName())
                .replaceAll("\\{\\{expirationDate\\}\\}", expiration)
                .replaceAll("\\{\\{url\\}\\}", jotFormService.buildRenewMembershipUrl(member));
    }

}
