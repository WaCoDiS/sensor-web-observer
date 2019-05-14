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
	 */
	void initializeParameters(WacodisJobDefinition job, JobDataMap data);

	/**
	 * Build new JobDetail
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
	 * @return JobDetail using job's data within JobDataMap
	 */
	JobDetail prepareJob(WacodisJobDefinition job, JobDataMap data);
}
