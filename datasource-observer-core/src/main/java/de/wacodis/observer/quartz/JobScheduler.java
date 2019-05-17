package de.wacodis.observer.quartz;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.observer.core.JobFactory;

@Component
public class JobScheduler {
    
    private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);


    @Autowired
    private QuartzServer wacodisQuartz;

    public JobScheduler() {
    }

    public void scheduleJob(WacodisJobDefinition job, JobFactory factory) {
        try {
            JobDataMap data = new JobDataMap();
            data.put("areaOfInterest", job.getAreaOfInterest());

            JobDetail jobDetail = factory.initializeJob(job, data);

            Trigger trigger = prepareTrigger(job, data);
            
            wacodisQuartz.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.warn(e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
    }

    private Trigger prepareTrigger(WacodisJobDefinition job, JobDataMap data) {
        /**
         * set a default execution interval
         */
        if (!data.containsKey("executionInterval")) {
            data.put("executionInterval", 60 * 60);
        }
        
        LOG.info("Build new Trigger with execution interval: {} seconds", data.get("executionInterval"));

        return TriggerBuilder.newTrigger()
                .withIdentity(job.getId().toString(), job.getName())
                .startNow()
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule().repeatForever()
                        .withIntervalInSeconds(data.getInt("executionInterval")))
                .build();
    }
}
