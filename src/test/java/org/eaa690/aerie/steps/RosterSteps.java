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
                .then().log().all());
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
                .then().log().all());
    }

    @When("^I request all Slack users$")
    public void iRequestAllSlackUsers() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(SLACK)
                .then());
    }

    @When("^I request an update of the roster data$")
    public void iRequestUpdateRosterData() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "update")
                .then().log().all());
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
                .then().log().all());
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
                .then().log().all());
    }

    @When("^I request the expiration data for member with ID (.*)$")
    public void iRequestExpirationData(final String memberId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + memberId + "/expiration")
                .then().log().all());
    }

    @When("^I request RFID data for all members$")
    public void iRequestAllRFIDData() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER)
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
                .then().log().all());
    }

    @When("^I request my membership info via Slack$")
    public void iRequestMyMembershipInfoViaSlack() {
        String username = "test";
        String userid = "unknown";
        if (testContext.getMembershipStatus() != null) {
            if (testContext.getMembershipStatus() == Boolean.TRUE) {
                username = "brian";
                userid = "U0LFDK199";
            } else {
                username = "dopferman";
                userid = "UK0URM9JA";
            }
        }
        final String message = "token=token\n" +
                "&team_id=T0001\n" +
                "&team_domain=example\n" +
                "&enterprise_id=E0001\n" +
                "&enterprise_name=Globular%20Construct%20Inc\n" +
                "&channel_id=C2147483705\n" +
                "&channel_name=test\n" +
                "&user_id=" + userid + "\n" +
                "&user_name=" + username + "\n" +
                "&command=/gatecode\n" +
                "&text=94070\n" +
                "&response_url=https://hooks.slack.com/commands/1234/5678\n" +
                "&trigger_id=13345224609.738474920.8088930838d88f008e0\n" +
                "&api_app_id=A123456";
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .body(message)
                .post( "/roster/slack")
                .then().log().all());
    }

    @When("^I request an email be sent to new member (.*)$")
    public void iRequestEmailToNewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + rosterId + "/new-membership")
                .then().log().all());
    }

    @When("^I request a message be sent to member (.*) to renew their membership$")
    public void iRequestAMessageToRenewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + rosterId + "/renew")
                .then().log().all());
    }

    @When("^I find by name members with (.*) and (.*)$")
    public void iFindByNameMembers(final String firstName, final String lastName) {
        final StringBuilder sb = buildFirstAndLastNameQueryParamString(firstName, lastName);
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + "find-by-name" + sb)
                .then().log().all());
    }

    @When("^I search for members with (.*) and (.*)$")
    public void iSearchForMembers(final String firstName, final String lastName) {
        final StringBuilder sb = buildFirstAndLastNameQueryParamString(firstName, lastName);
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get("members/search" + sb)
                .then().log().all());
    }

    @When("^I request membership renewal messages be sent$")
    public void iRequestMembershipRenewalMessageBeSent() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "-1/renew")
                .then().log().all());
    }

    @When("^Aerie checks for JotForm submissions$")
    public void aerieChecksForJotFormSubmissions() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "jotform")
                .then().log().all());
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

    @Then("^I should receive a list of membership details with (.*)$")
    public void iShouldReceiveAListOfMembershipDetailsWith(final String name) {
        if (name != null && !"null".equalsIgnoreCase(name)) {
            testContext.getValidatableResponse()
                    .assertThat()
                    .body("name", Matchers.hasItem(name));
        }
    }

    @Then("^The response should have Slack users listed$")
    public void theResponseShouldHaveSlackUsersListed() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("size()", Matchers.greaterThan(1));
    }

    @Then("^I should receive a membership info message$")
    public void iShouldReceiveAMembershipInfoMessage() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("text", Matchers.containsString("Your membership is set to expire on"));
    }

    /**
     * Builds a query param string with first and last name values.
     *
     * @param firstName First name
     * @param lastName Last name
     * @return query param string
     */
    private StringBuilder buildFirstAndLastNameQueryParamString(String firstName, String lastName) {
        final StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.equalsIgnoreCase("null")
                || lastName != null && !lastName.equalsIgnoreCase("null")) {
            sb.append("?");
        }
        if (firstName != null && !firstName.equalsIgnoreCase("null")) {
            sb.append("firstName=").append(firstName);
        }
        if (firstName != null && !firstName.equalsIgnoreCase("null")
                && lastName != null && !lastName.equalsIgnoreCase("null")) {
            sb.append("&");
        }
        if (lastName != null && !lastName.equalsIgnoreCase("null")) {
            sb.append("lastName=").append(lastName);
        }
        return sb;
    }

}
