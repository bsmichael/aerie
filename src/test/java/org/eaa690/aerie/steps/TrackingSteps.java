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
import org.hamcrest.Matchers;

/**
 * Tracking test steps.
 */
public class TrackingSteps extends BaseSteps {

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public TrackingSteps(final TestContext testContext) {
        super(testContext);
    }

    @Given("^I have received an email message$")
    public void iHaveReceivedATrackedMessage() {
        testContext.setMessageId(Long.parseLong(faker.numerify("#####")));
    }

    @When("^I open a tracked message$")
    public void iOpenATrackedMessage() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/tracking/record/" + testContext.getRosterId() + "/" + testContext.getMessageId())
                .then().log().all());
    }

    @When("^I request all tracking data$")
    public void iRequestAllTrackingData() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/tracking/events")
                .then().log().all());
    }

    @Then("^The response should have tracking data listed$")
    public void theResponseShouldHaveSlackUsersListed() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("size()", Matchers.greaterThan(0));
    }

    @Then("^The message open event is recorded$")
    public void theMessageOpenEventIsRecorded() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get("/tracking/" + testContext.getRosterId() + "/events")
                .then().log().all());
        testContext.getValidatableResponse()
                .assertThat()
                .statusCode(Matchers.equalTo(HttpStatus.SC_OK))
                .assertThat()
                .body("size()", Matchers.greaterThan(0));
    }
}
