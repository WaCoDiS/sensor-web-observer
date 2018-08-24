package de.wacodis.sensorweb.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzServer {
	
	
	private static final Logger log = LoggerFactory.getLogger(QuartzServer.class);

	
	private final String QUARTZ_PROPERTIES = "src/main/resources/quartzServer.properties";
	
	private Scheduler scheduler;
	private SchedulerFactory schedulerFactory;
	
	public void startup() {
		try {
			schedulerFactory = new StdSchedulerFactory(QUARTZ_PROPERTIES);
			scheduler = schedulerFactory.getScheduler();
			
			log.info("QuartzServer calls scheduler.start()");
			
			scheduler.start();
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

}
