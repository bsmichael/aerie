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

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlackService implements SlackMessagePostedListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackService.class);

    /**
     * SlackSession.
     */
    @Autowired
    private SlackSession slackSession;

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * Sets SlackSession. Note: mostly used for unit test mocks
     *
     * @param value SlackSession
     */
    @Autowired
    public void setSlackSession(final SlackSession value) {
        slackSession = value;
    }

    /**
     * Processes messages received by membership slack bot.
     *
     * @param event   SlackMessagePosted
     * @param session SlackSession
     */
    @Override
    public void onEvent(final SlackMessagePosted event, final SlackSession session) {
        // Ignore bot user messages
        if (session.sessionPersona().getId().equals(event.getSender().getId())) {
            return;
        }
        final String message = event.getMessageContent();
        final String user = event.getUser().getUserName();
        final String msg = String.format("Slack message received: user [%s]; message [%s]", user, message);
        LOGGER.info(msg);
    }

    /**
     * Gets all Slack users.
     *
     * @return list of users
     */
    public List<String> allSlackUsers() {
        final List<String> users = new ArrayList<>();
        slackSession.getUsers().forEach(user -> users.add(user.getRealName() + "|" + user.getUserName()));
        return users;
    }


    /**
     * Sends a message via the Slack bot.
     *
     * @param slackId Slack ID for recipient
     * @param body message to be delivered
     */
    public void sendSlackMessage(final String slackId, final String body) throws ResourceNotFoundException {
        if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SLACK_ENABLED_KEY).getValue())) {
            return;
        }
        try {
            LOGGER.info(String.format("Sending Slack message [%s] to [%s]", body, slackId));
            SlackPreparedMessage.SlackPreparedMessageBuilder builder = SlackPreparedMessage.builder();
            builder.message(body);
            slackSession.sendMessageToUser(slackId, builder.build());
        } catch (Exception e) {
            LOGGER.info("Unable to send slack message to [" + slackId + "] due to [" + e.getMessage() + "]");
        }
    }

}
