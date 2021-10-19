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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.constant.WeatherConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.WeatherProduct;
import org.eaa690.aerie.model.WeatherProductRepository;
import org.eaa690.aerie.model.wx.Barometer;
import org.eaa690.aerie.model.wx.Ceiling;
import org.eaa690.aerie.model.wx.Cloud;
import org.eaa690.aerie.model.wx.Dewpoint;
import org.eaa690.aerie.model.wx.METAR;
import org.eaa690.aerie.model.wx.Temperature;
import org.eaa690.aerie.model.wx.Visibility;
import org.eaa690.aerie.model.wx.Wind;
import org.eaa690.aerie.ssl.SSLUtilities;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * WeatherService.
 */
public class WeatherService {

    /**
     * Synchronous rest template.
     */
    @Autowired
    private RestTemplate restTemplate;

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * SSLUtilities.
     */
    @Autowired
    private SSLUtilities sslUtilities;

    /**
     * JSON Object Serializer/Deserializer.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(WeatherService.class);

    /**
     * WeatherProductRepository.
     */
    @Autowired
    private WeatherProductRepository weatherProductRepository;

    /**
     * Sets ObjectMapper.
     * Note: mostly used for unit test mocks
     *
     * @param value ObjectMapper
     */
    @Autowired
    public void setObjectMapper(final ObjectMapper value) {
        objectMapper = value;
    }

    /**
     * Sets WeatherProductRepository.
     * Note: mostly used for unit test mocks
     *
     * @param wpRepository WeatherProductRepository
     */
    @Autowired
    public void setWeatherProductRepository(final WeatherProductRepository wpRepository) {
        weatherProductRepository = wpRepository;
    }

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
     * Updates every 10 minutes.
     *
     * second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0,10,20,30,40,50 * * * *")
    public void update() {
        getMETARsFromAviationWeather();
        // https://www.aviationweather.gov/cgi-bin/json/TafJSON.php?density=all&bbox=-85.6898,30.1588,-80.8209,35.1475
    }

    /**
     * Retrieves the current METAR for a given airport.
     *
     * @param icaoCodes for the METAR observation
     * @return list of {@link METAR}
     */
    public List<METAR> getMETARs(final List<String> icaoCodes) {
        final List<METAR> metars = new ArrayList<>();
        if (icaoCodes == null || icaoCodes.isEmpty()) {
            return metars;
        }
        icaoCodes.forEach(icaoCode -> {
            try {
                metars.add(getMETAR(icaoCode));
            } catch (ResourceNotFoundException rnfe) {
                LOGGER.error("No METAR found for " + icaoCode, rnfe);
            }
        });
        return metars;
    }

    /**
     * Retrieves the current METAR for a given airport.
     *
     * @param icaoCode for the METAR observation
     * @return {@link METAR}
     * @throws ResourceNotFoundException when no information is found for the given ID
     */
    public METAR getMETAR(final String icaoCode) throws ResourceNotFoundException {
        METAR cachedMetar = null;
        Optional<WeatherProduct> weatherProductOpt =
                weatherProductRepository.findByKey(CommonConstants.METAR_KEY + icaoCode);
        if (weatherProductOpt.isPresent()) {
            WeatherProduct weatherProduct = weatherProductOpt.get();
            try {
                cachedMetar = objectMapper.readValue(weatherProduct.getValue(), METAR.class);
            } catch (IOException e) {
                LOGGER.warn(String.format("Unable to deserialize METAR from cache: %s", e.getMessage()));
            }
        }
        if (cachedMetar != null) {
            return cachedMetar;
        }
        throw new ResourceNotFoundException(String.format("METAR information not found for %s", icaoCode));
    }

    /**
     * Checks if provided station is valid.
     *
     * @param station to be validated
     * @return if station is valid
     * @throws ResourceNotFoundException when Atlanta ICAO codes property is not found
     */
    public boolean isValidStation(final String station) throws ResourceNotFoundException {
        boolean response = false;
        final List<String> validStationsList =
                Arrays
                        .asList(propertyService
                                .get(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY)
                                .getValue()
                                .split(","));
        if (validStationsList.contains(station)) {
            response = true;
        }
        return response;
    }

    /**
     * Queries AviationWeather.gov for METAR information.
     */
    private void getMETARsFromAviationWeather() {
        LOGGER.info(String.format("Querying AviationWeather.gov for METAR information"));
        final String url = "https://www.aviationweather.gov/cgi-bin/json/MetarJSON.php"
            + "?density=all&bbox=-85.6898,30.1588,-80.8209,35.1475";
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        final HttpEntity<String> headersEntity =  new HttpEntity<>("parameters", headers);
        // Ignoring SSL certificate checking
        sslUtilities.trustAllHostnames();
        sslUtilities.trustAllHttpsCertificates();
        try {
            final ResponseEntity<String> data =
                    restTemplate.exchange(url, HttpMethod.GET, headersEntity, String.class);
            if (data.getStatusCodeValue() >= HttpStatus.OK.value()
                    && data.getStatusCodeValue() < HttpStatus.MULTIPLE_CHOICES.value()) {
                JSONObject root = new JSONObject(new JSONTokener(data.getBody()));
                JSONArray features = root.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject station = features.getJSONObject(i);
                    if (station.has("id")) {
                        JSONObject props = station.getJSONObject("properties");
                        final METAR metar = parseMetar(props);
                        cacheMetar(metar.getIcao(), metar);
                    }
                }
            }
        } catch (RestClientException rce) {
            String msg = String.format("[RestClientException] Unable to retrieve METARs: %s", rce.getMessage());
            LOGGER.error(msg, rce);
        }
    }

    /**
     * Parses METAR information from AviationWeather.gov response.
     *
     * @param props JSONObject
     * @return METAR
     */
    @NotNull
    private METAR parseMetar(final JSONObject props) {
        final METAR metar = new METAR();
        metar.setIcao(props.getString(WeatherConstants.ID));
        metar.setObserved(props.getString(WeatherConstants.OBSERVED_TIME));
        if (props.has(WeatherConstants.TEMPERATURE)) {
            final Temperature temperature = new Temperature();
            temperature.setCelsius(Math.round(props.getDouble(WeatherConstants.TEMPERATURE)));
            metar.setTemperature(temperature);
        }
        if (props.has(WeatherConstants.DEWPOINT)) {
            final Dewpoint dewpoint = new Dewpoint();
            dewpoint.setCelsius(Math.round(props.getDouble(WeatherConstants.DEWPOINT)));
            metar.setDewpoint(dewpoint);
        }
        if (props.has(WeatherConstants.WIND_SPEED)) {
            final Wind wind = new Wind();
            wind.setSpeedKt(props.getInt(WeatherConstants.WIND_SPEED));
            wind.setDegrees(props.getInt(WeatherConstants.WIND_DIRECTION));
            metar.setWind(wind);
        }
        if (props.has(WeatherConstants.CEILING)) {
            final Ceiling ceiling = new Ceiling();
            ceiling.setFeet(props.getDouble(WeatherConstants.CEILING));
            ceiling.setCode(props.getString(WeatherConstants.COVER));
            metar.setCeiling(ceiling);
        }
        if (props.has(WeatherConstants.CLOUD_COVER + "1")) {
            final List<Cloud> clouds = new ArrayList<>();
            for (int j = 1; j < CommonConstants.TEN; j++) {
                if (props.has(WeatherConstants.CLOUD_COVER + j)) {
                    final Cloud cloud = new Cloud();
                    cloud.setCode(props.getString(WeatherConstants.CLOUD_COVER + j));
                    if (props.has(WeatherConstants.CLOUD_BASE + j)) {
                        cloud.setBaseFeetAgl(Double.parseDouble(props.getString(WeatherConstants.CLOUD_BASE + j))
                                * CommonConstants.ONE_HUNDRED);
                    }
                    clouds.add(cloud);
                }
            }
            metar.setClouds(clouds);
        }
        if (props.has(WeatherConstants.VISIBILITY)) {
            final Visibility visibility = new Visibility();
            visibility.setMiles(props.get(WeatherConstants.VISIBILITY).toString());
            metar.setVisibility(visibility);
        }
        if (props.has(WeatherConstants.FLIGHT_CATEGORY)) {
            metar.setFlightCategory(props.getString(WeatherConstants.FLIGHT_CATEGORY));
        }
        if (props.has(WeatherConstants.ALTIMETER)) {
            final Barometer barometer = new Barometer();
            barometer.setMb(props.getDouble(WeatherConstants.ALTIMETER));
            metar.setBarometer(barometer);
        }
        metar.setRawText(props.getString(WeatherConstants.RAW_OBSERVATION));
        metar.setCreatedAt(new Date());
        metar.setUpdatedAt(new Date());
        return metar;
    }

    /**
     * Caches METAR.
     *
     * @param icaoCode ICAO Code key for cached value
     * @param metar METAR to be cached
     */
    private void cacheMetar(final String icaoCode, final METAR metar) {
        try {
            WeatherProduct weatherProduct = new WeatherProduct();
            weatherProduct.setKey(CommonConstants.METAR_KEY + icaoCode);
            weatherProduct.setCreatedAt(new Date());
            final Optional<WeatherProduct> weatherProductOpt =
                    weatherProductRepository.findByKey(CommonConstants.METAR_KEY + icaoCode);
            if (weatherProductOpt.isPresent()) {
                weatherProduct = weatherProductOpt.get();
            }
            weatherProduct.setValue(objectMapper.writeValueAsString(metar));
            weatherProduct.setUpdatedAt(new Date());
            weatherProductRepository.save(weatherProduct);
        } catch (JsonProcessingException jpe) {
            LOGGER.warn(String.format("Unable to serialize METAR [%s]: %s", metar, jpe.getMessage()));
        }
    }

}
