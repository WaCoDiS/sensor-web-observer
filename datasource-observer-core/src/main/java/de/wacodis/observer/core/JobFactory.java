package de.wacodis.observer.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.AbstractSubsetDefinitionTemporalCoverage;
import de.wacodis.observer.model.AbstractWacodisJobExecutionEvent;
import de.wacodis.observer.model.AbstractWacodisJobExecutionEvent.EventTypeEnum;
import de.wacodis.observer.model.SingleJobExecutionEvent;
import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.observer.model.WacodisJobDefinitionExecution;
import de.wacodis.observer.model.WacodisJobDefinitionTemporalCoverage;

/**
 * Factory-Pattern Interface for SubsetDefinitionJobs
 */
public interface JobFactory {

    static final Logger LOG = LoggerFactory.getLogger(JobFactory.class);

    /**
     * Checks if a Job's inputDefinition is supported
     *
     * @param job - Instance of a new Job
     * @return true if job's inputDefinition is supported
     */
    boolean supportsJobDefinition(WacodisJobDefinition job);

    /**
     * Pack business logic data into JobDataMap's data package
     *
     * @param job  - Instance of a new Job
     * @param data - Contains the business logic
     * @deprecated replaced by #initializeJob
     */
    @Deprecated
    default void initializeParameters(WacodisJobDefinition job, JobDataMap data) {
    }

    /**
     * Build new JobDetail
     *
     * @param job  - Instance of a new Job
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
     *
     * @param job  - Instance of a new Job
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
     *
     * @param job  - Instance of a new Job
     * @param data - Contains the business logic
     * @return Collection of JobDetail objects using job's data within JobDataMap
     */
    default Collection<JobDetail> initializeJobs(WacodisJobDefinition job, JobDataMap data) {
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
            data_cloned = configureFirstDataQueryPeriod(job, data_cloned, subsetDefinition);
            JobBuilder jobBuilder = initializeJobBuilder(job, data_cloned, subsetDefinition);
            // store durably is required in order to replace an already existing job in scheduler to update its jobDataMap
            JobDetail quartzJob = jobBuilder.withIdentity(jobId, jobGroupName).storeDurably(true).build();
            quartzJobs.add(quartzJob);
        }

        return quartzJobs;
    }

    default JobDataMap configureFirstDataQueryPeriod(WacodisJobDefinition job, JobDataMap data_cloned,
			AbstractSubsetDefinition subsetDefinition) {
    	
    	/*
    	 * Analyse subsetDefinition and job information to detect and set the query start and end date for the very first data query
    	 * 
    	 * it depends on several aspects such as job type, execution time or job/input specific settings.
    	 * 
    	 * basic idea: determine execution date of job --> determine "backwards" observer settings according to specific WacodisJob type and settings
    	 * --> then consider observing backwards period from execution date and set it as startDate and endDate for first execution
    	 * 
    	 * later executions will consider endDate from previous query
    	 */
    	
    	WacodisJobDefinitionExecution execution = job.getExecution();
        AbstractWacodisJobExecutionEvent event = execution.getEvent();
        if (event != null && event.getEventType().equals(EventTypeEnum.SINGLEJOBEXECUTIONEVENT)) {
            // specification of a job, that shall only be executed once
            // e.g. for on demand jobs
        	

            data_cloned = this.configureFirstDataQueryPeriod_singleExecutionJob(job, data_cloned,
        			subsetDefinition, (SingleJobExecutionEvent) event);
        } else {
        	data_cloned = this.configureFirstDataQueryPeriod_regularExecutionJob(job, data_cloned,
        			subsetDefinition);
        }
    	
    	
    	return data_cloned;
    };

	default JobDataMap configureFirstDataQueryPeriod_regularExecutionJob(WacodisJobDefinition job, JobDataMap data_cloned,
			AbstractSubsetDefinition subsetDefinition) {
		
		/*
		 * Regular Execution Job
		 * check if the startAt parameter is present, which might cause job to be executed in future
		 * 
		 * if not present then use created setting from job level
		 */
		DateTime temporalCoverageEndDate = getTemporalCoverageEndDate_regularJob(job);
		
		String duration = getAssociatedDuration(job, subsetDefinition);
		String offset = getAssociatedDurationOffset(job, subsetDefinition);
		
		DateTime temporalCoverageStartDate = computeTemporalCoverageStartDate(temporalCoverageEndDate, duration, offset);
		
		data_cloned = setTemporalCoverage(data_cloned, temporalCoverageStartDate, temporalCoverageEndDate);
		
        return data_cloned;
	};

	default DateTime getTemporalCoverageEndDate_regularJob(WacodisJobDefinition job) {		
		DateTime startAt = job.getExecution().getStartAt();
		
		if(startAt != null) {
			return startAt;
		}
		else {
			return job.getCreated();
		}
	};

	default JobDataMap configureFirstDataQueryPeriod_singleExecutionJob(WacodisJobDefinition job, JobDataMap data_cloned,
			AbstractSubsetDefinition subsetDefinition, SingleJobExecutionEvent event) {

		/*
		 * Single Execution Job
		 * Event --> temporalCoverageEndDate  --> ist immer das Enddatum der Betrachtung --> duration backwards from that date
		 */
		DateTime temporalCoverageEndDate = event.getTemporalCoverageEndDate();
		
		String duration = getAssociatedDuration(job, subsetDefinition);
		String offset = getAssociatedDurationOffset(job, subsetDefinition);
		
		DateTime temporalCoverageStartDate = computeTemporalCoverageStartDate(temporalCoverageEndDate, duration, offset);
		
		data_cloned = setTemporalCoverage(data_cloned, temporalCoverageStartDate, temporalCoverageEndDate);
		
        return data_cloned;
	};

	default JobDataMap setTemporalCoverage(JobDataMap data_cloned, DateTime temporalCoverageStartDate,
			DateTime temporalCoverageEndDate) {
		data_cloned.put(TemporalCoverageConstants.START_DATE, temporalCoverageStartDate);
		data_cloned.put(TemporalCoverageConstants.END_DATE, temporalCoverageEndDate);
		
		return data_cloned;
	};

	default DateTime computeTemporalCoverageStartDate(DateTime temporalCoverageEndDate, String duration,
			String offset) {
		DateTime temporalCoverageStartDate;
		Period durationPeriod = Period.parse(duration, ISOPeriodFormat.standard());        

        temporalCoverageStartDate = temporalCoverageEndDate.withPeriodAdded(durationPeriod, -1);
        
        // apply additional offset if present
        if(offset != null) {
        	Period offsetPeriod = Period.parse(offset, ISOPeriodFormat.standard());

            temporalCoverageStartDate = temporalCoverageStartDate.withPeriodAdded(offsetPeriod, -1);
        }
        
        return temporalCoverageStartDate;
	};

	default String getAssociatedDurationOffset(WacodisJobDefinition job, AbstractSubsetDefinition subsetDefinition) {
		// use input specific details if available; else use job level details
		String offset = null;
		AbstractSubsetDefinitionTemporalCoverage temporalCoverage_subsetDef = subsetDefinition.getTemporalCoverage();
		WacodisJobDefinitionTemporalCoverage temporalCoverage_job = job.getTemporalCoverage();
		if (temporalCoverage_subsetDef != null) {
			offset = temporalCoverage_subsetDef.getOffset();
		} else {
			offset = temporalCoverage_job.getOffset();
		}

		return offset;
	};

	default String getAssociatedDuration(WacodisJobDefinition job, AbstractSubsetDefinition subsetDefinition) {
		// use input specific details if available; else use job level details
		String duration = null;
		AbstractSubsetDefinitionTemporalCoverage temporalCoverage_subsetDef = subsetDefinition.getTemporalCoverage();
		WacodisJobDefinitionTemporalCoverage temporalCoverage_job = job.getTemporalCoverage();
		if (temporalCoverage_subsetDef != null) {
			duration = temporalCoverage_subsetDef.getDuration();
		} else {
			duration = temporalCoverage_job.getDuration();
		}

		return duration;
	};

	/**
     * Pack business logic data into JobDataMap's data package and build new JobDetail
     *
     * @param job  - Instance of a new Job
     * @param data - Contains the business logic
     * @return JobBuilder using job's data within JobDataMap
     */
    JobBuilder initializeJobBuilder(WacodisJobDefinition job, JobDataMap data, AbstractSubsetDefinition subsetDefinition);

    /**
     * Method to filter the WACODIS job inputs relevant for the respective JobFactory instance
     *
     * @param job - instance of a WACODIS job
     * @return Stream of {@link AbstractSubsetDefinition}
     */
    Stream<AbstractSubsetDefinition> filterJobInputs(WacodisJobDefinition job);

    /**
     * Generates an identifier that encodes a set of important SubsetDefinition specific propeties.
     * It may be used as quartz job identifier in order to ensure that only one quartz job per unique data source type exists
     *
     * @param subsetDefinition - the concrete subsetDefinition, for which the identifier shall be generated
     * @return identifier encoding SubsetDefinition specific properties
     */
    String generateSubsetSpecificIdentifier(AbstractSubsetDefinition subsetDefinition);

    /**
     * Generates an group name for quartz job definition
     *
     * @param job              job - instance of a WACODIS job
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
