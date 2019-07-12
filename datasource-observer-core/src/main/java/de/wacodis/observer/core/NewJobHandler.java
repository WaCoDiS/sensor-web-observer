package de.wacodis.observer.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wacodis.observer.model.WacodisJobDefinition;
import java.util.stream.Collectors;

@Component
public class NewJobHandler implements InitializingBean {

	
	private static final Logger log = LoggerFactory.getLogger(NewJobHandler.class);

	@Autowired
	private List<JobFactory> jobFactories;
	
	/**
	 * accepts new Job Instance from MessageBroker
	 * @param job - Instance of new Job
	 * @return corresponding JobFactories to job's inputDefinition
	 */
	public List<JobFactory> receiveJob(WacodisJobDefinition job) {
		return jobFactories.stream()
                        .filter(jf -> jf.supportsJobDefinition(job))
                        .collect(Collectors.toList());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Candidates: {}", jobFactories);
	}
	
	
	
	
}
