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

import lombok.Getter;
import lombok.Setter;
import org.eaa690.aerie.config.SlackProperties;
import org.eaa690.aerie.model.MessageRepository;
import org.eaa690.aerie.ssl.SSLUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;

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
     * MessageRepository.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Synchronous rest template.
     */
    @Autowired
    private RestTemplate restTemplate;

    /**
     * SSLUtilities.
     */
    @Autowired
    private SSLUtilities sslUtilities;

    /**
     * Sets SSLUtilities.
     * Note: mostly used for unit test mocks
     *
     * @param value SSLUtilities
     */
    @Autowired
    public void setSSLUtilities(final SSLUtilities value) {
        sslUtilities = value;
    }

    /**
     * Sets RestTemplate.
     * Note: mostly used for unit test mocks
     *
     * @param value RestTemplate
     */
    @Autowired
    public void setRestTemplate(final RestTemplate value) {
        restTemplate = value;
    }

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
    private boolean enabled = true;

    /**
     * Sends a Slack message.
     *
     * @param to message recipient
     * @param body of the message
     * @param from sender
     */
    public void sendSlackMessage(final String to,
                                 final String body,
                                 final String from) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(slackProperties.getToken());
        final HttpEntity<String> headersEntity =  new HttpEntity<>("parameters", headers);
        // Ignoring SSL certificate checking
        sslUtilities.trustAllHostnames();
        sslUtilities.trustAllHttpsCertificates();
        final String convOpenUrl = slackProperties.getBaseUrl() + "conversations.open?users=" + to + "&pretty=1";
        final String postMessageUrl = slackProperties.getBaseUrl() + "chat.postMessage?channel=%40" + to + "&as_user="
                + from + "&text=" + URLEncoder.encode(body, StandardCharsets.UTF_8) + "&pretty=1";
        try {
            if (enabled) {
                LOGGER.info("Sending Slack message [" + body + "] to [" + to + "] from [" + from + "]");
                restTemplate.exchange(convOpenUrl, HttpMethod.POST, headersEntity, String.class);
                final ResponseEntity<String> responseEntity =
                        restTemplate.exchange(postMessageUrl, HttpMethod.POST, headersEntity, String.class);
                if (responseEntity.getBody() != null
                        && responseEntity.getStatusCodeValue() >= HttpStatus.OK.value()
                        && responseEntity.getStatusCodeValue() < HttpStatus.MULTIPLE_CHOICES.value()) {
                    messageRepository.save(new org.eaa690.aerie.model.Message(Instant.now(), to, "SLACK", body));
                } else {
                    LOGGER.warn("Unable to send Slack message: " + responseEntity.getStatusCodeValue());
                }
            }
        } catch (RestClientException rce) {
            String msg = String.format("[RestClientException] Unable to send Slack message: %s", rce.getMessage());
            LOGGER.error(msg, rce);
        }
    }

}
