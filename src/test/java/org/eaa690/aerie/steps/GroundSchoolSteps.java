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
 * GroundSchool test steps.
 */
public class GroundSchoolSteps extends BaseSteps {

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public GroundSchoolSteps(final TestContext testContext) {
        super(testContext);
    }

    @Given("^the question database is up-to-date$")
    public void theQuestionDatabaseIsUpToDate() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post( "gs/update")
                .then());
    }

    @When("^I request question (.*) in the (.*) course$")
    public void iRequestQuestionInCourse(final Long remoteQuestionId, final String course) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .param("qid", remoteQuestionId)
                .param("course", course)
                .get( "/gs/questions")
                .then().log().all());
    }

    @When("^I request the answers for question (.*) in the (.*) course$")
    public void iRequestAnswerForQuestionInCourse(final Long remoteQuestionId, final String course) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .param("qid", remoteQuestionId)
                .param("course", course)
                .get( "/gs/answers")
                .then().log().all());
    }

}
