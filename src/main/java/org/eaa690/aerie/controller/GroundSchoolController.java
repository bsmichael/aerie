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
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.gs.Answer;
import org.eaa690.aerie.model.gs.Question;
import org.eaa690.aerie.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

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

    /**
     * Finds questions.
     *
     * @param remoteQuestionId remote question ID
     * @param course Course
     * @return Question
     * @throws ResourceNotFoundException when questions are not found
     */
    @GetMapping(path = {
            "/questions"
    })
    public Question findQuestions(@RequestParam(name = "qid", required = false) final Long remoteQuestionId,
                                  @RequestParam(name = "course", required = false) final String course)
            throws ResourceNotFoundException {
        try {
            return questionService
                    .getQuestionsForCourse(course)
                    .stream()
                    .filter(q -> Objects.equals(q.getRemoteId(), remoteQuestionId))
                    .findFirst()
                    .orElseThrow();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    /**
     * Finds answers.
     *
     * @param remoteQuestionId remote question ID
     * @param course Course
     * @return list of answers
     * @throws ResourceNotFoundException when answers are not found
     */
    @GetMapping(path = {
            "/answers"
    })
    public List<Answer> findAnswers(@RequestParam(name = "qid", required = false) final Long remoteQuestionId,
                                    @RequestParam(name = "course", required = false) final String course)
            throws ResourceNotFoundException {
        try {
            log.info("Finding answers for qid: {} and course: {}", remoteQuestionId, course);
            return questionService.getAnswersForQuestion(remoteQuestionId, course);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

}
