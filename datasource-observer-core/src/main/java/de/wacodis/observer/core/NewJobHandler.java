package de.wacodis.observer.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wacodis.api.model.WacodisJobDefinition;

@Component
public class NewJobHandler implements InitializingBean {

	
	private static final Logger log = LoggerFactory.getLogger(NewJobHandler.class);

	@Autowired
	private List<JobFactory> jobFactories;
	
	/**
	 * accepts new Job Instance from MessageBroker
	 * @param job - Instance of new Job
	 * @return corresponding JobFactory to job's inputDefinition
	 */
	public JobFactory receiveJob(WacodisJobDefinition job) {
		JobFactory candidate = null;
		for(JobFactory jobFactory : jobFactories) {
			if(jobFactory.supportsJobDefinition(job)) {
				candidate = jobFactory;
				break;
			}
		}
		return candidate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Candidates: {}", jobFactories);
	}
	
	
	
	
}
