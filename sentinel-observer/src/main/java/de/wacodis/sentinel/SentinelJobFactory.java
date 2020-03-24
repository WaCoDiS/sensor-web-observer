package de.wacodis.sentinel;

import java.util.stream.Stream;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import de.wacodis.observer.config.ExecutionIntervalConfig;
import de.wacodis.observer.core.JobFactory;
import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.CopernicusSubsetDefinition;
import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.sentinel.apihub.QueryBuilder;

/**
 *
 * @author matthes rieke
 */
@Component
public class SentinelJobFactory implements JobFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelJobFactory.class);
    
    @Autowired
    private ExecutionIntervalConfig intervalConfig;

    @Override
    public boolean supportsJobDefinition(WacodisJobDefinition job) {
        long count = job.getInputs().stream().filter(i -> i instanceof CopernicusSubsetDefinition).count();
        return count > 0;
    }

//    @Override
//    public JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data) {
//        LOG.info("Preparing SentinelJob JobDetail");
//        
//        Optional<AbstractSubsetDefinition> def = job.getInputs().stream()
//                .filter((i -> i instanceof CopernicusSubsetDefinition)).findFirst();
//        
//        // this should always be the case
//        if (def.isPresent()) {
//            CopernicusSubsetDefinition copDef = (CopernicusSubsetDefinition) def.get();
//            if (copDef.getMaximumCloudCoverage() > 0) {
//                data.put(SentinelJob.MAX_CLOUD_COVERAGE_KEY, copDef.getMaximumCloudCoverage());
//            }
//            
//            switch (copDef.getSatellite()) {
//                case _1:
//                    data.put(SentinelJob.PLATFORM_KEY, QueryBuilder.PlatformName.Sentinel1);
//                    break;
//                case _2:
//                    data.put(SentinelJob.PLATFORM_KEY, QueryBuilder.PlatformName.Sentinel2);
//                    break;
//                case _3:
//                    data.put(SentinelJob.PLATFORM_KEY, QueryBuilder.PlatformName.Sentinel3);
//                    break;
//            }
//            
//            if (job.getTemporalCoverage() != null && !StringUtils.isEmpty(job.getTemporalCoverage().getDuration())) {
//                Period period = ISOPeriodFormat.standard().parsePeriod(job.getTemporalCoverage().getDuration());
//                int baseDays = period.getDays();
//                if (period.getHours() > 11) {
//                    // round to full days
//                    baseDays++;
//                }
//                data.put(SentinelJob.PREVIOUS_DAYS_KEY, baseDays);
//            }
//            
//            data.put("executionInterval", intervalConfig.getSentinel());
//        }
//
//        // create the quartz object
//        return JobBuilder.newJob(SentinelJob.class)
//                .withIdentity(job.getId().toString(), job.getName())
//                .usingJobData(data)
//                .build();
//    }

	@Override
	public JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data,
			AbstractSubsetDefinition subsetDefinition, String jobId, String jobGroupName) {		
		if(subsetDefinition instanceof CopernicusSubsetDefinition){
			
			LOG.info("Preparing SentinelJob JobDetail");
			
			CopernicusSubsetDefinition copDef = (CopernicusSubsetDefinition) subsetDefinition;
			if (copDef.getMaximumCloudCoverage() > 0) {
	            data.put(SentinelJob.MAX_CLOUD_COVERAGE_KEY, copDef.getMaximumCloudCoverage());
	        }
	        
	        switch (copDef.getSatellite()) {
	            case _1:
	                data.put(SentinelJob.PLATFORM_KEY, QueryBuilder.PlatformName.Sentinel1);
	                break;
	            case _2:
	                data.put(SentinelJob.PLATFORM_KEY, QueryBuilder.PlatformName.Sentinel2);
	                break;
	            case _3:
	                data.put(SentinelJob.PLATFORM_KEY, QueryBuilder.PlatformName.Sentinel3);
	                break;
	        }
	        
	        if (job.getTemporalCoverage() != null && !StringUtils.isEmpty(job.getTemporalCoverage().getDuration())) {
	            Period period = ISOPeriodFormat.standard().parsePeriod(job.getTemporalCoverage().getDuration());
	            int baseDays = period.getDays();
	            if (period.getHours() > 11) {
	                // round to full days
	                baseDays++;
	            }
	            data.put(SentinelJob.PREVIOUS_DAYS_KEY, baseDays);
	        }
	        
	        data.put("executionInterval", intervalConfig.getSentinel());

		}
		
		// create the quartz object
	    return JobBuilder.newJob(SentinelJob.class)
	            .withIdentity(jobId, jobGroupName)
	            .usingJobData(data)
	            .build();
		
	}

	@Override
	public Stream<AbstractSubsetDefinition> filterJobInputs(WacodisJobDefinition job) {
		return job.getInputs().stream()
                .filter((i -> i instanceof CopernicusSubsetDefinition));
	}

	@Override
	public String generateSubsetSpecificIdentifier(AbstractSubsetDefinition subsetDefinition) {
		
		StringBuilder builder = new StringBuilder("");
		
		if(subsetDefinition instanceof CopernicusSubsetDefinition){
			CopernicusSubsetDefinition copDef = (CopernicusSubsetDefinition) subsetDefinition;
			builder.append(copDef.getSourceType());
			
			if(copDef.getSatellite() != null){
				builder.append("_" + copDef.getSatellite());
			}
		}
		
		return builder.toString();
	}

}
