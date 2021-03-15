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

import org.eaa690.aerie.model.roster.Country;
import org.eaa690.aerie.model.roster.Gender;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.State;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.model.roster.WebAdminAccess;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

/**
 * Member.
 */
@Entity
@Table(name = "MEMBER")
public class Member extends BaseEntity implements Comparable<Member> {

    /**
     * Roster management system ID.
     */
    private Long rosterId;

    /**
     * Assigned RFID.
     */
    private String rfid;

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
     * "RFID=[ABC123ZXY43221]; Slack=[@brian]; Family=[Jennifer Michael, Billy Michael]; # of Family=[2]; Additional Info=[some random text]"
     */
    private String otherInfo;

    /**
     * Home Phone.
     */
    private String homePhone;

    /**
     * Cell Phone.
     */
    private String cellPhone;

    /**
     * Email.
     */
    private String email;

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
     * EAA Number.
     */
    private String eaaNumber;

    /**
     * Membership Expiration.
     */
    private Date expiration;

    public Long getRosterId() {
        return rosterId;
    }

    public void setRosterId(Long rosterId) {
        this.rosterId = rosterId;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEaaNumber() {
        return eaaNumber;
    }

    public void setEaaNumber(String eaaNumber) {
        this.eaaNumber = eaaNumber;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSpouse() {
        return spouse;
    }

    public void setSpouse(String spouse) {
        this.spouse = spouse;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public WebAdminAccess getWebAdminAccess() {
        return webAdminAccess;
    }

    public void setWebAdminAccess(WebAdminAccess webAdminAccess) {
        this.webAdminAccess = webAdminAccess;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getJoined() {
        return joined;
    }

    public void setJoined(String joined) {
        this.joined = joined;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getAircraftOwned() {
        return aircraftOwned;
    }

    public void setAircraftOwned(String aircraftOwned) {
        this.aircraftOwned = aircraftOwned;
    }

    public String getAircraftProject() {
        return aircraftProject;
    }

    public void setAircraftProject(String aircraftProject) {
        this.aircraftProject = aircraftProject;
    }

    public String getAircraftBuilt() {
        return aircraftBuilt;
    }

    public void setAircraftBuilt(String aircraftBuilt) {
        this.aircraftBuilt = aircraftBuilt;
    }

    public boolean isImcClub() {
        return imcClub;
    }

    public void setImcClub(boolean imcClub) {
        this.imcClub = imcClub;
    }

    public boolean isVmcClub() {
        return vmcClub;
    }

    public void setVmcClub(boolean vmcClub) {
        this.vmcClub = vmcClub;
    }

    public boolean isYePilot() {
        return yePilot;
    }

    public void setYePilot(boolean yePilot) {
        this.yePilot = yePilot;
    }

    public boolean isYeVolunteer() {
        return yeVolunteer;
    }

    public void setYeVolunteer(boolean yeVolunteer) {
        this.yeVolunteer = yeVolunteer;
    }

    public boolean isEaglePilot() {
        return eaglePilot;
    }

    public void setEaglePilot(boolean eaglePilot) {
        this.eaglePilot = eaglePilot;
    }

    public boolean isEagleVolunteer() {
        return eagleVolunteer;
    }

    public void setEagleVolunteer(boolean eagleVolunteer) {
        this.eagleVolunteer = eagleVolunteer;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int compareTo(final Member other) {
        if (equals(other)) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(email, member.email) &&
                Objects.equals(firstName, member.firstName) &&
                Objects.equals(lastName, member.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rosterId, rfid, firstName, lastName, eaaNumber, expiration);
    }

    @Override
    public String toString() {
        return "Member{" +
                "rosterId=" + rosterId +
                ", rfid='" + rfid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", nickname='" + nickname + '\'' +
                ", spouse='" + spouse + '\'' +
                ", gender=" + gender +
                ", memberType=" + memberType +
                ", status=" + status +
                ", webAdminAccess=" + webAdminAccess +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", city='" + city + '\'' +
                ", state=" + state +
                ", zipCode='" + zipCode + '\'' +
                ", country=" + country +
                ", birthDate='" + birthDate + '\'' +
                ", joined='" + joined + '\'' +
                ", otherInfo='" + otherInfo + '\'' +
                ", homePhone='" + homePhone + '\'' +
                ", cellPhone='" + cellPhone + '\'' +
                ", email='" + email + '\'' +
                ", ratings='" + ratings + '\'' +
                ", aircraftOwned='" + aircraftOwned + '\'' +
                ", aircraftProject='" + aircraftProject + '\'' +
                ", aircraftBuilt='" + aircraftBuilt + '\'' +
                ", imcClub=" + imcClub +
                ", vmcClub=" + vmcClub +
                ", yePilot=" + yePilot +
                ", yeVolunteer=" + yeVolunteer +
                ", eaglePilot=" + eaglePilot +
                ", eagleVolunteer=" + eagleVolunteer +
                ", eaaNumber='" + eaaNumber + '\'' +
                ", expiration=" + expiration +
                '}';
    }

}