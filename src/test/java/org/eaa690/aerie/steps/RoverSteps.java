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
import org.eaa690.aerie.model.rover.Team;
import org.hamcrest.Matchers;

import java.io.File;
import java.util.Date;

/**
 * Rover test steps.
 */
public class RoverSteps extends BaseSteps {

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public RoverSteps(final TestContext testContext) {
        super(testContext);
    }

    @Given("^A rover team exists$")
    public void aRoverTeamExists() {
        final Team team = new Team();
        team.setName("JPL");
        team.setRoverIPAddress("127.0.0.1");
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(team)
                .post( "/rover/teams")
                .then().log().all());
        testContext.setRoverTeamId(testContext.getValidatableResponse().extract().as(Team.class).getId());
    }

    @When("^I add a new rover team$")
    public void iAddANewRoverTeam() {
        final Team team = new Team();
        team.setName("JPL");
        team.setRoverIPAddress("127.0.0.1");
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(team)
                .post( "/rover/teams")
                .then().log().all());
    }

    @When("^I request the rover dashboard$")
    public void iRequestTheRoverDashboard() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/rover/dashboard")
                .then().log().all());
    }

    @When("^I request all rover teams$")
    public void iRequestAllRoverTeams() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/rover/teams")
                .then().log().all());
    }

    @When("^I request a rover team$")
    public void iRequestARoverTeam() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/rover/" + testContext.getRoverTeamId())
                .then().log().all());
    }

    @When("^I request a rover team's latest image$")
    public void iRequestARoverTeamLatestImage() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/rover/" + testContext.getRoverTeamId() + "/latest")
                .then().log().all());
    }

    @When("^I post a new image for a rover team$")
    public void iPostANewImageForARoverTeam() {
        File image = new File("test_image.png");
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .basePath("/rover/" + testContext.getRoverTeamId())
                .multiPart("image", image, "application/octet-stream")
                .post()
                .then().log().all());
    }

    @Then("^I should receive a rover team id$")
    public void iShouldReceiveARoverTeamId() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("id", Matchers.any(Long.class));
    }

}
