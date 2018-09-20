package de.wacodis.observer.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import de.wacodis.api.model.Job;
import de.wacodis.observer.core.JobFactory;
import de.wacodis.observer.core.NewJobHandler;
import de.wacodis.observer.quartz.JobScheduler;

@EnableBinding(ListenerChannel.class)
public class MessageListener {

	@Autowired
	private JobScheduler jobScheduler;
	
	@Autowired
	private NewJobHandler newJobHandler;

	private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

	@StreamListener(ListenerChannel.JOBCREATION_INPUT)
	public void receiveNewJob(Job newJob) {
		log.info("New job received:\n{}", newJob);
		
		JobFactory factory = newJobHandler.receiveJob(newJob);
		
		jobScheduler.scheduleJob(newJob, factory);
		
	}
}
