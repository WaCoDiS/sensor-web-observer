package de.wacodis.observer.listener;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface ListenerChannel {
	
	//reference to applicatoin.yml's binding
	String JOBCREATION_INPUT = "jobCreation";

	@Input(JOBCREATION_INPUT)
	SubscribableChannel jobCreation();
	
}
