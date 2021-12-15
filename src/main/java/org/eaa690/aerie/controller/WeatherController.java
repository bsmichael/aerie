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

import org.eaa690.aerie.config.WeatherProperties;
import org.eaa690.aerie.model.wx.METAR;
import org.eaa690.aerie.exception.InvalidPayloadException;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.service.WeatherService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * WeatherController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/weather"
})
public class WeatherController {

    /**
     * INVALID_STATION_MSG.
     */
    public static final String INVALID_STATION_MSG =
            "Provided station [%s] is not on the Atlanta sectional chart.  "
                    + "Please provide an accepted station identifier";

    /**
     * ATLANTA.
     */
    public static final String ATLANTA = "atlanta";

    /**
     * WeatherService.
     */
    @Autowired
    private WeatherService weatherService;

    /**
     * WeatherProperties.
     */
    @Autowired
    private WeatherProperties weatherProperties;

    /**
     * Sets WeatherService.
     *
     * @param value WeatherService
     */
    @Autowired
    public void setWeatherService(final WeatherService value) {
        weatherService = value;
    }

    /**
     * Sets WeatherProperties.
     *
     * @param value WeatherProperties
     */
    @Autowired
    public void setWeatherProperties(final WeatherProperties value) {
        weatherProperties = value;
    }

    /**
     * Updates weather information from AviationWeather.gov.
     * Note: normally this is run automatically every 10 minutes
     */
    @PostMapping(path = {"/update"})
    public void updateWeather() {
        weatherService.update();
    }

    /**
     * Get METAR.
     *
     * Note: The only accepted station codes are those found on the Atlanta Sectional Chart
     *
     * @param icao station code
     * @param dataList attributes to be returned in response
     * @return METAR
     * @throws ResourceNotFoundException when METAR is not found
     * @throws InvalidPayloadException when an invalid station code is provided
     */
    @GetMapping(path = {
            "/metars/{icao}"
    })
    public List<METAR> metar(
            @PathVariable("icao") final String icao,
            @RequestParam(required = false, value = "data") final List<String> dataList)
            throws ResourceNotFoundException,
            InvalidPayloadException {
        final List<METAR> metars = new ArrayList<>();
        if (ATLANTA.equalsIgnoreCase(icao)) {
            metars.addAll(weatherService.getMETARs(
                    Arrays.asList(weatherProperties.getAtlantaIcaoCodes().split(","))));
        } else if (weatherService.isValidStation(icao.toUpperCase())) {
            metars.addAll(List.of(weatherService.getMETAR(icao.toUpperCase())));
        }
        if (CollectionUtils.isNotEmpty(metars)) {
            return filterAttributes(metars, dataList);
        }
        throw new InvalidPayloadException(String.format(INVALID_STATION_MSG, icao));
    }

    /**
     * Filters METAR attributes to only those specified, if any are specified.
     *
     * @param metars List of METAR
     * @param dataList attributes to be returned
     * @return filtered List of METAR
     */
    private static List<METAR> filterAttributes(final List<METAR> metars, final List<String> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return metars;
        }
        final List<METAR> filteredMetars = new ArrayList<>();
        for (final METAR metar : metars) {
            final METAR filteredMetar = new METAR();
            for (final String data : dataList) {
                filteredMetar.setIcao(metar.getIcao());
                switch (data) {
                    case METAR.OBSERVED:
                        filteredMetar.setObserved(metar.getObserved());
                        break;
                    case METAR.RAW_TEXT:
                        filteredMetar.setRawText(metar.getRawText());
                        break;
                    case METAR.BAROMETER:
                        filteredMetar.setBarometer(metar.getBarometer());
                        break;
                    case METAR.CEILING:
                        filteredMetar.setCeiling(metar.getCeiling());
                        break;
                    case METAR.CLOUDS:
                        filteredMetar.setClouds(metar.getClouds());
                        break;
                    case METAR.DEWPOINT:
                        filteredMetar.setDewpoint(metar.getDewpoint());
                        break;
                    case METAR.ELEVATION:
                        filteredMetar.setElevation(metar.getElevation());
                        break;
                    case METAR.FLIGHT_CATEGORY:
                        filteredMetar.setFlightCategory(metar.getFlightCategory());
                        break;
                    case METAR.HUMIDITY_PERCENT:
                        filteredMetar.setHumidityPercent(metar.getHumidityPercent());
                        break;
                    case METAR.TEMPERATURE:
                        filteredMetar.setTemperature(metar.getTemperature());
                        break;
                    case METAR.VISIBILITY:
                        filteredMetar.setVisibility(metar.getVisibility());
                        break;
                    case METAR.WIND:
                        filteredMetar.setWind(metar.getWind());
                        break;
                    default:
                        filteredMetar.setName(metar.getName());
                }
            }
            filteredMetars.add(filteredMetar);
        }
        return filteredMetars;
    }

}
