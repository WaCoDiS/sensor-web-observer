package de.wacodis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.wacodis.observer.config.ExecutionIntervalConfig;
import de.wacodis.observer.config.ProfileConfig;


@SpringBootApplication
public class DatasourceObserverApplication {
	
	@Autowired
	private ExecutionIntervalConfig executionIntervalConfig;
	@Autowired
	private ProfileConfig profileConfig;

	public static void main(String[] args) {
		SpringApplication.run(DatasourceObserverApplication.class, args);
	}

}
