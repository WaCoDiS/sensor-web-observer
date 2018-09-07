package de.wacodis.sensorweb.observer;

import java.io.Serializable;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.exception.EncodingException;

import de.wacodis.sensorweb.decode.GetDataAvailabilityResDecoder;
import de.wacodis.sensorweb.decode.GetObservationResDecoder;
import de.wacodis.sensorweb.encode.GetDataAvailabilityReqEncoder;
import de.wacodis.sensorweb.encode.GetObservationReqEncoder;
import de.wacodis.sensorweb.http.SimpleHttpPost;

public class ObservationObserver implements Serializable{

	private static final long serialVersionUID = 1L;
	private List<String> procedures;
	private List<String> observedProperties;
	private List<String> offerings;
	private List<String> featureIdentifiers;
	private String url;
	
	private List<OmObservation> observations;
	private DateTime dateOfLastObs;
	private DateTime dateOfNextToLastObs;
	private DateTime dateOfFirstObs;
	
	private GetObservationReqEncoder observationEncoder;
	private GetDataAvailabilityReqEncoder availabilityEncoder;
	private GetObservationResDecoder observationDecoder;
	private GetDataAvailabilityResDecoder availabilityDecoder;
	
	private SimpleHttpPost post;


	public ObservationObserver(String url, List<String> procedures, List<String> observedProperties, 
							List<String> offerings, List<String> featureIdentifiers) {
		this.url = url;
		this.procedures = procedures;
		this.observedProperties = observedProperties;
		this.offerings = offerings;
		this.featureIdentifiers = featureIdentifiers;
		
		this.observationEncoder = new GetObservationReqEncoder();
		this.availabilityEncoder = new GetDataAvailabilityReqEncoder();
		this.observationDecoder = new GetObservationResDecoder();
		this.availabilityDecoder = new GetDataAvailabilityResDecoder();
		this.post = new SimpleHttpPost("application/soap+xml", "application/soap+xml");
		initalize();
	}
	
	/**
	 * initialize dates of last observations with valid values.
	 */
	private void initalize() {
		dateOfLastObs = null;
		dateOfNextToLastObs = null;
		try {
			String request = availabilityEncoder.encode(procedures, observedProperties, offerings, featureIdentifiers);
			String response = post.doPost(url, request);
			dateOfFirstObs = availabilityDecoder.decode(response).get(0).getPhenomenonTime().getStart();
			dateOfLastObs = availabilityDecoder.decode(response).get(0).getPhenomenonTime().getEnd();
			dateOfNextToLastObs = dateOfLastObs;
		} catch (EncodingException | DecodingException | XmlException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return boolean true if date of last observation is before current observation.
	 * @throws EncodingException
	 * @throws DecodingException
	 * @throws XmlException
	 */
	public boolean checkForAvailableUpdates() throws EncodingException, DecodingException, XmlException {
		String request;
		if(dateOfLastObs == null) {
			throw new NullPointerException("date of last observation must not be null!");
		}
		request = availabilityEncoder.encode(procedures, observedProperties, offerings, featureIdentifiers);
		String response = post.doPost(url, request);
		DateTime responseDate = availabilityDecoder.decode(response).get(0).getPhenomenonTime().getEnd();
		if(responseDate != null && dateOfLastObs.isBefore(responseDate)) {
			dateOfNextToLastObs = dateOfLastObs;
			dateOfLastObs = responseDate;
			return true;
		}
		return false;
	}
	
	/**
	 * updates the list of observations for a given time period
	 * @param start 
	 * @param end
	 */
	public void updateObservations(DateTime start, DateTime end) {
		if (start == null || end == null) {
			throw new NullPointerException("date of start and end must not be null!");
		}
		try {
			String request = observationEncoder.encode(procedures, observedProperties, offerings,
					featureIdentifiers, start.minusSeconds(1), end.plusSeconds(1)); 
			String response = post.doPost(url, request);
			setObservations(observationDecoder.decode(response));
		} catch (EncodingException | DecodingException | XmlException | OwsExceptionReport e) {
			e.printStackTrace();
		}
	}


	// setters and getters
	public List<String> getProcedures() {
		return procedures;
	}

	public void setProcedures(List<String> procedures) {
		this.procedures = procedures;
	}

	public List<String> getObservedProperties() {
		return observedProperties;
	}

	public void setObservedProperties(List<String> observedProperties) {
		this.observedProperties = observedProperties;
	}

	public List<String> getOfferings() {
		return offerings;
	}

	public void setOfferings(List<String> offerings) {
		this.offerings = offerings;
	}

	public List<String> getFeatureIdentifiers() {
		return featureIdentifiers;
	}

	public void setFeatureIdentifiers(List<String> featureIdentifiers) {
		this.featureIdentifiers = featureIdentifiers;
	}

	public DateTime getDateOfLastObs() {
		return dateOfLastObs;
	}

	public void setDateOfLastObs(DateTime dateOfLastObs) {
		this.dateOfLastObs = dateOfLastObs;
	}

	public DateTime getDateOfNextToLastObs() {
		return dateOfNextToLastObs;
	}

	public void setDateOfNextToLastObs(DateTime dateOfNextToLastObs) {
		this.dateOfNextToLastObs = dateOfNextToLastObs;
	}

	public List<OmObservation> getObservations() {
		return observations;
	}

	public void setObservations(List<OmObservation> observations) {
		this.observations = observations;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public SimpleHttpPost getPost() {
		return post;
	}
	
	public void setPost(SimpleHttpPost post) {
		this.post = post;
	}
	
	public DateTime getDateOfFirstObs() {
		return dateOfFirstObs;
	}
	
	public void setDateOfFirstObs(DateTime dateOfFirstObs) {
		this.dateOfFirstObs = dateOfFirstObs;
	}
	
	
	

}
