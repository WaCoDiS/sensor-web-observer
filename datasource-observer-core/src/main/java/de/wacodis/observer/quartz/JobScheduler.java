package de.wacodis.observer.quartz;

import org.joda.time.DateTime;
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

import de.wacodis.api.model.WacodisJobDefinition;
import de.wacodis.observer.core.JobFactory;

@Component
public class JobScheduler {

	private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

	@Autowired
	private QuartzServer scheduler;
	
	public JobScheduler() {
	}

	public void scheduleJob(WacodisJobDefinition job, JobFactory factory) {
		try {
			
			JobDataMap data = new JobDataMap();
			data.put("extent", job.getAreaOfInterest().getExtent());
			data.put("created", job.getCreated());
			
			factory.initializeParameters(job, data);
			JobDetail jobDetail = factory.prepareJob(job, data);

			Trigger trigger = prepareTrigger(job, data);

			scheduler.scheduleJob(jobDetail, trigger); 

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	private Trigger prepareTrigger(WacodisJobDefinition job, JobDataMap data) {
		log.info("Build new Trigger");
		return TriggerBuilder.newTrigger()
				.withIdentity(job.getId().toString(), job.getName())
				.startNow()
				.withSchedule(SimpleScheduleBuilder
						.simpleSchedule().repeatForever()
				.withIntervalInSeconds(data.getInt("executionInterval")))
				.build();
	}

}
