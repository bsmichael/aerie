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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.TinyURLResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * TinyURLService.
 */
public class TinyURLService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TinyURLService.class);

    /**
     * JSON Object Serializer/Deserializer.
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * HttpClient.
     */
    @Autowired
    private HttpClient httpClient;

    /**
     * Sets PropertyService.
     * Note: mostly used for unit test mocks
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    /**
     * Sets HttpClient.
     * Note: mostly used for unit test mocks
     *
     * @param value HttpClient
     */
    @Autowired
    public void setHttpClient(final HttpClient value) {
        httpClient = value;
    }

    /**
     * Sends new member message.
     *
     * @param originalValue Original URL value
     * @return tiny url
     */
    public String getTinyURL(final String originalValue) {
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                    // "https://api.tinyurl.com/create"
                    .uri(URI.create(propertyService.get(PropertyKeyConstants.TINY_URL_CREATE_API_KEY).getValue()))
                    .setHeader("accept", "application/json")
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Authorization", "Bearer "
                            + propertyService.get(PropertyKeyConstants.TINY_URL_API_KEY).getValue())
                    .POST(HttpRequest.BodyPublishers.ofString("{\"url\":\""
                            + originalValue
                            + "\",\"domain\":\"tiny.one\"}"));
            final HttpResponse<String> response =
                    httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            final TinyURLResponse tuResponse = mapper.readValue(response.body(), TinyURLResponse.class);
            return tuResponse.getData().getTinyUrl();
        } catch (IOException | InterruptedException | ResourceNotFoundException e) {
            LOGGER.error("[Get Tiny URL] Error: " + e.getMessage(), e);
        }
        return null;
    }

}
