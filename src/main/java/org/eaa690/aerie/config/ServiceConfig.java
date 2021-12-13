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

package org.eaa690.aerie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alexanderwe.bananaj.connection.MailChimpConnection;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.WeatherProductRepository;
import org.eaa690.aerie.roster.RosterManager;
import org.eaa690.aerie.service.JotFormService;
import org.eaa690.aerie.service.MailChimpService;
import org.eaa690.aerie.service.PropertyService;
import org.eaa690.aerie.service.RosterService;
import org.eaa690.aerie.service.TinyURLService;
import org.eaa690.aerie.service.WeatherService;
import org.eaa690.aerie.ssl.SSLUtilities;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * ServiceConfig.
 */
@Configuration
public class ServiceConfig {

    /**
     * Creates a rest template with default timeout settings. The bean definition will be updated to accept timeout
     * parameters once those are part of the Customer settings.
     *
     * @param restTemplateBuilder RestTemplateBuilder
     *
     * @return Rest Template with request, read, and connection timeouts set
     */
    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(CommonConstants.ONE_THOUSAND))
                .setReadTimeout(Duration.ofMillis(CommonConstants.TEN_THOUSAND))
                .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    /**
     * WeatherService.
     *
     * @param restTemplate RestTemplate
     * @param propertyService PropertyService
     * @param sslUtilities SSLUtilities
     * @param wpRepository WeatherProductRepository
     * @return WeatherService
     */
    @Bean
    public WeatherService weatherService(
            final RestTemplate restTemplate,
            final PropertyService propertyService,
            final SSLUtilities sslUtilities,
            final WeatherProductRepository wpRepository) {
        final WeatherService weatherService = new WeatherService();
        weatherService.setRestTemplate(restTemplate);
        weatherService.setPropertyService(propertyService);
        weatherService.setSSLUtilities(sslUtilities);
        weatherService.setWeatherProductRepository(wpRepository);
        return weatherService;
    }

    /**
     * RosterManager.
     *
     * @param propertyService PropertyService
     * @return RosterManager
     * @throws ResourceNotFoundException when properties are not found
     */
    @Bean
    public RosterManager rosterManager(final PropertyService propertyService) throws ResourceNotFoundException {
        return new RosterManager(propertyService.get(PropertyKeyConstants.ROSTER_USER_KEY).getValue(),
                propertyService.get(PropertyKeyConstants.ROSTER_PASS_KEY).getValue());
    }

    /**
     * HttpClient.
     *
     * @return HttpClient
     */
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    /**
     * ObjectMapper.
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * MailChimpConnection.
     *
     * @param propertyService PropertyService
     * @return MailChimpConnection
     * @throws ResourceNotFoundException when things go wrong
     */
    @Bean
    public MailChimpConnection mailChimpConnection(final PropertyService propertyService)
            throws ResourceNotFoundException {
        return new MailChimpConnection(propertyService
                .get(PropertyKeyConstants.MAILCHIMP_API_KEY).getValue());
    }

    /**
     * RosterService.
     *
     * @return RosterService
     */
    @Bean
    public RosterService rosterService() {
        return new RosterService();
    }

    /**
     * JotFormService.
     *
     * @return JotFormService
     */
    @Bean
    public JotFormService jotFormService() {
        return new JotFormService();
    }

    /**
     * TinyURLService.
     *
     * @return TinyURLService
     */
    @Bean
    public TinyURLService tinyUrlService() {
        return new TinyURLService();
    }

    /**
     * MailChimpService.
     *
     * @return MailChimpService
     */
    @Bean
    public MailChimpService mailChimpService() {
        return new MailChimpService();
    }

    /**
     * SSLUtilities.
     *
     * @return SSLUtilities
     */
    @Bean
    public SSLUtilities sslUtilities() {
        return new SSLUtilities();
    }

    /**
     * SlackSession.
     *
     * @param propertyService PropertyService
     * @return SlackSession
     * @throws IOException when things go wrong
     * @throws ResourceNotFoundException when things go wrong
     */
    @Bean
    public SlackSession slackSession(final PropertyService propertyService /*,
                                     final SlackService slackService */)
            throws IOException, ResourceNotFoundException {
        final SlackSession slackSession = SlackSessionFactory
                .createWebSocketSlackSession(
                        propertyService.get(PropertyKeyConstants.SLACK_TOKEN_KEY).getValue());
        slackSession.connect();
        //slackSession.addMessagePostedListener(slackService);
        return slackSession;
    }

}
