package de.wacodis.sensorweb.job;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wacodis.observer.config.ExecutionIntervalConfig;
import de.wacodis.observer.core.JobFactory;
import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.SensorWebSubsetDefinition;
import de.wacodis.observer.model.WacodisJobDefinition;

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
//	@Override
//	public void initializeParameters(WacodisJobDefinition job, JobDataMap data) {
//		for (AbstractSubsetDefinition subset : job.getInputs()) {
//			if (subset instanceof SensorWebSubsetDefinition) {
//				SensorWebSubsetDefinition senSubset = (SensorWebSubsetDefinition) subset;
//				data.put("procedures", Collections.singletonList(senSubset.getProcedure()));
//				data.put("observedProperties", Collections.singletonList(senSubset.getObservedProperty()));
//				data.put("offerings", Collections.singletonList(senSubset.getOffering()));
//				data.put("featureIdentifiers", Collections.singletonList(senSubset.getFeatureOfInterest()));
//				data.put("serviceURL", senSubset.getServiceUrl());
//				data.put("executionInterval", intervalConfig.getSensorWeb());
//			}
//		}
//	}
//
//	@Override
//	public JobDetail prepareJob(WacodisJobDefinition job, JobDataMap data) {
//		log.info("Preparing JobDetail");
//
//		return JobBuilder.newJob(SensorWebJob.class)
//				.withIdentity(job.getId().toString(), job.getName())
//				.usingJobData(data)
//				.build();
//	}

	@Override
	public JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data, AbstractSubsetDefinition subsetDefinition,
			String jobId, String jobGroupName) {
		if (subsetDefinition instanceof SensorWebSubsetDefinition) {
			SensorWebSubsetDefinition senSubset = (SensorWebSubsetDefinition) subsetDefinition;
			data.put("procedures", Collections.singletonList(senSubset.getProcedure()));
			data.put("observedProperties", Collections.singletonList(senSubset.getObservedProperty()));
			data.put("offerings", Collections.singletonList(senSubset.getOffering()));
			data.put("featureIdentifiers", Collections.singletonList(senSubset.getFeatureOfInterest()));
			data.put("serviceURL", senSubset.getServiceUrl());
			data.put("executionInterval", intervalConfig.getSensorWeb());
		}
		
		log.info("Preparing JobDetail");

		return JobBuilder.newJob(SensorWebJob.class)
				.withIdentity(jobId, jobGroupName)
				.usingJobData(data)
				.build();
	}

	@Override
	public Stream<AbstractSubsetDefinition> filterJobInputs(WacodisJobDefinition job) {
		return job.getInputs().stream()
				.filter(in -> in instanceof SensorWebSubsetDefinition);
	}

	@Override
	public String generateSubsetSpecificIdentifier(AbstractSubsetDefinition subsetDefinition) {
		StringBuilder builder = new StringBuilder("");
		
		// TODO check ID generation --> what parameters shall be used?
		
		if(subsetDefinition instanceof SensorWebSubsetDefinition){
			SensorWebSubsetDefinition senDef = (SensorWebSubsetDefinition) subsetDefinition;
			builder.append(senDef.getSourceType());
			
			if(senDef.getProcedure() != null){
				builder.append("_" + Collections.singletonList(senDef.getProcedure()));
			}
			if(senDef.getObservedProperty() != null){
				builder.append("_" + Collections.singletonList(senDef.getObservedProperty()));
			}
			if(senDef.getOffering() != null){
				builder.append("_" + Collections.singletonList(senDef.getOffering()));
			}
			if(senDef.getFeatureOfInterest() != null){
				builder.append("_" + Collections.singletonList(senDef.getFeatureOfInterest()));
			}
			if(senDef.getServiceUrl() != null){
				builder.append("_" + Collections.singletonList(senDef.getServiceUrl()));
			}
		}
		
		return builder.toString();
	}

}
