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
import org.eaa690.aerie.config.NotamProperties;
import org.eaa690.aerie.model.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Notam Service.
 */
@Slf4j
public class NotamService {

    /**
     * NotamProperties.
     */
    @Autowired
    private NotamProperties notamProperties;

    /**
     * MessageRepository.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Sets NotamProperties.
     * Note: mostly used for unit test mocks
     *
     * @param value NotamProperties
     */
    @Autowired
    public void setNotamProperties(final NotamProperties value) {
        notamProperties = value;
    }

    /**
     * Sets MessageRepository.
     * Note: mostly used for unit test mocks
     *
     * @param value MessageRepository
     */
    @Autowired
    public void setMessageRepository(final MessageRepository value) {
        messageRepository = value;
    }

}
