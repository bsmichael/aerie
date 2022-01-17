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

package org.eaa690.aerie.model.roster;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OtherInfoBuilder.
 */
@Getter
@Setter
@Slf4j
public class OtherInfoBuilder {

    /**
     * Family Pattern.
     */
    private static Pattern additionalFamilyPattern = Pattern.compile("Family=\\[(.*?)\\]");

    /**
     * # of Family Pattern.
     */
    private static Pattern numOfFamilyPattern = Pattern.compile("# of Family=\\[(.*?)\\]");

    /**
     * Slack Pattern.
     */
    private static Pattern slackPattern = Pattern.compile("Slack=\\[(.*?)\\]");

    /**
     * Family Slack Pattern.
     */
    private static Pattern familySlackPattern = Pattern.compile("Family Slack=\\[(.*?)\\]");

    /**
     * RFID Pattern.
     */
    private static Pattern rfidPattern = Pattern.compile("RFID=\\[(.*?)\\]");

    /**
     * Additional Info Pattern.
     */
    private static Pattern additionalInfoPattern = Pattern.compile("Additional Info=\\[(.*?)\\]");

    /**
     * Additional Family.
     */
    private String additionalFamily;

    /**
     * Slack.
     */
    private String slack;

    /**
     * Family Slack.
     */
    private String familySlack;

    /**
     * Number of Family.
     */
    private Long numOfFamily;

    /**
     * RFID.
     */
    private String rfid;

    /**
     * Additional Information.
     */
    private String additionalInfo;

    /**
     * Default constructor.
     */
    public OtherInfoBuilder() {
        // Default constructor
    }

    /**
     * Gets raw value.
     *
     * @return raw value
     */
    public String getRaw() {
        final List<String> parts = new ArrayList<>();
        if (additionalFamily != null) {
            parts.add(String.format("Family=[%s]", additionalFamily));
        }
        if (numOfFamily != null) {
            parts.add(String.format("# of Family=[%s]", numOfFamily));
        }
        if (slack != null) {
            parts.add(String.format("Slack=[%s]", slack));
        }
        if (familySlack != null) {
            parts.add(String.format("Family Slack=[%s]", familySlack));
        }
        if (rfid != null) {
            parts.add(String.format("RFID=[%s]", rfid));
        }
        if (additionalInfo != null) {
            parts.add(String.format("Additional Info=[%s]", additionalInfo));
        }
        return String.join("; ", parts);
    }

    /**
     * Sets raw value.
     *
     * @param raw string
     */
    public void setRaw(final String raw) {
        boolean matched = false;
        if (raw != null) {
            final Matcher additionalFamilyMatcher = additionalFamilyPattern.matcher(raw);
            if (additionalFamilyMatcher.find()) {
                matched = true;
                setAdditionalFamily(additionalFamilyMatcher.group(1));
                log.debug("Set additional family to [" + getAdditionalFamily() + "]");
            }
            final Matcher numOfFamilyMatcher = numOfFamilyPattern.matcher(raw);
            if (numOfFamilyMatcher.find()) {
                try {
                    setNumOfFamily(Long.parseLong(numOfFamilyMatcher.group(1)));
                    matched = true;
                    log.debug("Set number of family to [" + getNumOfFamily() + "]");
                } catch (NumberFormatException nfe) {
                    log.info("Unable to parse number of family value={} from {}",
                            numOfFamilyMatcher.group(1), raw);
                }
            }
            final Matcher slackMatcher = slackPattern.matcher(raw);
            if (slackMatcher.find()) {
                matched = true;
                setSlack(slackMatcher.group(1));
                log.debug("Set Slack to [" + getSlack() + "]");
            }
            final Matcher familySlackMatcher = familySlackPattern.matcher(raw);
            if (familySlackMatcher.find()) {
                matched = true;
                setFamilySlack(familySlackMatcher.group(1));
                log.debug("Set Slack to [" + getFamilySlack() + "]");
            }
            final Matcher rfidMatcher = rfidPattern.matcher(raw);
            if (rfidMatcher.find()) {
                matched = true;
                setRfid(rfidMatcher.group(1));
                log.debug("Set RFID to [" + getRfid() + "]");
            }
            final Matcher additionalInfoMatcher = additionalInfoPattern.matcher(raw);
            if (additionalInfoMatcher.find()) {
                matched = true;
                setAdditionalInfo(additionalInfoMatcher.group(1));
                log.debug("Set additional info to [" + getAdditionalInfo() + "]");
            }
            if (!matched) {
                log.debug("No patterns matched.  Setting additional info to [" + raw + "]");
                setAdditionalInfo(raw);
            }
        }
    }

    /**
     * toString.
     *
     * @return raw string
     */
    @Override
    public String toString() {
        return getRaw();
    }
}
