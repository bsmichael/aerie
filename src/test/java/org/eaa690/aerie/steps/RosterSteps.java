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

package org.eaa690.aerie.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.eaa690.aerie.TestContext;
import org.eaa690.aerie.TestDataFactory;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.RFIDRequest;
import org.hamcrest.Matchers;

/**
 * Roster test steps.
 */
public class RosterSteps extends BaseSteps {

    /**
     * Roster service.
     */
    private final String ROSTER = "roster/";

    /**
     * Email service.
     */
    private final String EMAIL = "email/";

    /**
     * Slack service.
     */
    private final String SLACK = "slack/";

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public RosterSteps(final TestContext testContext) {
        super(testContext);
    }

    @Given("^I am not a chapter member$")
    public void iAmNotAChapterMember() {
        testContext.setRosterId(null);
    }

    @Given("^I am a new chapter member$")
    public void iAmANewMember() {
        final Member member = TestDataFactory.getMember();
        throw new PendingException();
    }

    @Given("^I am a chapter member$")
    public void iAmAnExistingMember() {
        testContext.setRosterId("42648");
    }

    @Given("^I have a record in the roster management system$")
    public void iHaveARecord() {
        throw new PendingException();
    }

    @Given("^I do not have a record in the roster management system$")
    public void iDoNotHaveARecord() {
        throw new PendingException();
    }

    @Given("^email is (.*)$")
    public void emailEnabled(final String flag) {
        String enabled = "true";
        if ("disabled".equalsIgnoreCase(flag)) {
            enabled = "false";
        }
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(EMAIL + "enabled/" + enabled)
                .then());
    }

    @Given("^slack is (.*)$")
    public void slackEnabled(final String flag) {
        String enabled = "true";
        if ("disabled".equalsIgnoreCase(flag)) {
            enabled = "false";
        }
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(SLACK + "enabled/" + enabled)
                .then());
    }

    @When("^I request a membership report$")
    public void iRequestAMembershipReport() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + "membershipreport")
                .then());
    }

    @When("^I request an update of the roster data$")
    public void iRequestUpdateRosterData() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "update")
                .then());
    }

    @When("^I update member (.*)'s RFID with (.*)$")
    public void iUpdateMemberRFID(final String memberId, final String rfid) {
        final RFIDRequest rfidRequest = new RFIDRequest();
        rfidRequest.setRfid(rfid);
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(rfidRequest)
                .put(ROSTER + memberId + "/rfid")
                .then());
    }

    @When("^I find a member by their RFID (.*)$")
    public void iFindMemberByRFID(final String rfid) {
        final RFIDRequest rfidRequest = new RFIDRequest();
        rfidRequest.setRfid(rfid);
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(rfidRequest)
                .post(ROSTER + "find-by-rfid")
                .then());
    }

    @When("^I request the expiration data for member with ID (.*)$")
    public void iRequestExpirationData(final String memberId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + memberId + "/expiration")
                .then());
    }

    @When("^I request RFID data for all members$")
    public void iRequestAllRFIDData() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + "all-rfid")
                .then());
    }

    @When("^I submit a new membership Jot Form$")
    public void iSubmitANewMembershipJotForm() {
        throw new PendingException();
    }

    @When("^I submit a renew membership Jot Form$")
    public void iSubmitARenewMembershipJotForm() {
        throw new PendingException();
    }

    @When("^I check membership information for (.*)")
    public void iCheckMyMembershipInformation(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + rosterId)
                .then());
    }

    @When("^I request an email be sent to new member (.*)$")
    public void iRequestEmailToNewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + rosterId + "/new-membership")
                .then());
    }

    @When("^I request a message be sent to member (.*) to renew their membership$")
    public void iRequestAMessageToRenewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + rosterId + "/renew")
                .then());
    }

    @When("^I request membership renewal messages be sent$")
    public void iRequestMembershipRenewalMessageBeSent() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "-1/renew")
                .then());
    }

    @When("^Aerie checks for JotForm submissions$")
    public void aerieChecksForJotFormSubmissions() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "jotform")
                .then());
    }

    @Then("^I should have a record in the roster management system$")
    public void iShouldHaveARecordInTheRosterManagementSystem() {
        throw new PendingException();
    }

    @Then("^my membership expiration should be set to (.*) from now$")
    public void myMembershipExpirationShouldBeSetToFromNow(final String duration) {
        throw new PendingException();
    }

    @Then("^I should not receive a new member welcome email message$")
    public void iShouldNotReceiveANewMemberWelcomeEmailMessage() {
        throw new PendingException();
    }

    @Then("^I should receive a new member welcome email message$")
    public void iShouldReceiveANewMemberWelcomeEmailMessage() {
        throw new PendingException();
    }

    @Then("^my membership expiration should be set to (.*) from my previous expiration date$")
    public void myMembershipExpirationShouldBeSetToFromPreviousExpiration(final String duration) {
        throw new PendingException();
    }

    @Then("^I should receive a renew member welcome email message$")
    public void iShouldReceiveARenewMemberWelcomeEmailMessage() {
        throw new PendingException();
    }

    @Then("^I should receive my membership details$")
    public void iShouldReceiveMyMembershipDetails() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("id", Matchers.notNullValue())
                .body("firstName", Matchers.notNullValue())
                .body("expiration", Matchers.notNullValue())
                .body("rfid", Matchers.notNullValue());
    }

}
