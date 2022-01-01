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

import org.eaa690.aerie.config.TrackingProperties;
import org.eaa690.aerie.model.TrackingData;
import org.eaa690.aerie.model.TrackingDataRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TrackingService.
 */
public class TrackingService {

    /**
     * EmailProperties.
     */
    @Autowired
    private TrackingProperties trackingProperties;

    /**
     * TrackingDataRepository.
     */
    @Autowired
    private TrackingDataRepository trackingDataRepository;

    /**
     * Sets TrackingDataRepository.
     * Note: mostly used for unit test mocks
     *
     * @param tdRepository TrackingDataRepository
     */
    @Autowired
    public void setTrackingDataRepository(final TrackingDataRepository tdRepository) {
        trackingDataRepository = tdRepository;
    }

    /**
     * Sets TrackingProperties.
     * Note: mostly used for unit test mocks
     *
     * @param value TrackingProperties
     */
    @Autowired
    public void setTrackingProperties(final TrackingProperties value) {
        trackingProperties = value;
    }

    /**
     * Tracks message opens by members.
     *
     * @param rosterId Roster ID
     * @param messageId Message ID
     */
    public void recordTrackingResponse(final Long rosterId, final Long messageId) {
        trackingDataRepository.save(new TrackingData(rosterId, messageId, new Date()));
    }

    /**
     * Generates a tracking link to be embedded in messages (emails, Slack, SMS, etc).
     *
     * @param rosterId Roster ID
     * @param messageId Message ID
     * @return tracking link
     */
    public String generateTrackingLink(final Long rosterId, final Long messageId) {
        return trackingProperties.getHost() + "/tracking/record/" + rosterId + "/" + messageId;
    }

    /**
     * Retrieves tracking data for the provided member.
     *
     * @param rosterId roster ID
     * @return matching TrackingData
     */
    public List<TrackingData> getTrackingDataByMember(final Long rosterId) {
        return trackingDataRepository.findByRosterId(rosterId).orElseGet(ArrayList::new);
    }

    /**
     * Retrieves all tracking data.
     *
     * @return all TrackingData
     */
    public List<TrackingData> getAllTrackingData() {
        return trackingDataRepository.findAll().orElseGet(ArrayList::new);
    }
}
