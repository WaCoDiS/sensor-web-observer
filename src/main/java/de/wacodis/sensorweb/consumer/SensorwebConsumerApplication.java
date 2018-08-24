package de.wacodis.sensorweb.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.wacodis.sensorweb.scheduler.QuartzServer;

@SpringBootApplication
public class SensorwebConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorwebConsumerApplication.class, args);
	}
}
