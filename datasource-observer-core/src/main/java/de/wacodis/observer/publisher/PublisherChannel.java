package de.wacodis.observer.publisher;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface PublisherChannel {
	
	String DATAENVELOPE_OUTPUT = "output-data-envelope";
	
	@Output(DATAENVELOPE_OUTPUT)
	MessageChannel sendDataEnvelope();

}
