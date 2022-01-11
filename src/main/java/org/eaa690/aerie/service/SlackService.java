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
import org.eaa690.aerie.model.SlackCommand;
import org.eaa690.aerie.model.SlackRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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
     * Gets SlackID for provided user name.
     *
     * @param userName user name
     * @return id
     */
    public String getUserIDForUsername(final String userName) {
        if (userName == null || "".equals(userName)) {
            return null;
        }
        return allSlackUsers()
                .stream()
                .filter(record -> userName.equalsIgnoreCase(record.getUser()))
                .map(SlackRecord::getId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all Slack users.
     *
     * @return list of users
     */
    public List<SlackRecord> allSlackUsers() {
        return slackSession
                .getUsers()
                .stream()
                .map(user -> new SlackRecord(user.getId(), user.getRealName(), user.getUserName()))
                .collect(Collectors.toList());
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

    /**
     * Parses received text into a SlackCommand object.
     *
     * @param message to be parsed
     * @return SlackCommand
     */
    public SlackCommand getSlackCommand(final String message) {
        final String[] parts = message.split("&");
        final SlackCommand slackCommand = new SlackCommand();
        for (final String part : parts) {
            final String[] keyValuePair = part.split("=");
            final String key = keyValuePair[0].trim();
            String value = null;
            if (keyValuePair.length == 2) {
                value = keyValuePair[1].trim();
            }
            switch (key) {
                case "token":
                    slackCommand.setToken(value);
                    break;
                case "team_id":
                    slackCommand.setTeamId(value);
                    break;
                case "team_domain":
                    slackCommand.setTeamDomain(value);
                    break;
                case "enterprise_id":
                    slackCommand.setEnterpriseId(value);
                    break;
                case "enterprise_name":
                    slackCommand.setEnterpriseName(value);
                    break;
                case "channel_id":
                    slackCommand.setChannelId(value);
                    break;
                case "channel_name":
                    slackCommand.setChannelName(value);
                    break;
                case "user_id":
                    slackCommand.setUserId(value);
                    break;
                case "user_name":
                    slackCommand.setUser(value);
                    break;
                case "command":
                    slackCommand.setCommand(value);
                    break;
                case "text":
                    slackCommand.setText(value);
                    break;
                case "response_url":
                    slackCommand.setResponseUrl(value);
                    break;
                case "trigger_id":
                    slackCommand.setTriggerId(value);
                    break;
                case "api_app_id":
                    slackCommand.setApiAppId(value);
                    break;
                default:
            }
        }
        return slackCommand;
    }

}
