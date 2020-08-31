package de.wacodis.observer.quartz;

import java.util.Collection;
import java.util.UUID;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wacodis.observer.core.JobFactory;
import de.wacodis.observer.model.AbstractWacodisJobExecutionEvent;
import de.wacodis.observer.model.AbstractWacodisJobExecutionEvent.EventTypeEnum;
import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.observer.model.WacodisJobDefinitionExecution;
import de.wacodis.observer.model.WacodisJobDefinitionTemporalCoverage;
import exception.InvalidWacodisJobParameterException;

@Component
public class JobScheduler {
    
    private static final String SINGLE_TIME_EXECUTION_SUFFIX = "_ONCE";


	private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);


    @Autowired
    private QuartzServer wacodisQuartz;

    public JobScheduler() {
    }

    public void scheduleJob(WacodisJobDefinition job, JobFactory factory) {   	    	    	
        try {
        	
        	// first validate temporalCoverage
        	validateJobParameters(job);
        	
        	// generate all Quartz job definitions for each WACODIS job input (SubsetDefinition)
        	
        	Collection<JobDetail> quartzJobDefinitions = generateQuartzJobDefinitions(job, factory);  
        	
        	// if job queries data from the past, then execute each job ONCE to ensure that queried data exists
        	if(queriesDataFromThePast(job)){
        		// execute each quartz job definition one single time
        		LOG.info("WACODIS job has a duration temporalCoverage specification. Thus it queries data from the past. Hence each generated quartz job instance will be executed once before scheduling management in order to ensure that queried temporal coverage for associated data source is retrieved.");
        		
        		executeQuartzJobsOnce(quartzJobDefinitions);
        	}
        	
        	// then for each Quartz job definition 
    		/* 
    		 * manage Quartz job instances;
    		 * 
    		 * if is a single time execution, then skip
    		 * 
    		 * else
    		 * 		check if existing jobs already watch the same set of queried data
    		 * 		if so just update JobDataMap with WACODIS ID
    		 * 		else generate and execute new job & trigger for scheduling
    		 */
			WacodisJobDefinitionExecution execution = job.getExecution();
			AbstractWacodisJobExecutionEvent event = execution.getEvent();
			if(event != null && event.getEventType().equals(EventTypeEnum.SINGLEJOBEXECUTIONEVENT)){
				// specification of a job, that shall only be executed once
				// e.g. for on demand jobs
				
				// in this case, the job was already executed and thus, we may skip the rest				
				
				//TODO what if single time job shall start in the future?
				return;
			}
			else{
				manageQuartzJobDefinitions_onAddNewWacodisJob(job, quartzJobDefinitions);
			}
        } catch (SchedulerException e) {
            LOG.warn(e.getClass() + ": " + e.getMessage());
            LOG.debug(e.getClass() + ": " + e.getMessage(), e);
            e.printStackTrace();
        } catch (InterruptedException e) {
        	LOG.warn(e.getClass() + ": " + e.getMessage());
            LOG.debug(e.getClass() + ": " + e.getMessage(), e);
            e.printStackTrace();
		} catch (Exception e) {
			LOG.warn(e.getClass() + ": " + e.getMessage());
            LOG.debug(e.getClass() + ": " + e.getMessage(), e);
            e.printStackTrace();
		}
    }
    
    private void validateJobParameters(WacodisJobDefinition job) throws Exception{
		// check job parameters to ensure that all necessary parameters are set (depending on the type of job)
    	LOG.info("Start validation of WacodisJobDefinition.");
		validateTemporalCoverage(job);
		
		LOG.info("WacodisJobDefinition validation succeeded without issues.");
	}

	private void validateTemporalCoverage(WacodisJobDefinition job) throws InvalidWacodisJobParameterException {
		// bei SingleExecutionEvent muss duration angegeben werden
		//  wenn previousExecution==true muss execution pattern angegeben werden
		
		WacodisJobDefinitionTemporalCoverage temporalCoverage = job.getTemporalCoverage();
		WacodisJobDefinitionExecution execution = job.getExecution();
		AbstractWacodisJobExecutionEvent event = execution.getEvent();
		if(event != null && event.getEventType().equals(EventTypeEnum.SINGLEJOBEXECUTIONEVENT)) {
			// make sure that duration property is set
			String durationString = temporalCoverage.getDuration();
			if(durationString == null || durationString.isEmpty()) {
				throw new InvalidWacodisJobParameterException("Wacodis job of type '" + EventTypeEnum.SINGLEJOBEXECUTIONEVENT + "' is missing required parameter value for parameter 'temporalCoverage.duration'");
			}
			if(! isValidIso8601DurationString(durationString)) {
				throw new InvalidWacodisJobParameterException("Wacodis job of type '" + EventTypeEnum.SINGLEJOBEXECUTIONEVENT + "' has invalid parameter value for parameter 'temporalCoverage.duration'. The value of '" + durationString + "' cannot be parsed as ISO8601 duration");				
			}
		}
		if (temporalCoverage.getPreviousExecution()) {
			// make sure that pattern execution is set
			String patternString = execution.getPattern();
			if(patternString == null || patternString.isEmpty()) {
				throw new InvalidWacodisJobParameterException("Wacodis job of type 'pattern execution' with 'previousExecution=true' is missing required parameter value for parameter 'execution.pattern'");
			}
			if(! isValidCronPatternString(patternString)) {
				throw new InvalidWacodisJobParameterException("Wacodis job of type 'pattern execution' with 'previousExecution=true' has invalid parameter value for parameter 'execution.pattern'. The value of '" + patternString + "' cannot be parsed as CRON pattern");				
			}
		}
		// case no previosExecution and not event of type SingleTimeExecution
		if (! temporalCoverage.getPreviousExecution() && (event == null || !event.getEventType().equals(EventTypeEnum.SINGLEJOBEXECUTIONEVENT))) {
			// make sure that pattern execution is set
			String patternString = execution.getPattern();
			if(patternString == null || patternString.isEmpty()) {
				throw new InvalidWacodisJobParameterException("Wacodis job of type 'pattern execution' with 'previousExecution=false' is missing required parameter value for parameter 'execution.pattern'");
			}
			if(! isValidCronPatternString(patternString)) {
				throw new InvalidWacodisJobParameterException("Wacodis job of type 'pattern execution' with 'previousExecution=false' has invalid parameter value for parameter 'execution.pattern'. The value of '" + patternString + "' cannot be parsed as CRON pattern");				
			}
			
			// make sure that duration property is set
			String durationString = temporalCoverage.getDuration();
			if(! isValidIso8601DurationString(durationString)) {
				throw new InvalidWacodisJobParameterException("Wacodis job of type 'pattern execution' with 'previousExecution=false' has invalid parameter value for parameter 'temporalCoverage.duration'. The value of '" + durationString + "' cannot be parsed as ISO8601 duration");				
			}
		}
	}

	private boolean isValidCronPatternString(String patternString) {
		boolean isValidCronPattern = false;
		
		try {
			CronExpression cronExpr = new CronExpression(patternString);
			isValidCronPattern = true;
		} catch (Exception e) {
			LOG.debug("Error on parsing patternString '{}' as CRON pattern. Error message is:\n'{}'", patternString, e.getMessage());
		}
		
		return isValidCronPattern;
	}

	private boolean isValidIso8601DurationString(String durationString) {
		// try parse duration String. Expect it to be ISO8601 duration format
		
		// according to https://stackoverflow.com/questions/39411811/parsing-iso-8601-duration-format-to-joda-duration-illegalargumentexception 
		// both possibilities, Duration and Period, should be checked
		boolean isValidIso8601Duration = false;
		try {
			Duration jodaDuration = Duration.parse(durationString);
			isValidIso8601Duration = true;
		} catch (Exception e) {
			LOG.debug("Error on parsing durationString '{}' as Joda Time Duration. Error message is:\n'{}'", durationString, e.getMessage());
		}
		
		try {
			Period jodaPeriod = Period.parse(durationString);
			isValidIso8601Duration = true;
		} catch (Exception e) {
			LOG.debug("Error on parsing durationString '{}' as Joda Time Period. Error message is:\n'{}'", durationString, e.getMessage());
		}
		
		return isValidIso8601Duration;
	}

	public void onDeleteWacodisJob(WacodisJobDefinition wacodisJob, JobFactory factory) {
		/*
		 * when a WACODIS job is deleted we must check if there are running quartz jobs 
		 * for each input of the WACODIS job 
		 * --> within those eisting quartz jobs, we must remove the WACODIS job ID from quartz JobDataMap
		 * 
		 * if there is no remaining WACODIS job ID in quartz JobDataMap --> then pause/unschedule job (remove trigger!)! 
		 */
		
		// generate all Quartz job definitions for each WACODIS job input (SubsetDefinition)
		// they will be used to inspect existing quartz jobs    	 
    	
    	try {
    		Collection<JobDetail> quartzJobDefinitions = generateQuartzJobDefinitions(wacodisJob, factory); 
			manageQuartzJobDefinitions_onDeleteWacodisJob(wacodisJob, quartzJobDefinitions);
		} catch (SchedulerException e) {
			LOG.warn(e.getMessage());
            LOG.debug(e.getMessage(), e);
		}
	}

	private void manageQuartzJobDefinitions_onDeleteWacodisJob(WacodisJobDefinition wacodisJob,
			Collection<JobDetail> quartzJobDefinitions) throws SchedulerException {
		
		for (JobDetail jobDetail : quartzJobDefinitions) {
			if (wacodisQuartz.jobForSameDatasourceAndTypeAlreadyExists(jobDetail)){
				LOG.info("Existing quartz job with the same parameters identified. Will remove WACODIS job ID from its associated WACODIS jobs");
				JobDetail existingQuartzJob = wacodisQuartz.getQuartzJobForWacodisInputDefinition(jobDetail);
				// get Trigger, which has associated Key in case we only want to unschedule job
				Trigger trigger = prepareTrigger(jobDetail);
				wacodisQuartz.removeWacodisJobIdFromQuartzJobDataMap(existingQuartzJob, wacodisJob.getId(), trigger.getKey(), true);				
			}
		}
		
	}

	private void manageQuartzJobDefinitions_onAddNewWacodisJob(WacodisJobDefinition job, Collection<JobDetail> quartzJobDefinitions) throws SchedulerException {
		
		/**
		 * check if existing jobs already watch the same set of queried data
    	 * 		if so just update JobDataMap with WACODIS ID
    	 * 		else generate and execute new job & trigger for scheduling
		 */
		
		for (JobDetail jobDetail : quartzJobDefinitions) {
			if (wacodisQuartz.jobForSameDatasourceAndTypeAlreadyExists(jobDetail)){
				LOG.info("Existing quartz job with the same parameters identified. Will add WACODIS job ID to its associated WACODIS jobs");
				JobDetail existingQuartzJob = wacodisQuartz.getQuartzJobForWacodisInputDefinition(jobDetail);
				wacodisQuartz.addWacodisJobIdToQuartzJobDataMap(existingQuartzJob, job.getId(), true);
				
				// if the execution-interval, specified in WACODIS job definition, is shorter than the already existing trigger
				// then we might want to adjust the trigger --> shorten execution interval
				adjustTriggerIfIntervalIsShorter(existingQuartzJob, job.getExecution().getPattern());
			}
			else{
				// initialize wacodisJobIdStorage in jobDataMap
				LOG.info("No existing quartz job with the same parameters was found. Will create and schedule a new quartz job and add WACODIS job ID to its associated WACODIS jobs");
				wacodisQuartz.addWacodisJobIdToQuartzJobDataMap(jobDetail, job.getId(), false);
				
				Trigger trigger = prepareTrigger(jobDetail);
		        wacodisQuartz.scheduleJob(jobDetail, trigger);
			}
		}
		
	}

	private void adjustTriggerIfIntervalIsShorter(JobDetail existingQuartzJob, String pattern) throws SchedulerException {
		// TODO FIXME implement
		LOG.info("Adjustment of Triggers is currently not implemented.");
		
		/*
		 * it might happen, that an existing job was just paused!
		 * 
		 * TODO detect that and if it is state=paused/unscheduled, then we might just define a new trigger
		 */
		
//		CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(pattern);
//		
//		
//		Trigger associatedTrigger = wacodisQuartz.getAssociatedTrigger(existingQuartzJob);
		
		return;
		
	}

	private void executeQuartzJobsOnce(Collection<JobDetail> quartzJobDefinitions) throws SchedulerException, InterruptedException {
		for (JobDetail jobDetail : quartzJobDefinitions) {
			// create a jonCopy in order to adjust the quartz job KEY definition for single time execution
			JobKey key = jobDetail.getKey();
			JobKey key_singleTime = generateUniqueSingleTimeExecutionJobKey(key);
			JobDetail jobDetail_singleTimeCopy = (JobDetail) jobDetail.clone();
			
			jobDetail_singleTimeCopy = jobDetail_singleTimeCopy.getJobBuilder().withIdentity(key_singleTime).build();
			
			Trigger oneTimeTrigger = prepareSingleExecutionTrigger(jobDetail);
			
			LOG.info("Scheduling new one time execution job with jobID {} and groupName {}", key_singleTime.getName(), key_singleTime.getGroup());
			wacodisQuartz.scheduleJob(jobDetail_singleTimeCopy, oneTimeTrigger);			
		}
		
	}

	private JobKey generateUniqueSingleTimeExecutionJobKey(JobKey key) {
		
		// make a single time job key unique by adding a unique random UUID
		
		/*
		 * this can be necessary as a single time job might be startet immediately or
		 * in future (i.e. 1 year in the future). Hence the job must exist that long in order to be 
		 * triggered!
		 * 
		 * --> hence it must have a unique jobKey
		 */
		UUID uuid = UUID.randomUUID();
		
		JobKey key_singleTime = new JobKey(key.getName() + "_" + uuid + SINGLE_TIME_EXECUTION_SUFFIX, 
				key.getGroup()  + "_" + uuid + SINGLE_TIME_EXECUTION_SUFFIX);
		return key_singleTime;
	}

    private Trigger prepareSingleExecutionTrigger(JobDetail jobDetail) {
    	
    	//TODO what if single time job shall start in the future?
    	
    	return TriggerBuilder.newTrigger()
    			.withIdentity(jobDetail.getKey().getName() + SINGLE_TIME_EXECUTION_SUFFIX, jobDetail.getKey().getGroup() + SINGLE_TIME_EXECUTION_SUFFIX)
                .startNow()
                .build();
	}

	private boolean queriesDataFromThePast(WacodisJobDefinition job) {		
		WacodisJobDefinitionTemporalCoverage temporalCoverage = job.getTemporalCoverage();
		
		// temporalCoverage can either have property "duration" or "previousExecution"
		
		// only "duration" is used to query data from the past. Hence existance of that property is sufficient
		
		if(isValidIso8601DurationString(temporalCoverage.getDuration())){			
			return true;
		}
		
		return false;
	}

	private Collection<JobDetail> generateQuartzJobDefinitions(WacodisJobDefinition job, JobFactory factory) {  
    	JobDataMap data = new JobDataMap();
        data.put("areaOfInterest", job.getAreaOfInterest());
        
    	Collection<JobDetail> jobDetails = factory.initializeJobs(job, data);
    	
    	return jobDetails;
	}

	private Trigger prepareTrigger(JobDetail jobDetail) {
		
		JobDataMap data = jobDetail.getJobDataMap();
        /**
         * set a default execution interval
         */
        if (!data.containsKey("executionInterval")) {
            data.put("executionInterval", 60 * 60);
        }
        
        LOG.info("Build new Trigger with execution interval: {} seconds", data.get("executionInterval"));

        // use the jobDetails key information for the trigger to "group" them
        
        return TriggerBuilder.newTrigger()
                .withIdentity(jobDetail.getKey().getName().toString(), jobDetail.getKey().getGroup())
                .startNow()
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule().repeatForever()
                        .withIntervalInSeconds(data.getInt("executionInterval")))
                .build();
    }
}
