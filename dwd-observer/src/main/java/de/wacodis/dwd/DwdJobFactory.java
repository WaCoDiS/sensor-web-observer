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
import java.util.Optional;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            data.put(DwdJob.LAYER_NAME_KEY, def.getLayerName());
            
            //TODO Put all required request parameters into JobDataMap
            
            //TODO Set Job execution interval depending on DWD Layer 
            //(hourly, daily or monthly data providing is possible)
            //data.put("executionInterval", ???);

        }
        return JobBuilder.newJob(DwdJob.class)
                .withIdentity(job.getId().toString(), job.getName())
                .usingJobData(data)
                .build();
    }

}
