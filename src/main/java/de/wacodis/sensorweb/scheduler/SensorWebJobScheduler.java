package de.wacodis.sensorweb.scheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;

import de.wacodis.api.model.AbstractSubsetDefinition;
import de.wacodis.api.model.Job;
import de.wacodis.api.model.SensorWebSubsetDefinition;
import de.wacodis.sensorweb.observer.ObservationObserver;

public class SensorWebJobScheduler {
	
	
	private static final Logger log = LoggerFactory.getLogger(SensorWebJobScheduler.class);

	
	private final String QUARTZ_JOB_PROPERTIES = "src/main/resources/quartzJob.properties";
	
	private final String FLUGGS_URL = "http://fluggs.wupperverband.de/sos2/sos/soap";
	private final String N52_URL = "http://sensorweb.demo.52north.org/52n-sos-webapp/service";
	private List<String> procedures, observedProperties, offerings, featureIdentifiers;
	private ObservationObserver observer;
	
	private Scheduler scheduler;
	
	public SensorWebJobScheduler() {
		procedures = new ArrayList<>();
		observedProperties = new ArrayList<>();
		offerings = new ArrayList<>();
		featureIdentifiers = new ArrayList<>();
	}
	
	public void scheduleJob(Job job) {
		
		initializeParameters(job);	//(sensor)job data
		
		try {
			initializeScheduler();
			log.info("Scheduler initialized = {}", scheduler != null);
			
			JobDataMap data = new JobDataMap();
			data.put("procedures", procedures);
			data.put("observedProperties", observedProperties);
			data.put("offerings", offerings);
			data.put("featureIdentifiers", featureIdentifiers);
			data.put("date", new DateTime(2012, 11, 19, 12, 0, 0)); // set fake past date for test

			JobDetail jobDetail = prepareJob(job, data);
			
			Trigger trigger = prepareTrigger(job);
			
			scheduler.scheduleJob(jobDetail, trigger);	//execute SensorWebJob's execute()

		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

	private void initializeParameters(Job job) {
		for(AbstractSubsetDefinition subset : job.getInputs()) {
			if(subset instanceof SensorWebSubsetDefinition) {
				SensorWebSubsetDefinition senSubset = (SensorWebSubsetDefinition) subset;
				procedures.add(senSubset.getProcedure());
				observedProperties.add(senSubset.getObservedProperty());
				offerings.add(senSubset.getOffering());
				featureIdentifiers.add(senSubset.getFeatureOfInterest());
			}
		}
	}

	private Trigger prepareTrigger(Job job) {
		log.info("building new Trigger");
		return TriggerBuilder.newTrigger()
				.withIdentity(job.getId(), job.getName())
				.startNow()
				.withSchedule(SimpleScheduleBuilder.simpleSchedule()
						.repeatForever()
						.withIntervalInSeconds(Integer.parseInt(job.getTimeInterval())))
				.build();
	}

	
	private JobDetail prepareJob(Job job, JobDataMap data) {
		log.info("preparing Job, observer in jobDataMap = {}", data.get("observer"));
		return JobBuilder.newJob(SensorWebJob.class)
				.withIdentity(job.getId(), job.getName())
				.usingJobData(data)
				.build();
	}

	private void initializeScheduler() throws SchedulerException {
		scheduler = new StdSchedulerFactory(QUARTZ_JOB_PROPERTIES).getScheduler();
		scheduler.start();
	}

}
