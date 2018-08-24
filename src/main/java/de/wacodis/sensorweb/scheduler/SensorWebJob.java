package de.wacodis.sensorweb.scheduler;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.exception.EncodingException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;

import de.wacodis.sensorweb.observer.ObservationObserver;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SensorWebJob implements Job{
	
	
	private static final Logger log = LoggerFactory.getLogger(SensorWebJob.class);

	
	private ObservationObserver observer;
	private List<OmObservation> results;
	private DateTime date;	//fake date in past for test
	

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("---> " + context.toString());
		System.out.println("---> " + context.getMergedJobDataMap().size());
		try {
			log.info("called SensorWebJob's execute()");
			log.info("SensorWebJob's Observer = {}", observer != null);
			
			observer = (ObservationObserver) context.getMergedJobDataMap().get("observer");
//			date = (DateTime) context.getMergedJobDataMap().get("date");
			observer.setDateOfLastObs(new DateTime(2012, 11, 19, 12, 0, 0));		//for test only
			
			if(observer.checkForAvailableUpdates()) {
				observer.updateObservations(observer.getDateOfNextToLastObs(), observer.getDateOfLastObs());
				results = observer.getObservations();
				//-->... notify broker?
				for(OmObservation o : results) {
					System.out.println(o.getValue().getValue());
				}
				//<--
			}
			
			date = observer.getDateOfLastObs();		//for test only
			JobDataMap data = context.getJobDetail().getJobDataMap();
			data.put("observer", observer);	
			data.put("date", date);					//for test only
			
		} catch (EncodingException | DecodingException | XmlException e) {
			e.printStackTrace();
		}
		
	}
	
	//autom. DI via setters by QuartzScheduler for JobDataMap-Attributes
	
	public void setDate(DateTime date) {		//for test only
		this.date = date;
		log.info("called date()");
	}
	
	
	public void setObserver(ObservationObserver observer) {
		this.observer = observer;
		Log.info("called setObserver()");
	}

	
	
}
