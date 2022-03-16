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

import com.google.common.net.HttpHeaders;
import org.eaa690.aerie.model.rover.Team;
import org.eaa690.aerie.service.RoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RoverController {

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
    public RoverController(final RoverService rService) {
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

    /**
     * Gets list of rover teams.
     *
     * @return teams list
     */
    @GetMapping("/rover/teams")
    public List<Team> getTeams() {
        return roverService.getTeams();
    }

    /**
     * Gets a rover team.
     *
     * @param teamId ID
     * @return Team
     */
    @GetMapping("/rover/{teamId}")
    public Team getTeam(@PathVariable("teamId") final Long teamId) {
        return roverService.getTeam(teamId);
    }

    /**
     * Adds a rover team.
     *
     * @param team Team
     * @return Team
     */
    @PostMapping("/rover/teams")
    public Team addTeam(@RequestBody final Team team) {
        return roverService.addTeam(team);
    }

    /**
     * Gets the latest image for a rover team.
     *
     * @param teamId ID
     * @return image
     */
    @GetMapping("/rover/{teamId}/latest")
    @ResponseBody
    public ResponseEntity<Resource> teamLatest(@PathVariable final Long teamId) {
        final Resource file = roverService.getLatestImageAsResource(teamId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /**
     * Uploads an image for a rover team.
     *
     * @param teamId ID
     * @param file image
     * @param redirectAttributes RedirectAttributes
     */
    @PostMapping("/rover/{teamId}")
    public void upload(@PathVariable("teamId") final Long teamId,
                       @RequestParam("file") final MultipartFile file,
                       final RedirectAttributes redirectAttributes) {
        roverService.store(teamId, file);
    }
}
