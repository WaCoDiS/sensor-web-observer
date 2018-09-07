package de.wacodis.sensorweb.scheduler;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wacodis.sensorweb.publisher.PublishChannels;

@Component
public class QuartzServer implements InitializingBean{
	
	
	private static final Logger log = LoggerFactory.getLogger(QuartzServer.class);

	
	private final String QUARTZ_PROPERTIES = "src/main/resources/quartzServer.properties";
	public static final String PUBLISHER = "PUBLISHER";
	
	private Scheduler scheduler;
	private SchedulerFactory schedulerFactory;
	
	@Autowired
	private PublishChannels publisher;
	
	@Override
	public void afterPropertiesSet() {
		try {
			schedulerFactory = new StdSchedulerFactory(QUARTZ_PROPERTIES);
			scheduler = schedulerFactory.getScheduler();
			
			log.info("QuartzServer calls scheduler.start()");
			
			scheduler.getContext().put(PUBLISHER, publisher);
			
			scheduler.start();
			
		} catch (SchedulerException e) {
			log.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	// execute SensorWebJob's execute()
	public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
		return this.scheduler.scheduleJob(jobDetail, trigger);
	}
	

}
