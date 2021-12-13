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

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.roster.CellPhoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SMSService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    /**
     * EmailService.
     */
    @Autowired
    private EmailService emailService;

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * Sets EmailService. Note: mostly used for unit test mocks
     *
     * @param eService EmailService
     */
    @Autowired
    public void setEmailService(final EmailService eService) {
        emailService = eService;
    }

    /**
     * Sends an SMS message.
     *
     * @param recipientAddress Recipient Cell Phone Number
     * @param cellPhoneProvider Recipient's cell phone provider
     * @param subject of the message
     * @param body of the message
     * @param from sender
     * @param password for the sender's mailbox
     */
    public void sendSMSMessage(final String recipientAddress,
                                final CellPhoneProvider cellPhoneProvider,
                                final String subject,
                                final String body,
                                final String from,
                                final String password) throws ResourceNotFoundException {
        if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SMS_ENABLED_KEY).getValue())) {
            return;
        }
        final String to = String.format("%s@%s", recipientAddress, cellPhoneProvider.getCellPhoneProviderEmailDomain());
        LOGGER.info(String.format("Sending Slack message [%s] to [%s]", body, to));
        emailService.sendEmailMessage(
                to,
                subject,
                body,
                from,
                password,
                null);
    }

}
