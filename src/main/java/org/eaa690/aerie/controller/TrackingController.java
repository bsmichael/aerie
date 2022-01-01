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

package org.eaa690.aerie.controller;

import org.eaa690.aerie.model.TrackingData;
import org.eaa690.aerie.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TrackingController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/tracking"
})
public class TrackingController {

    /**
     * TrackingService.
     */
    private TrackingService trackingService;

    /**
     * Sets TrackingService.
     *
     * @param value TrackingService
     */
    @Autowired
    public void setTrackingService(final TrackingService value) {
        trackingService = value;
    }

    /**
     * Constructor.
     *
     * @param tService TrackingService
     */
    public TrackingController(final TrackingService tService) {
        trackingService = tService;
    }

    /**
     * Tracks message opens by members.
     *
     * @param rosterId roster ID
     * @param messageId message ID
     */
    @GetMapping(path = {"/record/{rosterId}/{messageId}"})
    public void tracking(@PathVariable("rosterId") final Long rosterId,
                         @PathVariable("messageId") final Long messageId) {
        trackingService.recordTrackingResponse(rosterId, messageId);
    }

    /**
     * Retrieves tracking events for specified member.
     *
     * @param rosterId roster ID
     * @return list of TrackingData
     */
    @GetMapping(path = {"/{rosterId}/events"})
    public List<TrackingData> memberEvents(@PathVariable("rosterId") final Long rosterId) {
        return trackingService.getTrackingDataByMember(rosterId);
    }

    /**
     * Retrieves all tracking events.
     *
     * @return list of TrackingData
     */
    @GetMapping(path = {"/events"})
    public List<TrackingData> allEvents() {
        return trackingService.getAllTrackingData();
    }
}
