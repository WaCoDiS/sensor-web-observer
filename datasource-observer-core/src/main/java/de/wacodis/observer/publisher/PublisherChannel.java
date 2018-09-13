package de.wacodis.observer.publisher;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

public interface PublisherChannel {
	
	String DATAENVELOPE_OUTPUT = "outputDataEnvelope";
	
	@Output(DATAENVELOPE_OUTPUT)
	MessageChannel sendDataEnvelope();

}
