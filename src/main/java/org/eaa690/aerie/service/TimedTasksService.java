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

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.config.TimedTaskProperties;
import org.eaa690.aerie.jobs.ClearTaskHistoryJob;
import org.eaa690.aerie.jobs.GetJotFormSubmissions;
import org.eaa690.aerie.jobs.SendMembershipRenewalMessages;
import org.eaa690.aerie.jobs.UpdateRoster;
import org.eaa690.aerie.jobs.UpdateWeather;
import org.eaa690.aerie.model.JobStatusRepository;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.NameMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Time Service.
 */
@Slf4j
public class TimedTasksService implements JobListener {

    /**
     * Default Service Group.
     */
    private static final String DEFAULT_SERVICE_GROUP = "DEFAULT_SERVICE_GROUP";

    /**
     * Quartz Scheduler.
     */
    @Autowired
    private final Scheduler scheduler;

    /**
     * TimedTaskProperties.
     */
    @Autowired
    private final TimedTaskProperties timedTaskProperties;

    /**
     * JobStatusRepository.
     */
    @Autowired
    private JobStatusRepository jobStatusRepository;

    /**
     * Constructor.
     *
     * @param quartzScheduler Scheduler
     * @param props TimedTaskProperties
     */
    public TimedTasksService(final Scheduler quartzScheduler, final TimedTaskProperties props) {
        scheduler = quartzScheduler;
        timedTaskProperties = props;
    }

    /**
     * Initial setup of default service jobs.
     *
     * @throws SchedulerException when things go wrong
     */
    @PostConstruct
    public void init() throws SchedulerException {
        final Map<String, String> tasks = timedTaskProperties.getTasks();
        for (final Map.Entry<String, String> entry : tasks.entrySet()) {
            final String task = entry.getKey();
            final String cron = entry.getValue();
            JobDetail jobDetail = null;
            Trigger trigger = null;
            switch (task) {
                case "update-weather":
                    jobDetail = buildJobDetail(task, UpdateWeather.class);
                    trigger = buildTrigger(task, cron, jobDetail);
                    break;
                case "update-roster":
                    jobDetail = buildJobDetail(task, UpdateRoster.class);
                    trigger = buildTrigger(task, cron, jobDetail);
                    break;
                case "get-jot-form-submissions":
                    jobDetail = buildJobDetail(task, GetJotFormSubmissions.class);
                    trigger = buildTrigger(task, cron, jobDetail);
                    break;
                case "send-membership-renewal-messages":
                    jobDetail = buildJobDetail(task, SendMembershipRenewalMessages.class);
                    trigger = buildTrigger(task, cron, jobDetail);
                    break;
                case "clear-task-history":
                    jobDetail = buildJobDetail(task, ClearTaskHistoryJob.class);
                    trigger = buildTrigger(task, cron, jobDetail);
                    break;
                default:
                    log.info("Unknown task provided {}", task);
            }
            scheduleJobAndTrigger(task, jobDetail, trigger);
        }
    }

    /**
     * Schedules a job and trigger for execution.
     *
     * @param task name
     * @param jobDetail JobDetail
     * @param trigger Trigger
     * @throws SchedulerException when things go wrong
     */
    private void scheduleJobAndTrigger(final String task, final JobDetail jobDetail, final Trigger trigger)
            throws SchedulerException {
        try {
            if (jobDetail != null) {
                scheduler.addJob(jobDetail, Boolean.FALSE);
                scheduler.getListenerManager()
                        .addJobListener(this, NameMatcher.jobNameEquals(task));
            }
            if (trigger != null) {
                scheduler.scheduleJob(trigger);
            }
        } catch (ObjectAlreadyExistsException e) {
            log.debug("Task is already scheduled: {}", e.getMessage());
        }
    }

    /**
     * Builds a JobDetail.
     *
     * @param task name
     * @param clazz Job
     * @return JobDetail
     */
    private JobDetail buildJobDetail(final String task, final Class clazz) {
        return JobBuilder.newJob(clazz)
                // This allows other nodes to pick up the job if the executing node fails.
                // Jobs will not be re-executed when an exception occurs.
                .requestRecovery()
                .storeDurably()
                .withIdentity(task, DEFAULT_SERVICE_GROUP)
                .build();
    }

    /**
     * Builds a trigger for a job.
     *
     * @param task name
     * @param cron schedule
     * @param jobDetail JobDetail
     * @return Trigger
     */
    private Trigger buildTrigger(final String task, final String cron, final JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail.getKey())
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .withIdentity(task, DEFAULT_SERVICE_GROUP)
                .build();
    }

    /**
     * Required Implementation.
     *
     * @return name
     */
    @Override
    public String getName() {
        return DEFAULT_SERVICE_GROUP;
    }

    /**
     * Required Implementation.
     *
     * @param jobExecutionContext JobExecutionContext
     */
    @Override
    public void jobToBeExecuted(final JobExecutionContext jobExecutionContext) {
        TriggerKey triggerKey;
        if (jobExecutionContext.isRecovering()) {
            triggerKey = jobExecutionContext.getRecoveringTriggerKey();
        } else {
            triggerKey = jobExecutionContext.getTrigger().getKey();
        }
        jobExecutionStarted(triggerKey.getName());
    }

    /**
     * Required Implementation.
     *
     * @param jobExecutionContext JobExecutionContext
     */
    @Override
    public void jobExecutionVetoed(final JobExecutionContext jobExecutionContext) {
        log.warn("Job vetoed: {}", jobExecutionContext.getJobDetail().getKey().getName());
    }

    /**
     * Required Implementation.
     *
     * @param jobExecutionContext JobExecutionContext
     * @param e JobExecutionException
     */
    @Override
    public void jobWasExecuted(final JobExecutionContext jobExecutionContext, final JobExecutionException e) {
        TriggerKey triggerKey;
        if (jobExecutionContext.isRecovering()) {
            triggerKey = jobExecutionContext.getRecoveringTriggerKey();
        } else {
            triggerKey = jobExecutionContext.getTrigger().getKey();
        }
        jobExecutionFinished(triggerKey.getName(), e == null);
    }

    /**
     * Updates a job's status as started.
     *
     * @param jobId Job ID
     */
    @Transactional
    public void jobExecutionStarted(final String jobId) {
        try {
            jobStatusRepository
                    .findById(jobId)
                    .ifPresent(jobStatus -> jobStatusRepository.save(jobStatus.jobStarted()));
        } catch (RuntimeException e) {
            log.warn("Unable to update job started status for job: {}", jobId, e);
        }
    }

    /**
     * Updates a job's status as finished.
     *
     * @param jobId ID
     * @param success successful completion
     */
    @Transactional
    public void jobExecutionFinished(final String jobId, final boolean success) {
        try {
            jobStatusRepository
                    .findById(jobId)
                    .ifPresent(jobStatus -> {
                        if (success) {
                            jobStatusRepository.save(jobStatus.jobFinished().finishedSuccessfully());
                        } else {
                            jobStatusRepository.save(jobStatus.jobFinished());
                        }
                    });
        } catch (RuntimeException e) {
            log.warn("Unable to update job finished status for job: {}", jobId, e);
        }
    }
}
