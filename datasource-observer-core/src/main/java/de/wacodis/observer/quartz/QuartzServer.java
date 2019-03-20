package de.wacodis.observer.quartz;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wacodis.observer.publisher.PublisherChannel;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Component
public class QuartzServer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(QuartzServer.class);

    public static final String PUBLISHER = "PUBLISHER";

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

}
