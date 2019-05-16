package de.wacodis.sensorweb.job;

import java.util.Collections;
import java.util.Optional;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.wacodis.api.model.AbstractSubsetDefinition;
import de.wacodis.api.model.SensorWebSubsetDefinition;
import de.wacodis.api.model.WacodisJobDefinition;
import de.wacodis.observer.config.ExecutionIntervalConfig;
import de.wacodis.observer.core.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class SensorWebJobFactory implements JobFactory {

	private static final Logger log = LoggerFactory.getLogger(SensorWebJobFactory.class);
        
        @Autowired
        private ExecutionIntervalConfig intervalConfig;

	@Override
	public boolean supportsJobDefinition(WacodisJobDefinition job) {
		Optional<AbstractSubsetDefinition> sweDefs = job.getInputs().stream()
				.filter(in -> in instanceof SensorWebSubsetDefinition).findAny();

		return sweDefs.isPresent();
	}

	//access job specific inputs
	@Override
	public void initializeParameters(WacodisJobDefinition job, JobDataMap data) {
		for (AbstractSubsetDefinition subset : job.getInputs()) {
			if (subset instanceof SensorWebSubsetDefinition) {
				SensorWebSubsetDefinition senSubset = (SensorWebSubsetDefinition) subset;
				data.put("procedures", Collections.singletonList(senSubset.getProcedure()));
				data.put("observedProperties", Collections.singletonList(senSubset.getObservedProperty()));
				data.put("offerings", Collections.singletonList(senSubset.getOffering()));
				data.put("featureIdentifiers", Collections.singletonList(senSubset.getFeatureOfInterest()));
				data.put("serviceURL", senSubset.getServiceUrl());
				data.put("executionInterval", intervalConfig.getSensorWeb());
			}
		}
	}

	@Override
	public JobDetail prepareJob(WacodisJobDefinition job, JobDataMap data) {
		log.info("Preparing JobDetail");

		return JobBuilder.newJob(SensorWebJob.class)
				.withIdentity(job.getId().toString(), job.getName())
				.usingJobData(data)
				.build();
	}

}
