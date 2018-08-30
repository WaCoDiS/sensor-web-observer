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
import org.springframework.stereotype.Component;

@Component
public class QuartzServer implements InitializingBean{
	
	
	private static final Logger log = LoggerFactory.getLogger(QuartzServer.class);

	
	private final String QUARTZ_PROPERTIES = "src/main/resources/quartzServer.properties";
	
	private Scheduler scheduler;
	private SchedulerFactory schedulerFactory;
	
	@Override
	public void afterPropertiesSet() {
		try {
			schedulerFactory = new StdSchedulerFactory(QUARTZ_PROPERTIES);
			scheduler = schedulerFactory.getScheduler();
			
			log.info("QuartzServer calls scheduler.start()");
			
			scheduler.start();
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
		return this.scheduler.scheduleJob(jobDetail, trigger);
	}
	

}
