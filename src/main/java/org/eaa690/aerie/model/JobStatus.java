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

package org.eaa690.aerie.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * JobStatus.
 */
@Getter
@Entity
@Table(name = "JOB_STATUS")
@NoArgsConstructor
public class JobStatus {

    /**
     * Job ID.
     */
    @Id
    @Column(name = "JOB_ID")
    private String jobId;

    /**
     * Job Name.
     */
    @NonNull
    @Column(name = "JOB_NAME")
    private String jobName;

    /**
     * Job Created.
     */
    @Column(name = "JOB_CREATED")
    private ZonedDateTime jobCreated;

    /**
     * Job Started.
     */
    @Column(name = "JOB_STARTED")
    private ZonedDateTime jobStarted;

    /**
     * Job Finished.
     */
    @Column(name = "JOB_FINISHED")
    private ZonedDateTime jobFinished;

    /**
     * Finished Successfully.
     */
    @NotNull
    @Column(name = "FINISHED_SUCCESSFULLY")
    private boolean finishedSuccessfully = false;

    /**
     * Constructor.
     *
     * @param id ID
     * @param name Name
     */
    public JobStatus(final String id, final String name) {
        jobId = id;
        jobName = name;
    }

    /**
     * Sets job created time to now.
     *
     * @return JobStatus
     */
    public JobStatus jobCreated() {
        jobCreated = ZonedDateTime.now();
        return this;
    }

    /**
     * Sets job start time to now.
     *
     * @return JobStatus
     */
    public JobStatus jobStarted() {
        jobStarted = ZonedDateTime.now();
        return this;
    }

    /**
     * Sets job finished time to now.
     *
     * @return JobStatus
     */
    public JobStatus jobFinished() {
        jobFinished = ZonedDateTime.now();
        return this;
    }

    /**
     * Sets job finished successfully to true.
     *
     * @return JobStatus
     */
    public JobStatus finishedSuccessfully() {
        finishedSuccessfully = Boolean.TRUE;
        return this;
    }
}
