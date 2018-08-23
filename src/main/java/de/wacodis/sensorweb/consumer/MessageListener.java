package de.wacodis.sensorweb.consumer;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import de.wacodis.api.model.AbstractSubsetDefinition;
import de.wacodis.api.model.Job;
import de.wacodis.api.model.SensorWebSubsetDefinition;
import de.wacodis.sensorweb.observer.ObservationObserver;
import de.wacodis.sensorweb.scheduler.SensorWebJob;

@EnableBinding(Channels.class)
public class MessageListener {
	
	private String url = "http://fluggs.wupperverband.de/sos2/sos/soap";
	private List<String> procedures = new ArrayList<>();
	private List<String> observedProperties = new ArrayList<>();
	private List<String> offerings = new ArrayList<>();
	private List<String> featureIdentifiers = new ArrayList<>();
	private ObservationObserver observer;
	
	private String timeIntervall;
	

	private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

	@StreamListener(Channels.JOBCREATION_INPUT)
	public void receiveNewJob(Job newJob) {
		log.info("new job received {}", newJob);
		
		observer = initializeNewObserverInstance(newJob);	// instantiate here to save in JobMapData
		timeIntervall = newJob.getTimeInterval();
		
		JobDataMap data = new JobDataMap();
		data.put("observer", observer);
		data.put("date", new DateTime(2018, 8, 18, 12, 0, 0));	//set fake past date for test
		
		JobDetail jobDetail = JobBuilder.newJob(SensorWebJob.class)
				.withIdentity(newJob.getId(), "SensorWebJobGroup")
				.usingJobData(data)
				.build();
		
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(newJob.getId(), "SensorWebTriggerGroup")
				.startNow()
				.withSchedule(SimpleScheduleBuilder.simpleSchedule()
						.repeatForever()
						.withIntervalInSeconds(Integer.parseInt(timeIntervall)))
				.build();
		
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
		
	}

	private ObservationObserver initializeNewObserverInstance(Job newJob) {
		for(AbstractSubsetDefinition subset : newJob.getInputs()) {
			if(subset instanceof SensorWebSubsetDefinition) {
				SensorWebSubsetDefinition senSubset = (SensorWebSubsetDefinition) subset;
				procedures.add(senSubset.getProcedure());
				observedProperties.add(senSubset.getObservedProperty());
				offerings.add(senSubset.getOffering());
				featureIdentifiers.add(senSubset.getFeatureOfInterest());
			}
		}
		ObservationObserver observer = new ObservationObserver(url, procedures, observedProperties, offerings, featureIdentifiers);
		return observer;
	}
}
