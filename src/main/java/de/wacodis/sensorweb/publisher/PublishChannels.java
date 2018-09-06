package de.wacodis.sensorweb.publisher;

import java.io.Serializable;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface PublishChannels extends Serializable{
	
	String DATAENVELOPE_OUTPUT = "outputDataEnvelope";
	
	@Output(DATAENVELOPE_OUTPUT)
	MessageChannel sendDataEnvelope();

}
