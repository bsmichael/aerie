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

import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.eaa690.aerie.TestContext;

/**
 * Roster test steps.
 */
public class ReportSteps extends BaseSteps {

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public ReportSteps(final TestContext testContext) {
        super(testContext);
    }

    @When("^I request a membership report$")
    public void iRequestAMembershipReport() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/reports/members")
                .then().log().all());
    }

    @When("^I request a membership expiring report$")
    public void iRequestAMembershipExpiringReport() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/reports/members/expiring")
                .then().log().all());
    }

    @When("^I request a membership expired report$")
    public void iRequestAMembershipExpiredReport() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/reports/members/expired")
                .then().log().all());
    }

    @When("^I request a new members report$")
    public void iRequestANewMembersReport() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( "/reports/members/new")
                .then().log().all());
    }

}
