package de.wacodis.sensorweb.scheduler;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.exception.EncodingException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import de.wacodis.sensorweb.observer.ObservationObserver;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SensorWebJob implements org.quartz.Job{
	
	private ObservationObserver observer;
	private List<OmObservation> results;
	private DateTime date;	//fake date in past for test
	

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			observer.setDateOfLastObs(date);		//for test only
			if(observer.checkForAvailableUpdates()) {
				observer.updateObservations(observer.getDateOfNextToLastObs(), observer.getDateOfLastObs());
				results = observer.getObservations();
				//... notify broker?
				for(OmObservation o : results) {
					System.out.println(o.getValue().getValue());
				}
			}
			date = observer.getDateOfLastObs();
			JobDataMap data = context.getJobDetail().getJobDataMap();
			data.put("observer", observer);
			data.put("date", date);
			
		} catch (EncodingException | DecodingException | XmlException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	public void setDate(DateTime date) {
		this.date = date;
	}
	
	
	public void setObserver(ObservationObserver observer) {
		this.observer = observer;
	}

	
	
}
