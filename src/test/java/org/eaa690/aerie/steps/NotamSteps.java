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
import org.eaa690.aerie.model.Message;
import org.eaa690.aerie.model.RFIDRequest;
import org.hamcrest.Matchers;

import java.time.Instant;

/**
 * Roster test steps.
 */
public class NotamSteps extends BaseSteps {

    /**
     * Messages service.
     */
    private final String MESSAGES = "messages/";

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public NotamSteps(final TestContext testContext) {
        super(testContext);
    }

    @When("^I get an edit post$")
    public void iGetAnEditPost() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(MESSAGES + "addEditPost")
                .then());
    }

    @When("^I add an edit post$")
    public void iAddAnEditPost() {
        final Message message = new Message();
        message.setBody("<HTML><BODY></BODY></HTML>");
        message.setSubject("Testing");
        message.setTo("someone");
        message.setSent(Instant.now());
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(message)
                .post(MESSAGES + "addEditPost")
                .then());
    }

}
