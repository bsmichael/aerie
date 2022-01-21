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

package org.eaa690.aerie.model.gs;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * QuestionRepository.
 */
public interface QuestionRepository extends Repository<Question, Long> {

    /**
     * Gets a question by remote ID.
     *
     * @param id remote question ID
     * @param course course
     * @return Question
     */
    Optional<Question> findByRemoteIdAndCourse(Long id, String course);

    /**
     * Gets a question by course.
     *
     * @param course Course
     * @return Question
     */
    Optional<List<Question>> findByCourse(String course);

    /**
     * Saves a question.
     *
     * @param question Question
     * @return Question
     */
    Question save(Question question);

}
