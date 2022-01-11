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
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.apache.catalina.connector.Connector;
import org.eaa690.aerie.model.WeatherProductRepository;
import org.eaa690.aerie.roster.RosterManager;
import org.eaa690.aerie.service.EmailService;
import org.eaa690.aerie.service.GateCodeService;
import org.eaa690.aerie.service.JotFormService;
import org.eaa690.aerie.service.NotamService;
import org.eaa690.aerie.service.RosterService;
import org.eaa690.aerie.service.SlackService;
import org.eaa690.aerie.service.TinyURLService;
import org.eaa690.aerie.service.TrackingService;
import org.eaa690.aerie.service.WeatherService;
import org.eaa690.aerie.ssl.SSLUtilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * ServiceConfig.
 */
@Configuration
@EnableConfigurationProperties({
        WeatherProperties.class,
        TinyUrlProperties.class,
        RosterProperties.class,
        JotFormProperties.class,
        EmailProperties.class,
        NotamProperties.class,
        TrackingProperties.class,
        SlackProperties.class,
        MembershipProperties.class})
public class ServiceConfig {

    /**
     * HttpPort.
     */
    @Value("${http.port}")
    private int httpPort;

    /**
     * SpringTemplateEngine.
     *
     * @param emailProperties EmailProperties
     * @return SpringTemplateEngine
     */
    @Bean
    public SpringTemplateEngine thymeleafTemplateEngine(final EmailProperties emailProperties) {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        final FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(emailProperties.getTemplatePath());
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCacheable(Boolean.FALSE);
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

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
     * @param props WeatherProperties
     * @param sslUtilities SSLUtilities
     * @param wpRepository WeatherProductRepository
     * @return WeatherService
     */
    @Bean
    public WeatherService weatherService(
            final RestTemplate restTemplate,
            final WeatherProperties props,
            final SSLUtilities sslUtilities,
            final WeatherProductRepository wpRepository) {
        final WeatherService weatherService = new WeatherService();
        weatherService.setRestTemplate(restTemplate);
        weatherService.setSSLUtilities(sslUtilities);
        weatherService.setWeatherProperties(props);
        weatherService.setWeatherProductRepository(wpRepository);
        return weatherService;
    }

    /**
     * RosterManager.
     *
     * @param props RosterProperties
     * @return RosterManager
     */
    @Bean
    public RosterManager rosterManager(final RosterProperties props) {
        return new RosterManager(props.getUsername(), props.getPassword());
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
     * RosterService.
     *
     * @return RosterService
     */
    @Bean
    public RosterService rosterService() {
        return new RosterService();
    }

    /**
     * NotamService.
     *
     * @return NotamService
     */
    @Bean
    public NotamService notamService() {
        return new NotamService();
    }

    /**
     * GateCodeService.
     *
     * @return GateCodeService
     */
    @Bean
    public GateCodeService gateCodeService() {
        return new GateCodeService();
    }

    /**
     * EmailService.
     *
     * @return EmailService
     */
    @Bean
    public EmailService emailService() {
        return new EmailService();
    }

    /**
     * SlackService.
     *
     * @return SlackService
     */
    @Bean
    public SlackService slackService() {
        return new SlackService();
    }

    /**
     * TrackingService.
     *
     * @return TrackingService
     */
    @Bean
    public TrackingService trackingService() {
        return new TrackingService();
    }

    /**
     * Membership Bot SlackSession.
     *
     * @param slackProperties SlackProperties
     * @return SlackSession
     */
    @Bean(name = "membership")
    public SlackSession slackSession(final SlackProperties slackProperties) {
        try {
            final SlackSession slackSession =
                    SlackSessionFactory.createWebSocketSlackSession(slackProperties.getToken());
            slackSession.connect();
            return slackSession;
        } catch (IOException e) {
            return null;
        }
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
     * SSLUtilities.
     *
     * @return SSLUtilities
     */
    @Bean
    public SSLUtilities sslUtilities() {
        return new SSLUtilities();
    }

    /**
     * Configuring additional connector to enable support for both HTTP and HTTPS.
     *
     * @return ServletWebServerFactory
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(connector);
        return tomcat;
    }
}
