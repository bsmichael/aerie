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

package org.eaa690.aerie.controller;

import org.eaa690.aerie.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EmailController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/email"
})
public class EmailController {

    /**
     * EmailService.
     */
    private EmailService emailService;

    /**
     * Sets EmailService.
     *
     * @param value EmailService
     */
    @Autowired
    public void setEmailService(final EmailService value) {
        emailService = value;
    }

    /**
     * Constructor.
     *
     * @param eService EmailService
     */
    public EmailController(final EmailService eService) {
        emailService = eService;
    }

    /**
     * Sets whether or not the email service is enabled.
     *
     * @param flag enabled
     */
    @PostMapping(path = {
            "/enabled/{flag}"
    })
    public void update(@PathVariable("flag") final String flag) {
        emailService.setEnabled(Boolean.parseBoolean(flag));
    }

}
