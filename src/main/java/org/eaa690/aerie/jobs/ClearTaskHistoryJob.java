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

import org.eaa690.aerie.config.TimedTaskProperties;
import org.eaa690.aerie.model.JobStatusRepository;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Clears old history records of task execution.
 */
@DisallowConcurrentExecution
public class ClearTaskHistoryJob extends QuartzJobBean {

    /**
     * TimedTaskProperties.
     */
    @Autowired
    private TimedTaskProperties timedTaskProperties;

    /**
     * JobStatusRepository.
     */
    @Autowired
    private JobStatusRepository jobStatusRepository;

    /**
     * Required Implementation.
     *
     * @param context JobExecutionContext
     * @throws JobExecutionException when things go wrong
     */
    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        try {
            ZonedDateTime expiryDate = LocalDate
                    .now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .minusDays(timedTaskProperties.getDaysToExpiry());
            jobStatusRepository.deleteByJobCreatedBefore(expiryDate);
        } catch (Throwable e) {
            throw new JobExecutionException(e);
        }
    }
}
