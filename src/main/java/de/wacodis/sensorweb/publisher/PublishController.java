package de.wacodis.sensorweb.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.wacodis.dataaccess.model.SensorWebDataEnvelope;

@RestController
public class PublishController{
	
	private static final Logger log = LoggerFactory.getLogger(PublishController.class);

	@Autowired
	PublishChannels pub;
	
	@RequestMapping("/pub")
	@ResponseBody
	public SensorWebDataEnvelope publish(@RequestBody SensorWebDataEnvelope data) {
		pub.sendDataEnvelope().send(MessageBuilder.withPayload(data).build());
		log.info("Published: \n{}", data);
		return data;
	}
	
}
