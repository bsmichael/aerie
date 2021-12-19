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
     * Type of message.
     */
    private String type;

    /**
     * Description of the message (subject or body).
     */
    private String description;

}
