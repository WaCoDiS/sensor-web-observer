package de.wacodis.observer.core;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.quartz.JobDataMap;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.AbstractSubsetDefinitionTemporalCoverage;
import de.wacodis.observer.model.AbstractWacodisJobExecutionEvent;
import de.wacodis.observer.model.SingleJobExecutionEvent;
import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.observer.model.WacodisJobDefinitionExecution;
import de.wacodis.observer.model.WacodisJobDefinitionTemporalCoverage;
import de.wacodis.observer.model.AbstractWacodisJobExecutionEvent.EventTypeEnum;

public class WacodisJobConfiguration {

	static JobDataMap configureFirstDataQueryPeriod(WacodisJobDefinition job, JobDataMap data_cloned,
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
        	

            data_cloned = configureFirstDataQueryPeriod_singleExecutionJob(job, data_cloned,
        			subsetDefinition, (SingleJobExecutionEvent) event);
        } else {
        	data_cloned = configureFirstDataQueryPeriod_regularExecutionJob(job, data_cloned,
        			subsetDefinition);
        }
    	
    	
    	return data_cloned;
	}
	
	private static JobDataMap configureFirstDataQueryPeriod_regularExecutionJob(WacodisJobDefinition job, JobDataMap data_cloned,
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
		
		DateTime temporalCoverageStartDate = computeTemporalCoverageStartDate_regularJobs(temporalCoverageEndDate, duration, offset, job);
		
		data_cloned = setTemporalCoverage(data_cloned, temporalCoverageStartDate, temporalCoverageEndDate);
		
        return data_cloned;
	};

	private static DateTime computeTemporalCoverageStartDate_regularJobs(DateTime temporalCoverageEndDate, String duration,
			String offset, WacodisJobDefinition job) {
		Boolean previousExecution = job.getTemporalCoverage().getPreviousExecution();
		
		if (previousExecution != null && previousExecution) {
			CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
			
			ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(job.getExecution().getPattern()));
			
			Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(temporalCoverageEndDate.toGregorianCalendar().toZonedDateTime());
			return new DateTime(lastExecution.get().toInstant().toEpochMilli());
		}
		else {
			return computeTemporalCoverageStartDate(temporalCoverageEndDate, duration, offset);
		}
	};

	private static DateTime getTemporalCoverageEndDate_regularJob(WacodisJobDefinition job) {	
		
		/*
		 * consider previous execution 
		 * if present no duration is available
		 * 
		 * anhand pattern ableiten, was ist der nächste Ausführungszeitpunkt 
		 * 
		 * (bsp. 25.11. angelegt aber Ausführung einmal monatlich am 1. --> 1.12. erste Ausführung)
		 * 
		 * mit cron-utils
		 */
		
		DateTime startAt = job.getExecution().getStartAt();
		
		if(startAt == null) {
			startAt = job.getCreated();
		}
		
		CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
				
		ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(job.getExecution().getPattern()));
		Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(startAt.toGregorianCalendar().toZonedDateTime());
		
		return new DateTime(nextExecution.get().toInstant().toEpochMilli());
		
	};

	private static JobDataMap configureFirstDataQueryPeriod_singleExecutionJob(WacodisJobDefinition job, JobDataMap data_cloned,
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

	private static JobDataMap setTemporalCoverage(JobDataMap data_cloned, DateTime temporalCoverageStartDate,
			DateTime temporalCoverageEndDate) {
		data_cloned.put(TemporalCoverageConstants.START_DATE, temporalCoverageStartDate.toString());
		data_cloned.put(TemporalCoverageConstants.END_DATE, temporalCoverageEndDate.toString());
		
		return data_cloned;
	};

	private static DateTime computeTemporalCoverageStartDate(DateTime temporalCoverageEndDate, String duration,
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

	private static String getAssociatedDurationOffset(WacodisJobDefinition job, AbstractSubsetDefinition subsetDefinition) {
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

	private static String getAssociatedDuration(WacodisJobDefinition job, AbstractSubsetDefinition subsetDefinition) {
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

}
