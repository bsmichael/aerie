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
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.eaa690.aerie.TestContext;
import org.eaa690.aerie.model.Member;
import org.hamcrest.Matchers;

/**
 * Subscription test steps.
 */
public class SubscriptionSteps extends BaseSteps {

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public SubscriptionSteps(final TestContext testContext) {
        super(testContext);
    }

    @Given("^I click on the unsubscribe link$")
    public void iClickOnTheUnsubscribeLink() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get("/unsubscribe/" + testContext.getRosterId() + "/email")
                .then().log().all());
    }

    @Given("^I want to subscribe to email messages$")
    public void iWantToSubscribeToEmailMessages() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get("/subscribe/email")
                .then().log().all());
    }

    @Given("^I want to resubscribe to email messages$")
    public void iWantToResubscribeToEmailMessages() {
        final Member member = testContext.getMember();
        member.setRosterId(42648L);
        testContext.setMember(member);
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get("/subscribe/email")
                .then().log().all());
    }

    @When("^I confirm I want to unsubscribe from email messages$")
    public void iConfirmIWantToUnsubscribeFromEmails() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post( "/unsubscribe/" + testContext.getRosterId() + "/email/confirm")
                .then().log().all());
    }

    @When("^I confirm I want to subscribe to email messages$")
    public void iConfirmIWantToSubscribeToEmails() {
        final Member member = testContext.getMember();
        member.setRosterId(42648L);
        testContext.setMember(member);
        testContext.setRosterId("42648");
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(member)
                .post( "/subscribe/email/confirm")
                .then().log().all());
    }

    @Then("^I have email (.*)$")
    public void iHaveEmailFlag(final String enabledFlag) {
        final Boolean enabled = "enabled".equalsIgnoreCase(enabledFlag) ? Boolean.TRUE : Boolean.FALSE;
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get("/roster/" + testContext.getRosterId())
                .then().log().all());
        testContext.getValidatableResponse()
                .assertThat()
                .statusCode(Matchers.equalTo(HttpStatus.SC_OK))
                .assertThat()
                .body("emailEnabled", Matchers.equalTo(enabled));
    }
}
