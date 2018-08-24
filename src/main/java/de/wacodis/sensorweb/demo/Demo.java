package de.wacodis.sensorweb.demo;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.tz.DateTimeZoneBuilder;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.ObservationValue;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.values.Value;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse.DataAvailability;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.exception.EncodingException;

import com.google.common.collect.Lists;

import de.wacodis.sensorweb.decode.GetDataAvailabilityResDecoder;
import de.wacodis.sensorweb.decode.GetObservationResDecoder;
import de.wacodis.sensorweb.encode.GetDataAvailabilityReqEncoder;
import de.wacodis.sensorweb.encode.GetObservationReqEncoder;
import de.wacodis.sensorweb.http.SimpleHttpPost;
import de.wacodis.sensorweb.observer.ObservationObserver;
import de.wacodis.sensorweb.scheduler.QuartzServer;

public class Demo {
	
	public static void main(String[] args) {
		QuartzServer jdbcQuartzServer = new QuartzServer();
		jdbcQuartzServer.startup();
	}
	
	
//	public static void main(String[] args) {
//		String fluggsURL = "http://fluggs.wupperverband.de/sos2/sos/soap";
//		
//		// parameters to identify data
//		List<String> procedures = Lists.newArrayList("Einzelwert");
//		List<String> observedProperties = Lists.newArrayList("Wassertemperatur");
//		List<String> offerings = Lists.newArrayList("Zeitreihen_Einzelwert");
//		List<String> featureIdentifiers = Lists.newArrayList("Laaken");
//		
//		ObservationObserver observer = new ObservationObserver(fluggsURL, procedures, observedProperties, offerings, featureIdentifiers);
//		List<OmObservation> results;
//		
//		try {
//			//fake date for test
//			observer.setDateOfLastObs(new DateTime(2018, 8, 12, 21, 0, 0));
//			
//			if(observer.checkForAvailableUpdates()) {
//				observer.updateObservations(observer.getDateOfNextToLastObs(), observer.getDateOfLastObs());
//				results = observer.getObservations();
//				for(OmObservation o : results) {
//					System.out.println(o.getValue().getValue());
//				}
//			}
//		} catch (EncodingException | DecodingException | XmlException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public static void main(String[] args) throws EncodingException, DecodingException, XmlException, OwsExceptionReport {
//		String fluggsURL = "http://fluggs.wupperverband.de/sos2/sos/soap";
//		
//		// parameters to identify data
//		List<String> procedures = Lists.newArrayList("Einzelwert");
//		List<String> observedProperties = Lists.newArrayList("Wassertemperatur");
//		List<String> offerings = Lists.newArrayList("Zeitreihen_Einzelwert");
//		List<String> featureIdentifiers = Lists.newArrayList("Laaken");
//		
//		DateTime start = new DateTime(2018, 3, 3, 0, 0, 0);
//		DateTime end = new DateTime(2018, 3, 3, 12, 0, 0);
//		
//		GetObservationReqEncoder encoder = new GetObservationReqEncoder();
//		String request = encoder.encode(procedures, observedProperties, offerings, featureIdentifiers, start.minusSeconds(1), end.plusSeconds(1));
//		
//		SimpleHttpPost post = new SimpleHttpPost();
//		String response = post.doPost(fluggsURL, request);
//		
//		GetObservationResDecoder decoder = new GetObservationResDecoder();
//		List<OmObservation> results = decoder.decode(response);
//		for(OmObservation obs : results) {
//			Time a = obs.getValue().getPhenomenonTime();
//			Value<?> b = obs.getValue().getValue();
//			System.out.println(a + " _ " + b);
//		}
//	}
}
