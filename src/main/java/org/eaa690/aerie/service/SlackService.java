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

import com.ullink.slack.simpleslackapi.SlackSession;
import lombok.Getter;
import lombok.Setter;
import org.eaa690.aerie.config.SlackProperties;
import org.eaa690.aerie.model.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Slack Service.
 */
public class SlackService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackService.class);

    /**
     * SlackProperties.
     */
    @Autowired
    private SlackProperties slackProperties;

    /**
     * SlackSession.
     */
    @Autowired
    @Qualifier("membership")
    private SlackSession slackSession;

    /**
     * MessageRepository.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Sets SlackProperties.
     * Note: mostly used for unit test mocks
     *
     * @param value SlackProperties
     */
    @Autowired
    public void setSlackProperties(final SlackProperties value) {
        slackProperties = value;
    }

    /**
     * Sets SlackSession.
     * Note: mostly used for unit test mocks
     *
     * @param value SlackSession
     */
    @Autowired
    public void setSlackSession(final SlackSession value) {
        slackSession = value;
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

    /**
     * Slack Enabled.
     */
    @Getter
    @Setter
    private boolean enabled = false;

    /**
     * Gets all Slack users.
     *
     * @return list of users
     */
    public List<String> allSlackUsers() {
        final List<String> users = new ArrayList<>();
        slackSession
                .getUsers()
                .forEach(user -> users.add(user.getRealName() + "|" + user.getUserName()));
        return users;
    }

    /**
     * Sends a Slack message.
     *
     * @param to message recipient
     * @param body of the message
     */
    public void sendSlackMessage(final String to, final String body) {
        if (enabled) {
            LOGGER.info(String.format("Sending %s to %s", body, to));
            slackSession.sendMessageToUser(slackSession.findUserByUserName(to), body, null);
            messageRepository.save(new org.eaa690.aerie.model.Message(Instant.now(), to, body));
        }
    }

}
