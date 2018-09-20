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
			data.put("date", new DateTime(2018, 3, 28, 4, 0, 0)); // set fake past date for test
			data.put("extent", job.getAreaOfInterest().getExtent());

			factory.initializeParameters(job, data);
			JobDetail jobDetail = factory.prepareJob(job, data);

			Trigger trigger = prepareTrigger(job);

			scheduler.scheduleJob(jobDetail, trigger); 

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	private Trigger prepareTrigger(WacodisJobDefinition job) {
		log.info("Build new Trigger");
		return TriggerBuilder.newTrigger()
				.withIdentity(job.getId().toString(), job.getName())
				.startNow()
				.withSchedule(SimpleScheduleBuilder
						.simpleSchedule().repeatForever()
				.withIntervalInSeconds(Integer.parseInt(job.getTimeInterval())))
				.build();
	}

}
