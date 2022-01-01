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
import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.config.SlackProperties;
import org.eaa690.aerie.model.Message;
import org.eaa690.aerie.model.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Slack Service.
 */
@Slf4j
public class SlackService {

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
     * @param message Message
     */
    public void sendSlackMessage(final Message message) {
        if (enabled) {
            log.info("Sending {} to {}", message.getBody(), message.getTo());
            slackSession.sendMessageToUser(slackSession.findUserByUserName(message.getTo()), message.getBody(), null);
            messageRepository.save(message.sent(Instant.now()));
        }
    }

}
