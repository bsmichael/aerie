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

package org.eaa690.aerie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eaa690.aerie.config.CommonConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

/**
 * Message.
 */
@Entity
@Table(name = "MESSAGE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message extends BaseEntity {

    /**
     * Time message was sent.
     */
    private Instant sent;

    /**
     * Recipient of the message.
     */
    private String to;

    /**
     * Subject of the message.
     */
    private String subject;

    /**
     * Body of the message.
     */
    @Column(length = CommonConstants.FOUR_THOUSAND)
    private String body;

    /**
     * Adds sent value.
     *
     * @param value sent
     * @return Message
     */
    public Message sent(final Instant value) {
        sent = value;
        return this;
    }

    /**
     * Adds to value.
     *
     * @param value to
     * @return Message
     */
    public Message to(final String value) {
        to = value;
        return this;
    }

    /**
     * Adds subject value.
     *
     * @param value subject
     * @return Message
     */
    public Message subject(final String value) {
        subject = value;
        return this;
    }

    /**
     * Adds body value.
     *
     * @param value body
     * @return Message
     */
    public Message body(final String value) {
        body = value;
        return this;
    }
}
