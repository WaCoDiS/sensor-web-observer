package de.wacodis.sensorweb.job;

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
import de.wacodis.observer.publisher.PublisherChannel;
import de.wacodis.observer.quartz.QuartzServer;
import de.wacodis.dataaccess.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.dataaccess.model.AbstractDataEnvelopeTimeFrame;
import de.wacodis.sensorweb.observer.ObservationObserver;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SensorWebJob implements Job{
	
	
	private static final Logger log = LoggerFactory.getLogger(SensorWebJob.class);
	
//	private final String FLUGGS_URL = "http://fluggs.wupperverband.de/sos2/sos/soap";
	private final String N52_URL = "http://sensorweb.demo.52north.org/sensorwebtestbed/service";
	
	
	private List<String> procedures, observedProperties, offerings, featureIdentifiers;
//	private DateTime dateOfLastObs;
	

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("Start SensorWebJob's execute()");
		procedures = (List<String>) context.getMergedJobDataMap().get("procedures");
		observedProperties = (List<String>) context.getMergedJobDataMap().get("observedProperties");
		offerings = (List<String>) context.getMergedJobDataMap().get("offerings");
		featureIdentifiers = (List<String>) context.getMergedJobDataMap().get("featureIdentifiers");
		ObservationObserver observer = new ObservationObserver(N52_URL, procedures, observedProperties, offerings, featureIdentifiers);
		if(context.getJobDetail().getJobDataMap().get("dateOfLastObs") == null) {
			context.getJobDetail().getJobDataMap().put("dateOfLastObs", observer.initalizeDatesOfObservation());
			context.getJobDetail().getJobDataMap().put("dateOfFirstObs", observer.getDateOfFirstObs());
		}

		try {
			DateTime dateOfLastObs = (DateTime) context.getJobDetail().getJobDataMap().get("dateOfLastObs");
			DateTime dateOfFirstObs = (DateTime) context.getJobDetail().getJobDataMap().get("dateOfFirstObs");

			if(observer.checkForAvailableUpdates(dateOfLastObs)) {	//if new data Available -> Publish new DataEnvelope
				dateOfLastObs = observer.getDateOfLastObs();
				dateOfFirstObs = observer.getDateOfFirstObs();
				
				//Build new SensorWebDataEnvelope
				SensorWebDataEnvelope dataEnvelope = new SensorWebDataEnvelope();
				dataEnvelope.setServiceUrl(observer.getUrl());
				dataEnvelope.setOffering(observer.getOfferings().get(0));
				dataEnvelope.setFeatureOfInterest(observer.getFeatureIdentifiers().get(0));
				dataEnvelope.setObservedProperty(observer.getObservedProperties().get(0));
				dataEnvelope.setProcedure(observer.getProcedures().get(0));
				dataEnvelope.setSourceType(SourceTypeEnum.SENSORWEBDATAENVELOPE);
				dataEnvelope.setAreaOfInterest(new AbstractDataEnvelopeAreaOfInterest().extent((List<Float>) context.getMergedJobDataMap().get("extent")));

				dataEnvelope.setModified(dateOfLastObs);
				dataEnvelope.setCreated((DateTime) context.getJobDetail().getJobDataMap().get("created"));
				
				AbstractDataEnvelopeTimeFrame timeFrame = new AbstractDataEnvelopeTimeFrame();
				timeFrame.setStartTime(dateOfFirstObs);
				timeFrame.setEndTime(dateOfLastObs);
				dataEnvelope.setTimeFrame(timeFrame);
				
				
				log.info("New dataEnvelope:\n{}", dataEnvelope.toString());
				
				//Publish DataEnvelope through PublishChannel to MessageBroker's Exchange
				PublisherChannel pub = (PublisherChannel) context.getScheduler().getContext().get(QuartzServer.PUBLISHER);
				publish(pub, dataEnvelope);

			}
			context.getJobDetail().getJobDataMap().put("dateOfLastObs", dateOfLastObs);
			context.getJobDetail().getJobDataMap().put("dateOfFirstObs", dateOfFirstObs);
			
			
		} catch (EncodingException | DecodingException | XmlException | SchedulerException e) {
			log.warn(e.getMessage(), e);
		}
		
	}
	
	public SensorWebDataEnvelope publish(PublisherChannel pub, SensorWebDataEnvelope data) {
		pub.sendDataEnvelope().send(MessageBuilder.withPayload(data).build());
		log.info("DataEnvelope published");
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
