package de.wacodis.observer.core;

import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.WacodisJobDefinition;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory-Pattern Interface for SubsetDefinitionJobs
 */
public interface JobFactory {
	
	static final Logger LOG = LoggerFactory.getLogger(JobFactory.class);

	/**
	 * Checks if a Job's inputDefinition is supported
	 * @param job - Instance of a new Job
	 * @return true if job's inputDefinition is supported
	 */
	boolean supportsJobDefinition(WacodisJobDefinition job);
	
	/**
	 * Pack business logic data into JobDataMap's data package
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
         * @deprecated replaced by #initializeJob
	 */
        @Deprecated
        default void initializeParameters(WacodisJobDefinition job, JobDataMap data) {
        }

	/**
	 * Build new JobDetail
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
	 * @return JobDetail using job's data within JobDataMap
         * @deprecated replaced by #initializeJob
	 */
        @Deprecated
        default JobDetail prepareJob(WacodisJobDefinition job, JobDataMap data) {
            return null;
        }
        
	/**
	 * Pack business logic data into JobDataMap's data package and build new JobDetail
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
	 * @return JobDetail using job's data within JobDataMap
	 */        
        @Deprecated
        default JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data) {
            initializeParameters(job, data);
            return prepareJob(job, data);
        }

    /**    
     * Pack business logic data into JobDataMap's data package and build new JobDetail object for each associated 
     * input of the WACODIS job
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
	 * @return Collection of JobDetail objects using job's data within JobDataMap
     */
	default Collection<JobDetail> initializeJobs(WacodisJobDefinition job, JobDataMap data){
		Stream<AbstractSubsetDefinition> wacodisJobInputs = filterJobInputs(job);
		
		List<AbstractSubsetDefinition> inputList = wacodisJobInputs.collect(Collectors.toList());
		
		LOG.info("Build and inspect a total number of {} quartz jobs for WACODIS job with id {} and SubsetDefinition of type {}", inputList.size(), job.getId(), inputList.get(0).getClass().getSimpleName());
		
		Collection<JobDetail> quartzJobs = new ArrayList<>(inputList.size());
		
		for (AbstractSubsetDefinition subsetDefinition : inputList) {
			
			JobDataMap data_cloned = (JobDataMap) data.clone();
			String jobId = generateSubsetSpecificIdentifier(subsetDefinition);
			String jobGroupName = generateGroupName(job, subsetDefinition);
			
			// TODO FIXME take care of areaOfInterest from JobDataMap --> maybe as part of the job key
			List<Float> areaOfInterestExtent = job.getAreaOfInterest().getExtent();
			String extent = areaOfInterestExtent.stream()
					.map(String::valueOf)
					.collect(Collectors.joining(","));
			jobId = String.join("_", jobId, extent);
			
			LOG.debug("Initialize a potential quartz job with jobId '{}' and groupName '{}' for input subsetDefinition \n{}", jobId, jobGroupName, subsetDefinition);
			JobBuilder jobBuilder = initializeJobBuilder(job, data_cloned, subsetDefinition);
			// store durably is required in order to replace an already existing job in scheduler to update its jobDataMap
			JobDetail quartzJob = jobBuilder.withIdentity(jobId, jobGroupName).storeDurably(true).build();
			quartzJobs.add(quartzJob);
		}
		
		return quartzJobs;		
	}

	/**
	 * Pack business logic data into JobDataMap's data package and build new JobDetail
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
	 * @return JobBuilder using job's data within JobDataMap
	 */   
	JobBuilder initializeJobBuilder(WacodisJobDefinition job, JobDataMap data, AbstractSubsetDefinition subsetDefinition);

	/**
	 * Method to filter the WACODIS job inputs relevant for the respective JobFactory instance
	 * @param job - instance of a WACODIS job
	 * @return Stream of {@link AbstractSubsetDefinition}
	 */
	Stream<AbstractSubsetDefinition> filterJobInputs(WacodisJobDefinition job);
	
	/**
	 * Generates an identifier that encodes a set of important SubsetDefinition specific propeties. 
	 * It may be used as quartz job identifier in order to ensure that only one quartz job per unique data source type exists
	 * @param subsetDefinition - the concrete subsetDefinition, for which the identifier shall be generated
	 * @return identifier encoding SubsetDefinition specific properties
	 */
	String generateSubsetSpecificIdentifier(AbstractSubsetDefinition subsetDefinition);
	
	/**
	 *  Generates an group name for quartz job definition 
	 * @param job job - instance of a WACODIS job
	 * @param subsetDefinition - the concrete subsetDefinition, for which the identifier shall be generated
	 * @return quartz job group name
	 */
	default String generateGroupName(WacodisJobDefinition job, AbstractSubsetDefinition subsetDefinition) {
		
		// we only return a constant value here as we want to construct quartz job keys that are build only from subsetDefinition specific properties
		
		// that is necessary in order to compare different jobKeys fr different WACODIS jobs with same subsetDefinitions 
		
		// the overall goal is to only have one quartz job per unique subsetDefinition
		
		// a WACODIS job specific group name (e.g. job.getName()) would destroy the possibility to only have one quartz job per unique subsetDefinition
		return "GROUP";
	}
}
