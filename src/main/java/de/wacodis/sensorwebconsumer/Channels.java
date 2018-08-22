package de.wacodis.sensorwebconsumer;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface Channels {
	
	String JOBCREATION_INPUT = "jobCreation";

	@Input(JOBCREATION_INPUT)
	SubscribableChannel jobCreation();
	
}
