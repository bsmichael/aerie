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

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GroundSchoolController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/gs"
})
@Slf4j
public class GroundSchoolController {

    /**
     * QuestionService.
     */
    private QuestionService questionService;

    /**
     * Sets QuestionService.
     *
     * @param value QuestionService
     */
    @Autowired
    public void setQuestionService(final QuestionService value) {
        questionService = value;
    }

    /**
     * Constructor.
     *
     * @param qService QuestionService
     */
    public GroundSchoolController(final QuestionService qService) {
        questionService = qService;
    }

    /**
     * Updates questions and answers.
     */
    @PostMapping(path = {
            "/update"
    })
    public void update() {
        questionService.update();
    }

}
