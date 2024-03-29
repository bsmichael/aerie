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

package org.eaa690.aerie.model.wx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Forecast object received from api.checkwx.com.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Forecast implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ForecastTimestamp.
     */
    private ForecastTimestamp timestamp;

    /**
     * Cloud List.
     */
    private List<Cloud> clouds;

    /**
     * Visibility.
     */
    private Visibility visibility;

    /**
     * Wind.
     */
    private Wind wind;

    /**
     * Change Indicator.
     */
    @JsonProperty("change_indicator")
    private String changeIndicator;

}
