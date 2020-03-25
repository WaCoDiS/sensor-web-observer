package de.wacodis.observer.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.observer.core.JobFactory;
import de.wacodis.observer.core.JobHandler;
import de.wacodis.observer.quartz.JobScheduler;
import java.util.List;
import javax.annotation.PostConstruct;

@EnableBinding(ListenerChannel.class)
public class MessageListener {

	@Autowired
	private JobScheduler jobScheduler;
	
	@Autowired
	private JobHandler newJobHandler;

	private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

	@StreamListener(ListenerChannel.JOBCREATION_INPUT)
	public void receiveNewJob(WacodisJobDefinition newJob) {
		log.info("New job received:\n{}", newJob);
		
		List<JobFactory> factories = newJobHandler.getResponsibleJobFactories(newJob);
		
		factories.forEach(factory -> {
                    jobScheduler.scheduleJob(newJob, factory);
                });
	}
	
	@StreamListener(ListenerChannel.JOBDELETION_INPUT)
	public void onDeleteWacodisJob(WacodisJobDefinition wacodisJob) {
		log.info("Received deletion event for WACODIS job with id {}", wacodisJob.getId().toString());
		
		List<JobFactory> factories = newJobHandler.getResponsibleJobFactories(wacodisJob);
		
		factories.forEach(factory -> {
                    jobScheduler.onDeleteWacodisJob(wacodisJob, factory);
                });
	}
        
        @PostConstruct
        public void init() {
//            new Thread(() -> {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                
//                WacodisJobDefinition j = new WacodisJobDefinition();
//                j.setId(UUID.randomUUID());
//                j.setAreaOfInterest(new AbstractDataEnvelopeAreaOfInterest().extent(Arrays.asList(6.9315f, 50.9854f, 7.6071f, 51.3190f)));
//                CopernicusSubsetDefinition cs = new CopernicusSubsetDefinition();
//                cs.setMaximumCloudCoverage(20f);
//                cs.setIdentifier("cs-test");
//                cs.setSatellite(CopernicusSubsetDefinition.SatelliteEnum._2);
//                j.setInputs(Collections.singletonList(cs));
//
//                receiveNewJob(j);
//            }).start();
        }
}
