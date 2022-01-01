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
import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.config.TinyUrlProperties;
import org.eaa690.aerie.model.TinyURLResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * TinyURLService.
 */
@Slf4j
public class TinyURLService {

    /**
     * JSON Object Serializer/Deserializer.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * PropertyService.
     */
    @Autowired
    private TinyUrlProperties tinyUrlProperties;

    /**
     * HttpClient.
     */
    @Autowired
    private HttpClient httpClient;

    /**
     * Sets TinyUrlProperties.
     * Note: mostly used for unit test mocks
     *
     * @param value TinyUrlProperties
     */
    @Autowired
    public void setTinyUrlProperties(final TinyUrlProperties value) {
        tinyUrlProperties = value;
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
        String tinyUrl = null;
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(tinyUrlProperties.getCreateUrl()))
                    .setHeader("accept", "application/json")
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Authorization", "Bearer " + tinyUrlProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString("{\"url\":\""
                            + originalValue
                            + "\",\"domain\":\"tiny.one\"}"));
            final HttpResponse<String> response =
                    httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            final TinyURLResponse tuResponse = mapper.readValue(response.body(), TinyURLResponse.class);
            tinyUrl = tuResponse.getData().getTinyUrl();
        } catch (IOException | InterruptedException e) {
            log.error("[Get Tiny URL] Error: " + e.getMessage(), e);
        }
        return tinyUrl;
    }

}
