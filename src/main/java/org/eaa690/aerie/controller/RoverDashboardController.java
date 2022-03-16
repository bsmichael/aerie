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

import org.eaa690.aerie.service.RoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoverDashboardController {

    /**
     * RoverService.
     */
    private RoverService roverService;

    /**
     * Sets RoverService.
     *
     * @param rService RoverService
     */
    @Autowired
    public void setRoverService(final RoverService rService) {
        roverService = rService;
    }

    /**
     * Contructor.
     *
     * @param rService RoverService
     */
    public RoverDashboardController(final RoverService rService) {
        roverService = rService;
    }

    /**
     * Gets rover dashboard.
     *
     * @param model Model
     * @return dashboard
     */
    @GetMapping("/rover/dashboard")
    public String dashboard(final Model model) {
        model.addAttribute("", "");
        return "roverDashboard";
    }

}
