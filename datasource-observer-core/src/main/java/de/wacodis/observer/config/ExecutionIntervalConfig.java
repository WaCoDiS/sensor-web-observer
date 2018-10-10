package de.wacodis.observer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("execution.interval")
public class ExecutionIntervalConfig {

	public static int sensorWeb;
	
	public int getSensorWeb() {
		return sensorWeb;
	}
	public void setSensorWeb(int sensorWeb) {
		this.sensorWeb = sensorWeb;
	}
	
}
