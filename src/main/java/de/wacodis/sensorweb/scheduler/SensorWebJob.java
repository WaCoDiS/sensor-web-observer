package de.wacodis.sensorweb.scheduler;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.exception.EncodingException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;

import de.wacodis.dataaccess.model.SensorWebDataEnvelope;
import de.wacodis.dataaccess.model.AbstractDataEnvelope.SourceTypeEnum;
import de.wacodis.sensorweb.http.SimpleHttpPost;
import de.wacodis.sensorweb.observer.ObservationObserver;
import de.wacodis.sensorweb.publisher.PublishChannels;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SensorWebJob implements Job{
	
	
	private static final Logger log = LoggerFactory.getLogger(SensorWebJob.class);
	
	private final String FLUGGS_URL = "http://fluggs.wupperverband.de/sos2/sos/soap";
//	private final String N52_URL = "http://sensorweb.demo.52north.org/52n-sos-webapp/service";
	private final String N52_URL = "http://sensorweb.demo.52north.org/sensorwebtestbed/service";
	private final String PUBLISH_URL = "http://localhost:8080/pub";
	
	
	private List<String> procedures, observedProperties, offerings, featureIdentifiers;
	private DateTime dateOfLastObs, dateOfNextToLastObs;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		procedures = (List<String>) context.getMergedJobDataMap().get("procedures");
		observedProperties = (List<String>) context.getMergedJobDataMap().get("observedProperties");
		offerings = (List<String>) context.getMergedJobDataMap().get("offerings");
		featureIdentifiers = (List<String>) context.getMergedJobDataMap().get("featureIdentifiers");
		ObservationObserver observer = new ObservationObserver(N52_URL, procedures, observedProperties, offerings, featureIdentifiers);

		try {
			log.info("called SensorWebJob's execute()");
			log.info("SensorWebJob's Observer = {}", observer != null);
			
			dateOfLastObs = (DateTime) context.getMergedJobDataMap().get("dateOfLastObs");
			dateOfNextToLastObs = (DateTime) context.getMergedJobDataMap().get("dateOfNextToLastObs");
			
			if(dateOfLastObs != null && dateOfNextToLastObs != null) {
				observer.setDateOfLastObs(dateOfLastObs);
				observer.setDateOfNextToLastObs(dateOfNextToLastObs);
			} else {
				observer.setDateOfLastObs(new DateTime(2018, 3, 28, 4, 0, 0));		//for test only				
			}
			
			
			if(observer.checkForAvailableUpdates()) {
				dateOfLastObs = observer.getDateOfLastObs();
				dateOfNextToLastObs = observer.getDateOfNextToLastObs();
				observer.updateObservations(dateOfNextToLastObs, dateOfLastObs);
				
				//Build SensorWebDataEnvelope
				SensorWebDataEnvelope dataEnvelope = new SensorWebDataEnvelope();
				dataEnvelope.setServiceUrl(observer.getUrl());
				dataEnvelope.setOffering(observer.getOfferings().get(0));
				dataEnvelope.setFeatureOfInterest(observer.getFeatureIdentifiers().get(0));
				dataEnvelope.setObservedProperty(observer.getObservedProperties().get(0));
				dataEnvelope.setProcedure(observer.getProcedures().get(0));
				dataEnvelope.setSourceType(SourceTypeEnum.SENSORWEBDATAENVELOPE);
				
				
				
				log.info("dataEnvelope = {}", dataEnvelope.toString());
				PublishChannels pub = (PublishChannels) context.getScheduler().getContext().get(QuartzServer.PUBLISHER);
				publish(pub, dataEnvelope);
			}
			
			JobDataMap data = context.getJobDetail().getJobDataMap();
			data.put("dateOfLastObs", dateOfLastObs);
			data.put("dateOfNextToLastObs", dateOfNextToLastObs);
			
		} catch (EncodingException | DecodingException | XmlException | SchedulerException e) {
			log.info(e.getMessage(), e);
		}
		
	}
	
	public SensorWebDataEnvelope publish(PublishChannels pub, SensorWebDataEnvelope data) {
		pub.sendDataEnvelope().send(MessageBuilder.withPayload(data).build());
		log.info("Published: \n{}", data);
		return data;
	}

	public void setProcedures(List<String> procedures) {
		this.procedures = procedures;
	}

	public void setObservedProperties(List<String> observedProperties) {
		this.observedProperties = observedProperties;
	}

	public void setOfferings(List<String> offerings) {
		this.offerings = offerings;
	}

	public void setFeatureIdentifiers(List<String> featureIdentifiers) {
		this.featureIdentifiers = featureIdentifiers;
	}
	
}
