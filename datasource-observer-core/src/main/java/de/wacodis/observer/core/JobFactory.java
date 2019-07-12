package de.wacodis.observer.core;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;

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
        default JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data) {
            initializeParameters(job, data);
            return prepareJob(job, data);
        }
}
