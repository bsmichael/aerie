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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eaa690.aerie.config.CommonConstants;
import org.eaa690.aerie.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Answer.
 */
@Entity
@Table(name = "ANSWERS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Answer extends BaseEntity implements Comparable<Answer> {

    /**
     * Remote Answer ID.
     */
    private Long remoteId;

    /**
     * Answer Text.
     */
    private String text;

    /**
     * Question ID.
     */
    private Long questionId;

    /**
     * Is Correct.
     */
    private Boolean correct;

    /**
     * Last Modified.
     */
    private Date lastModified;

    /**
     * Compares Answer records.
     *
     * @param o other Answer
     * @return comparison
     */
    @Override
    public int compareTo(final Answer o) {
        if (o == null || getId() == null || getId() < o.getId()) {
            return -1;
        } else if (getId().equals(o.getId())) {
            return 0;
        }
        return 1;
    }

    /**
     * Required implementation.
     *
     * @param o other Object
     * @return comparison
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Answer question = (Answer) o;
        if (compareTo(question) == 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Required implementation.
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        int result = 0;
        if (getText() != null) {
            result = getText().hashCode();
        }
        return CommonConstants.THIRTY_ONE * result;
    }
}
