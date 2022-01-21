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

package org.eaa690.aerie.service;

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.model.gs.Answer;
import org.eaa690.aerie.model.gs.AnswerRepository;
import org.eaa690.aerie.model.gs.Question;
import org.eaa690.aerie.model.gs.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Question Service.
 */
@Slf4j
public class QuestionService {

    /**
     * QuestionRepository.
     */
    @Autowired
    private QuestionRepository questionRepository;

    /**
     * AnswerRepository.
     */
    @Autowired
    private AnswerRepository answerRepository;

    /**
     * Sets QuestionRepository.
     * Note: mostly used for unit test mocks
     *
     * @param value QuestionRepository
     */
    @Autowired
    public void setQuestionRepository(final QuestionRepository value) {
        questionRepository = value;
    }

    /**
     * Sets AnswerRepository.
     * Note: mostly used for unit test mocks
     *
     * @param value AnswerRepository
     */
    @Autowired
    public void setAnswerRepository(final AnswerRepository value) {
        answerRepository = value;
    }

    /**
     * Constructor.
     */
    public QuestionService() {
        // Do nothing
    }

    /**
     * Gets all questions for the provided ACS ID.
     *
     * @param acsId ACS ID
     * @return list of questions
     */
    public List<Question> getQuestionsForACS(final Long acsId) {
        return questionRepository.findByAcsId(acsId).orElse(new ArrayList<>());
    }

    /**
     * Gets all questions for the provided course.
     *
     * @param course ACS ID
     * @return list of questions
     */
    public List<Question> getQuestionsForCourse(final String course) {
        return questionRepository.findByCourse(course).orElse(new ArrayList<>());
    }

    /**
     * Gets all questions.
     *
     * @return list of questions
     */
    public List<Question> getAllQuestions() {
        return questionRepository.findAll().orElse(new ArrayList<>());
    }

    /**
     * Gets all answers for the provided question ID.
     *
     * @param questionId Question ID
     * @return list of answers
     */
    public List<Answer> getAnswersForQuestion(final Long questionId) {
        return answerRepository.findByQuestionId(questionId).orElse(new ArrayList<>());
    }

    /**
     * Gets all answers.
     *
     * @return list of answers
     */
    public List<Answer> getAllAnswers() {
        return answerRepository.findAll().orElse(new ArrayList<>());
    }

}
