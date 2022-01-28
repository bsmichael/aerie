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

import org.eaa690.aerie.service.JotFormService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Retrieves JotForm submissions.
 */
@DisallowConcurrentExecution
public class GetJotFormSubmissions extends QuartzJobBean {

    /**
     * JotFormService.
     */
    private JotFormService jotFormService;

    /**
     * Sets JotFormService.
     *
     * @param jfService JotFormService
     */
    @Autowired
    public void setJotFormService(final JotFormService jfService) {
        jotFormService = jfService;
    }

    /**
     * Required Implementation.
     *
     * @param context JobExecutionContext
     * @throws JobExecutionException when things go wrong
     */
    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        jotFormService.getSubmissions();
    }
}
