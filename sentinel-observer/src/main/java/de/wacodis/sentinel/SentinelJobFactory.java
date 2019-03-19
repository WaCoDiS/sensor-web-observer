package de.wacodis.sentinel;

import de.wacodis.api.model.AbstractSubsetDefinition;
import de.wacodis.api.model.CopernicusSubsetDefinition;
import de.wacodis.api.model.WacodisJobDefinition;
import de.wacodis.observer.core.JobFactory;
import de.wacodis.sentinel.apihub.QueryBuilder;
import java.util.Optional;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * @author matthes rieke
 */
public class SentinelJobFactory implements JobFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelJobFactory.class);

    @Override
    public boolean supportsJobDefinition(WacodisJobDefinition job) {
        return job.getInputs().stream().filter(i -> i instanceof CopernicusSubsetDefinition).count() > 0;
    }

    @Override
    public JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data) {
        LOG.info("Preparing SentinelJob JobDetail");
        
        Optional<AbstractSubsetDefinition> def = job.getInputs().stream()
                .filter((i -> i instanceof CopernicusSubsetDefinition)).findFirst();
        
        // this should always be the case
        if (def.isPresent()) {
            CopernicusSubsetDefinition copDef = (CopernicusSubsetDefinition) def.get();
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
        }

        // create the quartz object
        return JobBuilder.newJob(SentinelJob.class)
                .withIdentity(job.getId().toString(), job.getName())
                .usingJobData(data)
                .build();
    }

}
