package de.wacodis.sensorweb.scheduler;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.quartz.JobBuilder;
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

import de.wacodis.api.model.AbstractSubsetDefinition;
import de.wacodis.api.model.Job;
import de.wacodis.api.model.SensorWebSubsetDefinition;
import de.wacodis.sensorweb.observer.ObservationObserver;
import de.wacodis.sensorweb.publisher.PublishChannels;

@Component
public class SensorWebJobScheduler {

	private static final Logger log = LoggerFactory.getLogger(SensorWebJobScheduler.class);

	private final String FLUGGS_URL = "http://fluggs.wupperverband.de/sos2/sos/soap";
	private final String N52_URL = "http://sensorweb.demo.52north.org/52n-sos-webapp/service";

	@Autowired
	private QuartzServer scheduler;
	
	public SensorWebJobScheduler() {
	}

	public void scheduleJob(Job job) {
		try {
			
			log.info("Scheduler initialized = {}", scheduler != null);

			JobDataMap data = new JobDataMap();
			initializeParameters(job, data);
			
			data.put("date", new DateTime(2018, 3, 28, 4, 0, 0)); // set fake past date for test

			data.put("extent", job.getAreaOfInterest().getExtent());
			JobDetail jobDetail = prepareJob(job, data);

			Trigger trigger = prepareTrigger(job);

			scheduler.scheduleJob(jobDetail, trigger); 

		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

	private void initializeParameters(Job job, JobDataMap data) {
		for (AbstractSubsetDefinition subset : job.getInputs()) {
			
			if (subset instanceof SensorWebSubsetDefinition) {
				SensorWebSubsetDefinition senSubset = (SensorWebSubsetDefinition) subset;
				data.put("procedures", Collections.singletonList(senSubset.getProcedure()));
				data.put("observedProperties", Collections.singletonList(senSubset.getObservedProperty()));
				data.put("offerings", Collections.singletonList(senSubset.getOffering()));
				data.put("featureIdentifiers", Collections.singletonList(senSubset.getFeatureOfInterest()));
			}
		}
	}

	private Trigger prepareTrigger(Job job) {
		log.info("building new Trigger");
		return TriggerBuilder.newTrigger().withIdentity(job.getId(), job.getName()).startNow()
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().repeatForever()
						.withIntervalInSeconds(Integer.parseInt(job.getTimeInterval())))
				.build();
	}

	private JobDetail prepareJob(Job job, JobDataMap data) {
		log.info("preparing Job, observer in jobDataMap = {}", data.get("observer"));
		
		return JobBuilder.newJob(SensorWebJob.class).withIdentity(job.getId(), job.getName()).usingJobData(data)
				.build();
		
	}

}
