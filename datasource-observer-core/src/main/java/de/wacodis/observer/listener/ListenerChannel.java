package de.wacodis.observer.listener;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface ListenerChannel {
	
	//reference to applicatoin.yml's binding
	String JOBCREATION_INPUT = "job-creation";
	
	String JOBDELETION_INPUT = "job-deletion";

	@Input(JOBCREATION_INPUT)
	SubscribableChannel jobCreation();
	
	@Input(JOBDELETION_INPUT)
	SubscribableChannel jobDeletion();
	
}
