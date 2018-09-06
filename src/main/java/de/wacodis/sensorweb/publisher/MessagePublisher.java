package de.wacodis.sensorweb.publisher;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding(PublishChannels.class)
public class MessagePublisher implements Serializable{
	
}
