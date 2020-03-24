package de.wacodis.observer.quartz;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import de.wacodis.observer.publisher.PublisherChannel;

@Component
public class QuartzServer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(QuartzServer.class);

    public static final String PUBLISHER = "PUBLISHER";
    public static final String WACODIS_JOB_ID_STORAGE = "wacodisJobIds";
    

    private Scheduler scheduler;

    @Autowired
    private PublisherChannel publisher;
    
    @Autowired
    private SchedulerFactoryBean schedulerBean;

    @Override
    public void afterPropertiesSet() throws SchedulerException {
        scheduler = schedulerBean.getScheduler();
        scheduler.getContext().put(PUBLISHER, publisher);

        log.info("QuartzServer initialized");
    }

    public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        return this.scheduler.scheduleJob(jobDetail, trigger);	//runs QuartzJob's execute()
    }

	public void removeJobByKey(JobKey key) throws SchedulerException {
		if(this.scheduler.checkExists(key)){
			log.debug("Deleting quartz job for key '{}'", key);
			this.scheduler.deleteJob(key);
		}		
		
	}

	public void removeJobsByKeys(List<JobKey> jobKeys) throws SchedulerException {
		for (JobKey jobKey : jobKeys) {
			this.removeJobByKey(jobKey);
		}
		
	}

	public boolean jobForSameDatasourceAndTypeAlreadyExists(JobDetail jobDetail) throws SchedulerException {
		// this should work, as the jobKey is generated from WACODIS SubsetDefinition 
		// specific properties
		
		// hence the same key is guaranteed to be generated for equal SubsetDefinitions
		return this.scheduler.checkExists(jobDetail.getKey());
	}

	public JobDetail getQuartzJobForWacodisInputDefinition(JobDetail jobDetail) throws SchedulerException {
		JobKey key = jobDetail.getKey();
			
		return this.scheduler.getJobDetail(key);		
	}

	public void addWacodisJobIdToQuartzJobDataMap(JobDetail quartzJob, UUID wacodisJobId, boolean replaceExistingQuartzJob) throws SchedulerException {
		JobDataMap jobDataMap = quartzJob.getJobDataMap();
		
		log.info("Associated WACODIS job management: add WACODIS job ID '{}' to the associated jobs of quartz job with key '{}' .", wacodisJobId, quartzJob.getKey());
		
		
		// implement a storage for associated WACODIS job ids than require the quartz job
		
		// implement as HashSet in order to ensure that WACODIS jobIds cannot be inserted twice
		HashSet<UUID> wacodisJobIds = null;
		
		if(jobDataMap.containsKey(WACODIS_JOB_ID_STORAGE)){
			Object object = jobDataMap.get(WACODIS_JOB_ID_STORAGE);
			if (object instanceof HashSet<?>){
				wacodisJobIds = (HashSet<UUID>) object;
				
				wacodisJobIds.add(wacodisJobId);
			}
		}
		else{
			wacodisJobIds = new HashSet<UUID>();
			wacodisJobIds.add(wacodisJobId);			
		}
		
		log.info("Associated WACODIS job management: the quartz job with key '{}' now has a total number of '{}' associated WACODIS jobs.", quartzJob.getKey(), wacodisJobIds.size());
		
		// modify element in quartz job data map
		jobDataMap.put(WACODIS_JOB_ID_STORAGE, wacodisJobIds);
		
		// now we must replace the existing job innscheduler in order to apply the updated jobDataMap !
		// otherwise the initial jobDataMa will still be used
		if(replaceExistingQuartzJob){
			this.scheduler.addJob(quartzJob, true);
		}		
	}

	public Trigger getAssociatedTrigger(JobDetail existingQuartzJob) throws SchedulerException {		
		
		/*
		 * Currently only one trigger shall exist for the same job
		 * 
		 * so we can simply return the first value
		 */
		List<? extends Trigger> triggersOfJob = this.scheduler.getTriggersOfJob(existingQuartzJob.getKey());
		
		return triggersOfJob.get(0);
	}

}
