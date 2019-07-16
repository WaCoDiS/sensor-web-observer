/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd;

import de.wacodis.observer.core.JobFactory;
import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.DwdSubsetDefinition;
import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.sentinel.SentinelJob;

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
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdJobFactory implements JobFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DwdJobFactory.class);

    @Override
    public boolean supportsJobDefinition(WacodisJobDefinition job) {
        Optional<AbstractSubsetDefinition> def = job.getInputs().stream()
                .filter(in -> in instanceof DwdSubsetDefinition).findAny();

        return def.isPresent();
    }

    @Override
    public JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data) {
        LOG.info("Preparing SentinelJob JobDetail");

        Optional<AbstractSubsetDefinition> defOpt = job.getInputs().stream()
                .filter((i -> i instanceof DwdSubsetDefinition)).findFirst();

        // this should always be the case
        if (defOpt.isPresent()) {
            DwdSubsetDefinition def = (DwdSubsetDefinition) defOpt.get();
            
            //Put all required request parameters into JobDataMap
            
            //data.put(DwdJob.LAYER_NAME_KEY, def.getLayerName());
            data.put("version", "2.0.0");
            data.put("layerName", def.getLayerName());
            data.put("serviceUrl", def.getServiceUrl());
             
            // Set Job execution interval depending on DWD Layer
            
            data.put("executionInterval", 60*60*24);
            data.put("executionTemporalCoverage", job.getTemporalCoverage());
            data.put("executionArea", job.getAreaOfInterest());
            
            // string temporalcoverage to period
            if (job.getTemporalCoverage() != null && !StringUtils.isEmpty(job.getTemporalCoverage().getDuration())) {
                Period period = ISOPeriodFormat.standard().parsePeriod(job.getTemporalCoverage().getDuration());
                int baseDays = period.getDays();
                if (period.getHours() > 11) {
                    // round to full days
                    baseDays++;
                }
                data.put(DwdJob.PREVIOUS_DAYS_KEY, baseDays);
            }

        }
        return JobBuilder.newJob(DwdJob.class)
                .withIdentity(job.getId().toString(), job.getName())
                .usingJobData(data)
                .build();
    }

}
