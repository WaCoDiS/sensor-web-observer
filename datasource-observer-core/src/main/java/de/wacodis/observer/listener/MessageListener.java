package de.wacodis.observer.listener;

import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.api.model.CopernicusSubsetDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import de.wacodis.api.model.WacodisJobDefinition;
import de.wacodis.observer.core.JobFactory;
import de.wacodis.observer.core.NewJobHandler;
import de.wacodis.observer.quartz.JobScheduler;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.PostConstruct;

@EnableBinding(ListenerChannel.class)
public class MessageListener {

	@Autowired
	private JobScheduler jobScheduler;
	
	@Autowired
	private NewJobHandler newJobHandler;

	private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

	@StreamListener(ListenerChannel.JOBCREATION_INPUT)
	public void receiveNewJob(WacodisJobDefinition newJob) {
		log.info("New job received:\n{}", newJob);
		
		List<JobFactory> factories = newJobHandler.receiveJob(newJob);
		
		factories.forEach(factory -> {
                    jobScheduler.scheduleJob(newJob, factory);
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
