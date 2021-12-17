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
import org.apache.http.HttpStatus;
import org.eaa690.aerie.TestContext;
import org.hamcrest.Matchers;

public class CommonSteps extends BaseSteps {

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public CommonSteps(final TestContext testContext) {
        super(testContext);
    }

    @Given("^I am an unauthenticated user$")
    public void unauthenticatedUser() {
        requestSpecification();
    }

    @Then("^The request should be successful$")
    public void requestSuccessful() {
        testContext.getValidatableResponse()
                .assertThat()
                .statusCode(Matchers.equalTo(HttpStatus.SC_OK));
    }

    @Then("^A (.*) exception should be thrown$")
    public void aExceptionShouldBeThrown(String exception) {
        if ("not found".equalsIgnoreCase(exception)) {
            testContext.getValidatableResponse()
                    .assertThat()
                    .statusCode(Matchers.equalTo(HttpStatus.SC_NOT_FOUND));
        }
        if ("bad request".equalsIgnoreCase(exception)) {
            testContext.getValidatableResponse()
                    .assertThat()
                    .statusCode(Matchers.equalTo(HttpStatus.SC_BAD_REQUEST));
        }
    }

}
