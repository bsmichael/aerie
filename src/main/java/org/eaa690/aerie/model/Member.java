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

package org.eaa690.aerie.model;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.eaa690.aerie.model.roster.CellPhoneProvider;
import org.eaa690.aerie.model.roster.Country;
import org.eaa690.aerie.model.roster.Gender;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.State;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.model.roster.WebAdminAccess;

/**
 * Member.
 */
@Entity
@Table(name = "MEMBER")
@Getter
@Setter
public class Member extends BaseEntity implements Comparable<Member> {

    /**
     * Date representing the beginning of dates.
     */
    private static final Date ZERO_DATE = new Date(0);

    /**
     * Roster management system ID.
     */
    private Long rosterId;

    /**
     * Assigned RFID.
     */
    private String rfid;

    /**
     * Slack handle.
     */
    private String slack;

    /**
     * Family Slack handle(s).
     */
    private String familySlack;

    /**
     * First Name.
     */
    private String firstName;

    /**
     * Last Name.
     */
    private String lastName;

    /**
     * Nickname.
     */
    private String nickname;

    /**
     * Username.
     */
    private String username;

    /**
     * Spouse.
     */
    private String spouse;

    /**
     * Gender.
     */
    private Gender gender;

    /**
     * Member Type.
     */
    private MemberType memberType;

    /**
     * Status.
     */
    private Status status;

    /**
     * Web Admin Access.
     */
    private WebAdminAccess webAdminAccess;

    /**
     * Address Line 1.
     */
    private String addressLine1;

    /**
     * Address Line 2.
     */
    private String addressLine2;

    /**
     * City.
     */
    private String city;

    /**
     * State.
     */
    private State state;

    /**
     * Zip Code.
     */
    private String zipCode;

    /**
     * Country.
     */
    private Country country;

    /**
     * Birth Date.
     */
    private String birthDate;

    /**
     * Joined Date.
     */
    private String joined;

    /**
     * Other Information.
     *
     * "RFID=[ABC123ZXY43221]; Slack=[brian]; Family=[Jennifer Michael, Billy Michael]; # of Family=[2]; Additional "
     * "Info=[some random text]"
     */
    private String otherInfo;

    /**
     * Family.
     */
    private String family;

    /**
     * Num of Family.
     */
    private Long numOfFamily;

    /**
     * AdditionalInfo.
     */
    private String additionalInfo;

    /**
     * Home Phone.
     */
    private String homePhone;

    /**
     * Ratings.
     */
    private String ratings;

    /**
     * Aircraft Owned.
     */
    private String aircraftOwned;

    /**
     * Aircraft Project.
     */
    private String aircraftProject;

    /**
     * Aircraft Built.
     */
    private String aircraftBuilt;

    /**
     * IMC Club.
     */
    private boolean imcClub = Boolean.FALSE;

    /**
     * VMC Club.
     */
    private boolean vmcClub = Boolean.FALSE;

    /**
     * YE Pilot.
     */
    private boolean yePilot = Boolean.FALSE;

    /**
     * YE Volunteer.
     */
    private boolean yeVolunteer = Boolean.FALSE;

    /**
     * Eagle Pilot.
     */
    private boolean eaglePilot = Boolean.FALSE;

    /**
     * Eagle Volunteer.
     */
    private boolean eagleVolunteer = Boolean.FALSE;

    /**
     * EAA Membership Expiration Date.
     */
    @JsonFormat(pattern = "EEE. MMMMM dd, yyyy")
    private Date eaaExpiration;

    /**
     * Youth Protection Expiration Date.
     */
    @JsonFormat(pattern = "EEE. MMMMM dd, yyyy")
    private Date youthProtection;

    /**
     * Background Check Expiration Date.
     */
    @JsonFormat(pattern = "EEE. MMMMM dd, yyyy")
    private Date backgroundCheck;

    /**
     * EAA Number.
     */
    private String eaaNumber;

    /**
     * Email.
     */
    private String email;

    /**
     * Cell Phone.
     */
    private String cellPhone;

    /**
     * Cell Phone Provider.
     */
    @Enumerated(EnumType.STRING)
    private CellPhoneProvider cellPhoneProvider;

    /**
     * Membership Expiration.
     */
    @JsonFormat(pattern = "EEE. MMMMM dd, yyyy")
    private Date expiration;

    /**
     * Email Enabled Flag.
     */
    private boolean emailEnabled = false;

    /**
     * SMS Enabled Flag.
     */
    private boolean smsEnabled = false;

    /**
     * Slack Enabled Flag.
     */
    private boolean slackEnabled = false;

    /**
     * Required implementation.
     *
     * @param o Member
     * @return comparison
     */
    @Override
    public int compareTo(final Member o) {
        if (o != null && Objects.equals(getId(), o.getId())) {
            return 0;
        }
        return -1;
    }

    /**
     * Required implementation.
     *
     * @param o Object
     * @return if objects are the same
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Member member = (Member) o;
        return getRosterId().equals(member.getRosterId());
    }

    /**
     * Required implementation.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return getRosterId().hashCode();
    }
}
