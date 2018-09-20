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
import de.wacodis.observer.core.JobFactory;

@Component
public class SensorWebJobFactory implements JobFactory {

	private static final Logger log = LoggerFactory.getLogger(SensorWebJobFactory.class);

	@Override
	public boolean supportsJobDefinition(WacodisJobDefinition job) {
		Optional<AbstractSubsetDefinition> sweDefs = job.getInputs().stream()
				.filter(in -> in instanceof SensorWebSubsetDefinition).findAny();

		return sweDefs.isPresent();
	}

	@Override
	public void initializeParameters(WacodisJobDefinition job, JobDataMap data) {
		for (AbstractSubsetDefinition subset : job.getInputs()) {
			if (subset instanceof SensorWebSubsetDefinition) {
				SensorWebSubsetDefinition senSubset = (SensorWebSubsetDefinition) subset;
				data.put("procedures", Collections.singletonList(senSubset.getProcedure()));
				data.put("observedProperties", Collections.singletonList(senSubset.getObservedProperty()));
				data.put("offerings", Collections.singletonList(senSubset.getOffering()));
				data.put("featureIdentifiers", Collections.singletonList(senSubset.getFeatureOfInterest()));
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
