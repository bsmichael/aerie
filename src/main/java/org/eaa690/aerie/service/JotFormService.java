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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.JotForm;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.OtherInfoBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import io.github.bsmichael.rostermanagement.model.State;

/**
 * Retrieves form submissions to JotForm.
 */
public class JotFormService {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(JotFormService.class);

    /**
     * Answers.
     */
    private static final String ANSWERS = "answers";

    /**
     * Answer.
     */
    private static final String ANSWER = "answer";

    /**
     * Used to filter form submissions retrieval.
     */
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

    /**
     * Submissions cache.
     */
    private static final Cache<String, String> SUBMISSIONS_CACHE =
            CacheBuilder.newBuilder().expireAfterWrite(CommonConstants.THIRTY_SIX, TimeUnit.HOURS).build();

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * TinyURLService.
     */
    private TinyURLService tinyUrlService;

    /**
     * RosterService.
     */
    @Autowired
    private RosterService rosterService;

    /**
     * EmailService.
     */
    @Autowired
    private CommunicationService communicationService;

    /**
     * Sets TinyURLService.
     * Note: mostly used for unit test mocks
     *
     * @param value TinyURLService
     */
    @Autowired
    public void setTinyURLService(final TinyURLService value) {
        tinyUrlService = value;
    }

    /**
     * Sets PropertyService.
     * Note: mostly used for unit test mocks
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    /**
     * Sets RosterService.
     * Note: mostly used for unit test mocks
     *
     * @param value RosterService
     */
    @Autowired
    public void setRosterService(final RosterService value) {
        rosterService = value;
    }

    /**
     * Sets EmailService.
     * Note: mostly used for unit test mocks
     *
     * @param value EmailService
     */
    @Autowired
    public void setCommunicationService(final CommunicationService value) {
        communicationService = value;
    }

    /**
     * Retrieves JotForm submissions.
     *
     * second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 * * * *")
    public void getSubmissions() {
        try {
            final String dateStr = simpleDateFormat.format(new Date());
            final JotForm client =
                    new JotForm(propertyService.get(PropertyKeyConstants.JOTFORM_API_KEY_KEY).getValue());
            processNewMemberSubmissions(dateStr, client);
            processRenewingMemberSubmissions(dateStr, client);
        } catch (ResourceNotFoundException rnfe) {
            LOGGER.error(rnfe);
        }
    }

    /**
     * Builds a member's renew membership URL, complete with pre-populated fields.
     *
     * @param member Member
     * @return URL
     */
    public String buildRenewMembershipUrl(final Member member) {
        try {
            final StringBuilder sb = new StringBuilder();
            // https://form.jotform.com/
            sb.append(propertyService.get(PropertyKeyConstants.JOTFORM_BASE_URL_KEY).getValue());
            // 203205658119150
            sb.append(propertyService.get(PropertyKeyConstants.JOTFORM_MEMBER_RENEWAL_FORM_ID_KEY).getValue());
            sb.append("?");
            final List<String> parameters = new ArrayList<>();
            parameters.add("fullName3[first]=" + handleNull(member.getFirstName()));
            parameters.add("fullName3[last]=" + handleNull(member.getLastName()));
            parameters.add("address4[addr_line1]=" + handleNull(member.getAddressLine1()));
            parameters.add("address4[addr_line2]=" + handleNull(member.getAddressLine2()));
            parameters.add("address4[city]=" + handleNull(member.getCity()));
            parameters.add("address4[state]=" + handleNull(State.getDisplayString(member.getState())));
            parameters.add("address4[postal]=" + handleNull(member.getZipCode()));
            parameters.add("phoneNumber5=" + handleNull(member.getCellPhone())); // TODO
            parameters.add("email6=" + handleNull(member.getEmail()));
            parameters.add("additionalFamily9="); // TODO
            parameters.add("numberOf="); // TODO
            parameters.add("eaaNational15=" + handleNull(member.getEaaNumber()));
            parameters.add("additionalInformation="); // TODO
            sb.append(StringUtils.join(parameters, "&"));
            return tinyUrlService.getTinyURL(sb.toString());
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error", e);
        }
        return null;
    }

    /**
     * Ensures string is either an empty string or a text value.  Never null.
     *
     * @param value to be evaulated
     * @return non-null value
     */
    private String handleNull(final String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * Processes renewing member submissions.
     *
     * @param dateStr Date
     * @param client JotFormClient
     * @throws ResourceNotFoundException when property is not found
     */
    private void processRenewingMemberSubmissions(final String dateStr, final JotForm client)
            throws ResourceNotFoundException {
        final HashMap<String, String> submissionFilter = new HashMap<>();
        submissionFilter.put("id:gt",
                propertyService.get(PropertyKeyConstants.JOTFORM_MEMBER_RENEWAL_FORM_ID_KEY).getValue());
        submissionFilter.put("created_at:gt", dateStr);
        LOGGER.info("Querying for member renewal form submissions after " + dateStr);
        final Map<String, Member> renewMembersMap =
                parseRenewingMember(client.getSubmissions("0", "1000", submissionFilter, "created_at"));

        if (!renewMembersMap.isEmpty()) {
            LOGGER.info("RenewMembersMap size is " + renewMembersMap.size());
            for (final Map.Entry<String, Member> entry : renewMembersMap.entrySet()) {
                final String key = entry.getKey();
                if (SUBMISSIONS_CACHE.getIfPresent(key) == null) {
                    SUBMISSIONS_CACHE.put(key, key);
                    rosterService.saveRenewingMember(entry.getValue());
                }
            }
        }
    }

    /**
     * Processes new member submissions.
     *
     * @param dateStr Date
     * @param client JotFormClient
     * @throws ResourceNotFoundException when property is not found
     */
    private void processNewMemberSubmissions(final String dateStr, final JotForm client)
            throws ResourceNotFoundException {
        final HashMap<String, String> submissionFilter = new HashMap<>();
        submissionFilter.put("id:gt",
                propertyService.get(PropertyKeyConstants.JOTFORM_NEW_MEMBER_FORM_ID_KEY).getValue());
        submissionFilter.put("created_at:gt", dateStr);
        LOGGER.info("Querying for new member form submissions after " + dateStr);
        final Map<String, Member> newMembersMap =
                parseNewMember(client.getSubmissions("0", "1000", submissionFilter, "created_at"));

        if (!newMembersMap.isEmpty()) {
            LOGGER.info("NewMembersMap size is " + newMembersMap.size());
            for (final Map.Entry<String, Member> entry : newMembersMap.entrySet()) {
                final String key = entry.getKey();
                if (SUBMISSIONS_CACHE.getIfPresent(key) == null) {
                    final Member member = rosterService.saveNewMember(entry.getValue());
                    SUBMISSIONS_CACHE.put(key, key);
                    communicationService.sendNewMembershipMsg(member);
                }
            }
        }
    }

    /**
     * Parses new member information from JotForm.
     *
     * @param submission JotForm
     * @return membersMap map of new members
     */
    private Map<String, Member> parseNewMember(final JSONObject submission) {
        final Map<String, Member> map = new HashMap<>();
        final Date oneYearFromNow = Date.from(Instant.now().plus(365, ChronoUnit.DAYS));
        final JSONArray content = submission.getJSONArray("content");
        for (int i = 0; i < content.length(); i++) {
            final Member member = new Member();
            final OtherInfoBuilder otherInfoBuilder = new OtherInfoBuilder();
            final JSONObject object = content.getJSONObject(i);
            if (object.has(ANSWERS)) {
                final JSONObject answers = object.getJSONObject(ANSWERS);
                parseName(member, answers);
                parseAddress(member, answers);
                parsePhone(member, answers);
                parseEmail(member, answers);
                parseAdditionalFamily(otherInfoBuilder, answers);
                parseAdditionalInfo(otherInfoBuilder, answers);
                parseEAANumber(member, answers);
                parseNumOfFamily(otherInfoBuilder, answers);
                member.setExpiration(oneYearFromNow);
                member.setOtherInfo(otherInfoBuilder.getRaw());
            }
            map.put((String) object.get("id"), member);
        }
        return map;
    }

    /**
     * Parses renewing member information from JotForm.
     *
     * @param submission JotForm
     * @return membersMap map of new members
     */
    private Map<String, Member> parseRenewingMember(final JSONObject submission) {
        final Map<String, Member> map = new HashMap<>();
        final Date oneYearFromNow = Date.from(Instant.now().plus(365, ChronoUnit.DAYS));
        final JSONArray content = submission.getJSONArray("content");
        for (int i = 0; i < content.length(); i++) {
            final Member member = new Member();
            final OtherInfoBuilder otherInfoBuilder = new OtherInfoBuilder();
            final JSONObject object = content.getJSONObject(i);
            if (object.has(ANSWERS)) {
                final JSONObject answers = object.getJSONObject(ANSWERS);
                parseName(member, answers);
                parseAddress(member, answers);
                parsePhone(member, answers);
                parseEmail(member, answers);
                parseAdditionalFamily(otherInfoBuilder, answers);
                parseAdditionalInfo(otherInfoBuilder, answers);
                parseEAANumber(member, answers);
                parseNumOfFamily(otherInfoBuilder, answers);
                member.setExpiration(oneYearFromNow);
                member.setOtherInfo(otherInfoBuilder.getRaw());
            }
            map.put((String) object.get("id"), member);
        }
        return map;
    }

    /**
     * Parses number of family members.
     *
     * @param otherInfoBuilder OtherInfoBuilder
     * @param answers JotForm
     */
    private void parseNumOfFamily(final OtherInfoBuilder otherInfoBuilder, final JSONObject answers) {
        if (answers.has("17")) {
            JSONObject numOfFamily = answers.getJSONObject("17");
            if (numOfFamily.has(ANSWER)) {
                try {
                    otherInfoBuilder.setNumOfFamily(Long.parseLong(numOfFamily.getString(ANSWER)));
                } catch (NumberFormatException nfe) {
                    LOGGER.error("Unable to parse number of family value=[" + numOfFamily.getString(ANSWER) + "]");
                }
            }
        }
    }

    /**
     * Parses EAA number.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseEAANumber(final Member member, final JSONObject answers) {
        if (answers.has("15")) {
            JSONObject eaaNumber = answers.getJSONObject("15");
            if (eaaNumber.has(ANSWER)) {
                member.setEaaNumber(eaaNumber.getString(ANSWER));
            }
        }
    }

    /**
     * Parses additional info.
     *
     * @param otherInfoBuilder OtherInfoBuilder
     * @param answers JotForm
     */
    private void parseAdditionalInfo(final OtherInfoBuilder otherInfoBuilder, final JSONObject answers) {
        if (answers.has("11")) {
            JSONObject additionalInfo = answers.getJSONObject("11");
            if (additionalInfo.has(ANSWER)) {
                otherInfoBuilder.setAdditionalInfo(additionalInfo.getString(ANSWER));
            }
        }
    }

    /**
     * Parses additional family.
     *
     * @param otherInfoBuilder OtherInfoBuilder
     * @param answers JotForm
     */
    private void parseAdditionalFamily(final OtherInfoBuilder otherInfoBuilder, final JSONObject answers) {
        if (answers.has("9")) {
            final JSONObject additionalFamily = answers.getJSONObject("9");
            if (additionalFamily.has(ANSWER)) {
                otherInfoBuilder.setAdditionalFamily(additionalFamily.getString(ANSWER));
            }
        }
    }

    /**
     * Parses email.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseEmail(final Member member, final JSONObject answers) {
        if (answers.has("6")) {
            final JSONObject email = answers.getJSONObject("6");
            if (email.has(ANSWER)) {
                member.setEmail(email.getString(ANSWER));
            }
        }
    }

    /**
     * Parses phone number.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parsePhone(final Member member, final JSONObject answers) {
        if (answers.has("5")) {
            final JSONObject phone = answers.getJSONObject("5");
            if (phone.has(ANSWER)) {
                final JSONObject phoneAnswer = phone.getJSONObject(ANSWER);
                if (phoneAnswer.has("full")) {
                    member.setHomePhone(phoneAnswer.getString("full"));
                }
            }
        }
    }

    /**
     * Parses address.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseAddress(final Member member, final JSONObject answers) {
        if (answers.has("4")) {
            final JSONObject address = answers.getJSONObject("4");
            if (address.has(ANSWER)) {
                final JSONObject addressAnswer = address.getJSONObject(ANSWER);
                if (addressAnswer.has("addr_line1")) {
                    member.setAddressLine1(addressAnswer.getString("addr_line1"));
                }
                if (addressAnswer.has("city")) {
                    member.setCity(addressAnswer.getString("city"));
                }
                if (addressAnswer.has("state")) {
                    member.setState(State.deriveState(addressAnswer.getString("state")));
                }
                if (addressAnswer.has("postal")) {
                    member.setZipCode(addressAnswer.getString("postal"));
                }
            }
        }
    }

    /**
     * Parses name.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseName(final Member member, final JSONObject answers) {
        if (answers.has("3")) {
            final JSONObject fullName = answers.getJSONObject("3");
            if (fullName.has(ANSWER)) {
                final JSONObject fullNameAnswer = fullName.getJSONObject(ANSWER);
                if (fullNameAnswer.has("first")) {
                    member.setFirstName(fullNameAnswer.getString("first"));
                }
                if (fullNameAnswer.has("last")) {
                    member.setLastName(fullNameAnswer.getString("last"));
                }
            }
        }
    }
}
