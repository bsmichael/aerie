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

import io.cucumber.java.en.Given;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.eaa690.aerie.TestContext;
import org.eaa690.aerie.TestDataFactory;
import org.eaa690.aerie.model.Member;
import org.hamcrest.Matchers;

/**
 * Membership test steps.
 */
public class MembershipSteps extends BaseSteps {

    /**
     * Roster service.
     */
    private final String ROSTER = "roster/";

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public MembershipSteps(final TestContext testContext) {
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

    @Given("^I provide an email address$")
    public void iProvideAnEmailAddress() {
        throw new PendingException();
    }

    @Given("^I provide a cell phone number$")
    public void iProvideACellPhoneNumber() {
        throw new PendingException();
    }

    @Given("^I provide a Slack name$")
    public void iProvideASlackName() {
        throw new PendingException();
    }

    @Given("^I do not provide an email address$")
    public void iDoNotProvideAnEmailAddress() {
        throw new PendingException();
    }

    @Given("^I do not provide a cell phone number$")
    public void iDoNotProvideACellPhoneNumber() {
        throw new PendingException();
    }

    @Given("^I do not provide a Slack name$")
    public void iDoNotProvideASlackName() {
        throw new PendingException();
    }

    @Given("^I have enabled sending of messages by email$")
    public void iHaveEnabledSendingMessagesByEmail() {
        throw new PendingException();
    }

    @Given("^I have enabled sending of messages by SMS/Text$")
    public void iHaveEnabledSendingMessagesBySMS() {
        throw new PendingException();
    }

    @Given("^I have enabled sending of messages by Slack$")
    public void iHaveEnabledSendingMessagesBySlack() {
        throw new PendingException();
    }

    @Given("^I have disabled sending of messages by SMS/Text$")
    public void iHaveDisabledSendingMessagesBySMS() {
        throw new PendingException();
    }

    @Given("^I have disabled sending of messages by Slack$")
    public void iHaveDisabledSendingMessagesBySlack() {
        throw new PendingException();
    }

    @When("^I submit a new membership Jot Form$")
    public void iSubmitANewMembershipJotForm() {
        throw new PendingException();
    }

    @When("^I submit a renew membership Jot Form$")
    public void iSubmitARenewMembershipJotForm() {
        throw new PendingException();
    }

    @When("^I check my membership status$")
    public void iCheckMyMembershipStatus() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + testContext.getRosterId() + "/expiration")
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
                .post(ROSTER + "membership/renew/" + rosterId)
                .then());
    }

    @When("^I request member (.*) be added to the MailChimp member list$")
    public void iRequestMemberAddedToMailChimpMemberList(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "mailchimp/" + rosterId + "/add-member")
                .then());
    }

    @When("^I request non-member (.*) be added to the MailChimp non-member list$")
    public void iRequestNonMemberAddedToMailChimpNonMemberList(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "mailchimp/" + rosterId + "/add-non-member")
                .then());
    }

    @When("^I request membership renewal messages be sent$")
    public void iRequestMembershipRenewalMessageBeSent() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "roster/process-membership-renewals")
                .then());
    }

    @When("^I (.*)subscribe (.*) receiving (.*) messages$")
    public void iChangeMySMSSubscriptionStatus(final String action,
                                               final String ignored,
                                               final String messageType) {
        if ("un".equalsIgnoreCase(action)) {
            // Do unsubscribe action
        } else {
            // Do resubscribe action
        }
        throw new PendingException();
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

    @Then("^I should be listed in the members MailChimp audience$")
    public void iShouldBeListedInTheMembersMailChimpAudience() {
        throw new PendingException();
    }

    @Then("^my membership expiration should be set to (.*) from now$")
    public void iShouldBeListedInTheMembersMailChimpAudience(final String duration) {
        throw new PendingException();
    }

    @Then("^I should not receive a new member welcome email message$")
    public void iShouldNotReceiveANewMemberWelcomeEmailMessage() {
        throw new PendingException();
    }

    @Then("^I should receive a new member SMS/Text message$")
    public void iShouldReceiveANewMemberWelcomeSMSMessage() {
        throw new PendingException();
    }

    @Then("^I should not receive a new member Slack message$")
    public void iShouldNotReceiveANewMemberWelcomeSlackMessage() {
        throw new PendingException();
    }

    @Then("^I should receive a new member welcome email message$")
    public void iShouldReceiveANewMemberWelcomeEmailMessage() {
        throw new PendingException();
    }

    @Then("^I should not receive a new member SMS/Text message$")
    public void iShouldNotReceiveANewMemberWelcomeSMSMessage() {
        throw new PendingException();
    }

    @Then("^my membership expiration should be set to (.*) from my previous expiration date$")
    public void iShouldNotReceiveANewMemberWelcomeSMSMessage(final String duration) {
        throw new PendingException();
    }

    @Then("^I should receive a renew member welcome email message$")
    public void iShouldReceiveARenewMemberWelcomeEmailMessage() {
        throw new PendingException();
    }

    @Then("^I should receive a new member Slack message$")
    public void iShouldReceiveANewMemberWelcomeSlackMessage() {
        throw new PendingException();
    }

    @Then("^I should receive my membership details$")
    public void iShouldReceiveMyMembershipDetails() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.notNullValue())
                .body("expirationDate", Matchers.notNullValue())
                .body("rfid", Matchers.notNullValue());
    }

    @Then("^I have an (.*)Enabled status of (.*)$")
    public void iHaveMessageStatus(final String messageType, final String value) {
        throw new PendingException();
    }

    @Then("^I should see a message stating that I have been (.*)subscribed$")
    public void iReceiveSubscriptionStatusMessage(final String action) {
        if ("un".equalsIgnoreCase(action)) {
            // Verify unsubscribe message
        } else {
            // Verify resubscribe message
        }
        throw new PendingException();
    }

}
