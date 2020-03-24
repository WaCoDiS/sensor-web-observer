package de.wacodis.observer.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.WacodisJobDefinition;

/**
 * Factory-Pattern Interface for SubsetDefinitionJobs
 */
public interface JobFactory {

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
        };

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
        };
        
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
		
		Collection<JobDetail> quartzJobs = new ArrayList<JobDetail>(inputList.size());
		
		for (AbstractSubsetDefinition subsetDefinition : inputList) {
			
			JobDataMap data_cloned = (JobDataMap) data.clone();
			String jobId = generateSubsetSpecificIdentifier(subsetDefinition);
			String jobGroupName = generateGroupName(job, subsetDefinition);
			quartzJobs.add(initializeJob(job, data_cloned, subsetDefinition, jobId, jobGroupName));
		}
		
		return quartzJobs;		
	}

	/**
	 * Pack business logic data into JobDataMap's data package and build new JobDetail
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
	 * @param subsetDefinition - the concrete subsetDefinition, for which the job shall be generated
	 * @param jobGroupName - the job group name to use for quartz JobDetail
	 * @param jobId - the jobId to use for quartz JobDetail
	 * @return JobDetail using job's data within JobDataMap
	 */   
	JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data, AbstractSubsetDefinition subsetDefinition, String jobId, String jobGroupName);

	/**
	 * Method to filter the WACODIS job inputs relevant for the respective JobFactory instance
	 * @param job - instance of a WACODIS job
	 * @return
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
		return job.getName();
	};
}
