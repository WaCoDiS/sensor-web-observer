package de.wacodis.sensorweb.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import de.wacodis.api.model.AbstractSubsetDefinition;
import de.wacodis.api.model.Job;
import de.wacodis.api.model.SensorWebSubsetDefinition;
import de.wacodis.sensorweb.scheduler.SensorWebJobScheduler;

@EnableBinding(Channels.class)
public class MessageListener {

	private SensorWebJobScheduler jobScheduler;

	private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

	@StreamListener(Channels.JOBCREATION_INPUT)
	public void receiveNewJob(Job newJob) {
		log.info("new job received \n{}", newJob);
		
		if(checkSensorWebSubsetDefinition(newJob)) {
			jobScheduler = new SensorWebJobScheduler();
			log.info("JobScheduler calls scheduleJob()");
			jobScheduler.scheduleJob(newJob);
		}
		//here: possiblility to check for other SubsetDefinitions in job
		
	}
	
	private boolean checkSensorWebSubsetDefinition(Job job) {
		for(AbstractSubsetDefinition subset : job.getInputs()) {
			if(subset instanceof SensorWebSubsetDefinition) {
				return true;
			}
		}
		return false;
	}
}
