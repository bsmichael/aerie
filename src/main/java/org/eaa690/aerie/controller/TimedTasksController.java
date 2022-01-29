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

import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.JobStatus;
import org.eaa690.aerie.service.TimedTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TimedTasksController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/timed-tasks"
})
public class TimedTasksController {

    /**
     * TimedTasksService.
     */
    private TimedTasksService timedTasksService;

    /**
     * Sets TimedTasksService.
     *
     * @param value TimedTasksService
     */
    @Autowired
    public void setTimedTasksService(final TimedTasksService value) {
        timedTasksService = value;
    }

    /**
     * Constructor.
     *
     * @param ttService TimedTasksService
     */
    public TimedTasksController(final TimedTasksService ttService) {
        timedTasksService = ttService;
    }

    /**
     * Gets the status of the specified job.
     *
     * @param jobId Job ID
     * @return JobStatus
     */
    @GetMapping(path = {"/{jobId}/status" })
    public JobStatus getJobStatus(@PathVariable("jobId") final String jobId) throws ResourceNotFoundException {
        return timedTasksService.getJobStatus(jobId);
    }

}
