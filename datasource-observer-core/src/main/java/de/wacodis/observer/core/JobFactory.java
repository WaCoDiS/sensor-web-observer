package de.wacodis.observer.core;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import de.wacodis.api.model.Job;

/**
 * Factory-Pattern Interface for SubsetDefinitionJobs
 */
public interface JobFactory {

	/**
	 * Checks if a Job's inputDefinition is supported
	 * @param job - Instance of a new Job
	 * @return true if job's inputDefinition is supported
	 */
	boolean supportsJobDefinition(Job job);
	
	/**
	 * Pack business logic data into JobDataMap's data package
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
	 */
	void initializeParameters(Job job, JobDataMap data);

	/**
	 * Build new JobDetail
	 * @param job - Instance of a new Job
	 * @param data - Contains the business logic
	 * @return JobDetail using job's data within JobDataMap
	 */
	JobDetail prepareJob(Job job, JobDataMap data);
}
