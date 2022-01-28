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

package org.eaa690.aerie.jobs;

import org.eaa690.aerie.service.WeatherService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Updates local database with data from EAA Roster Management database.
 */
@DisallowConcurrentExecution
public class UpdateWeather extends QuartzJobBean {

    /**
     * WeatherService.
     */
    private WeatherService weatherService;

    /**
     * Sets WeatherService.
     *
     * @param wService WeatherService
     */
    @Autowired
    public void setWeatherService(final WeatherService wService) {
        weatherService = wService;
    }

    /**
     * Required Implementation.
     *
     * @param context JobExecutionContext
     * @throws JobExecutionException when things go wrong
     */
    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        weatherService.update();
    }
}
