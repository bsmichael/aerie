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
import org.eaa690.aerie.TestContext;
import org.eaa690.aerie.model.GateCode;
import org.hamcrest.Matchers;

import java.util.Date;

/**
 * GateCode test steps.
 */
public class GateCodeSteps extends BaseSteps {

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public GateCodeSteps(final TestContext testContext) {
        super(testContext);
    }

    @Given("^the roster database is up-to-date$")
    public void theRosterDatabaseIsUpToDate() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post( "roster/update")
                .then());
    }

    @Given("^a gate code has been set$")
    public void aGateCodeHasBeenSet() {
        final GateCode gateCode = new GateCode();
        gateCode.setDate(new Date());
        gateCode.setCodeAt690AirportRd("1234");
        gateCode.setCodeAt770AirportRd("1234");
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(gateCode)
                .post( "/gatecodes/add")
                .then().log().all());
    }

    @When("^I add a new gate code$")
    public void iAddANewGateCode() {
        final GateCode gateCode = new GateCode();
        gateCode.setDate(new Date());
        gateCode.setCodeAt690AirportRd("1234");
        gateCode.setCodeAt770AirportRd("1234");
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(gateCode)
                .post( "/gatecodes/add")
                .then().log().all());
    }

    @When("^I request the gate code via Slack$")
    public void iRequestTheGateCodeViaSlack() {
        String username = "test";
        if (testContext.getMembershipStatus() != null) {
            if (testContext.getMembershipStatus() == Boolean.TRUE) {
                username = "brian";
            } else {
                username = "jerryfischer";
            }
        }
        final String message = "token=gIkuvaNzQIHg97ATvDxqgjtO\n" +
                "&team_id=T0001\n" +
                "&team_domain=example\n" +
                "&enterprise_id=E0001\n" +
                "&enterprise_name=Globular%20Construct%20Inc\n" +
                "&channel_id=C2147483705\n" +
                "&channel_name=test\n" +
                "&user_id=U2147483697\n" +
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
                .post( "/gatecodes/slack")
                .then().log().all());
    }

    @When("^I request all gate codes$")
    public void iRequestAllGateCodes() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/gatecodes")
                .then().log().all());
    }

    @Then("^I should receive a gate code$")
    public void iShouldReceiveAGateCode() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("text", Matchers.containsString("1234"));
    }

    @Then("^I should receive a message with a link to renew my membership$")
    public void iShouldReceiveAMessageWithALinkToRenewMyMembership() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("text", Matchers.containsString("Please renew your membership"));
    }

    @Then("^I should receive a message with a link to become a member$")
    public void iShouldReceiveAMessageWithALinkToBecomeAMember() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("text", Matchers.containsString("Please become a chapter member"));
    }

}
