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
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.eaa690.aerie.config.CommonConstants;
import org.eaa690.aerie.config.GroundSchoolProperties;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.gs.Answer;
import org.eaa690.aerie.model.gs.AnswerRepository;
import org.eaa690.aerie.model.gs.Question;
import org.eaa690.aerie.model.gs.QuestionRepository;
import org.eaa690.aerie.ssl.GSDecryptor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Question Service.
 */
@Slf4j
public class QuestionService {

    /**
     * GroundSchoolProperties.
     */
    @Autowired
    private GroundSchoolProperties groundSchoolProperties;

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
     * GSDecryptor.
     */
    @Autowired
    private GSDecryptor gsDecryptor;

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
     * Sets GroundSchoolProperties.
     * Note: mostly used for unit test mocks
     *
     * @param value GroundSchoolProperties
     */
    @Autowired
    public void setGroundSchoolProperties(final GroundSchoolProperties value) {
        groundSchoolProperties = value;
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
     * Sets GSDecryptor.
     * Note: mostly used for unit test mocks
     *
     * @param value GSDecryptor
     */
    @Autowired
    public void setGSDecryptor(final GSDecryptor value) {
        gsDecryptor = value;
    }

    /**
     * Question ID <-> ACS ID.
     */
    private final Map<Long, Long> questionAcs = new HashMap<>();

    /**
     * Constructor.
     *
     * @param properties GroundSchoolProperties
     */
    public QuestionService(final GroundSchoolProperties properties) {
        groundSchoolProperties = properties;
    }

    /**
     * Gets all questions for the provided course.
     *
     * @param course ACS ID
     * @return list of questions
     * @throws NoSuchElementException if no questions are found
     */
    public List<Question> getQuestionsForCourse(final String course) throws NoSuchElementException {
        return questionRepository.findByCourse(course).orElseThrow();
    }

    /**
     * Gets all answers for the provided question ID.
     *
     * @param questionId Question ID
     * @param course Course
     * @return list of answers
     */
    public List<Answer> getAnswersForQuestion(final Long questionId, final String course)
            throws ResourceNotFoundException {
        final Optional<List<Answer>> answersOpt = answerRepository.findByQuestionIdAndCourse(questionId, course);
        if (answersOpt.isPresent()) {
            final List<Answer> answers = answersOpt.get();
            if (!answers.isEmpty()) {
                return answers;
            }
        }
        throw new ResourceNotFoundException();
    }

    /**
     * Updates questions and answers.
     */
    @PostConstruct
    public void update() {
        final String[] courses = "PVT,IFR,COM,CFI,ATP,FLE,AMG,AMA,AMP,PAR,SPG,SPI,MIL,IOF,MCI,RDP".split(",");
        for (String course : courses) {
            String jdbcUrl = "jdbc:sqlite:" + groundSchoolProperties.getDbLocation() + "/" + course + ".db";
            Connection conn = null;
            try {
                log.info("Updating questions and answers for course: {}", course);
                DriverManager.registerDriver(new org.sqlite.JDBC());
                conn = DriverManager.getConnection(jdbcUrl);
                buildQuestionsACSMap(conn);
                getQuestions(conn, course);
                getAnswers(conn, course);
                log.info("Completed updating questions and answers for course: {}", course);
            } catch (SQLException | InvalidCipherTextException sqle) {
                log.error("Error: " + sqle.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException sqle) {
                        log.warn("Unable to close database connection: " + sqle.getMessage());
                    }
                }
            }
            questionAcs.clear();
        }
    }

    /**
     * Gets questions from remote database.
     *
     * @param conn Remote database connection
     * @param course Ground School course
     * @throws SQLException when a query fails
     */
    private void getQuestions(final Connection conn, final String course)
            throws SQLException, InvalidCipherTextException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT QuestionID, QuestionText, ChapterID, "
                + "SMCID, SourceID, LastMod, Explanation, OldQID FROM Questions");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final Long remoteId = rs.getLong(1);
                final Question question = questionRepository
                        .findByRemoteIdAndCourse(remoteId, course)
                        .orElse(new Question());
                question.setCourse(course);
                question.setRemoteId(remoteId);
                question.setText(gsDecryptor.decrypt(rs.getString(2)));
                question.setChapterId(rs.getLong(CommonConstants.THREE));
                question.setSmcId(rs.getLong(CommonConstants.FOUR));
                question.setSource(rs.getString(CommonConstants.FIVE));
                question.setLastModified(rs.getDate(CommonConstants.SIX));
                question.setExplanation(gsDecryptor.decrypt(rs.getString(CommonConstants.SEVEN)));
                question.setOldQuestionId(rs.getLong(CommonConstants.EIGHT));
                if (questionAcs.containsKey(question.getRemoteId())) {
                    question.setAcsId(questionAcs.get(question.getRemoteId()));
                }
                questionRepository.save(question);
            }
        }
    }

    /**
     * Gets answers from remote database.
     *
     * @param conn Remote database connection
     * @param course Course
     * @throws SQLException when a query fails
     */
    private void getAnswers(final Connection conn, final String course)
            throws SQLException, InvalidCipherTextException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT AnswerID, AnswerText, QuestionID, IsCorrect, LastMod FROM Answers");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final Long remoteId = rs.getLong(1);
                final Answer answer = answerRepository.findByRemoteIdAndCourse(remoteId, course).orElse(new Answer());
                answer.setCourse(course);
                answer.setRemoteId(remoteId);
                answer.setText(gsDecryptor.decrypt(rs.getString(2)));
                answer.setQuestionId(rs.getLong(CommonConstants.THREE));
                answer.setCorrect(rs.getBoolean(CommonConstants.FOUR));
                answer.setLastModified(rs.getDate(CommonConstants.FIVE));
                answerRepository.save(answer);
            }
        }
    }

    /**
     * Builds QuestionACS map.
     *
     * @param conn Remote database connection
     * @throws SQLException when a query fails
     */
    private void buildQuestionsACSMap(final Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT QuestionID, ACSID FROM QuestionsACS");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                questionAcs.put(rs.getLong(1), rs.getLong(2));
            }
        }
    }

}
