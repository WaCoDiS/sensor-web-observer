package de.wacodis.sensorweb.http;

import de.wacodis.sensorweb.http.SimpleHttpPost;
import de.wacodis.sensorweb.util.SimpleFileReader;

public class DummyHttpPost extends SimpleHttpPost{
	
	public DummyHttpPost(String contentType, String accept) {
		super(contentType, accept);
	}

	private boolean isGetDataAvailability = false;
	private boolean isGetObservation = false;
	
	
	
	@Override
	public String doPost(String url, String payload) {
		if(isGetDataAvailability) {
			String resp = SimpleFileReader.readFile("src/test/resources/GetDataAvailabilityResponse.txt");
			return resp;
		}
		else if (isGetObservation) {
			String resp = SimpleFileReader.readFile("src/test/resources/GetObservationResponse.txt");
			return resp;
		}
		else return null;
	}

	public boolean isGetDataAvailability() {
		return isGetDataAvailability;
	}

	public void setGetDataAvailability(boolean isGetDataAvailability) {
		this.isGetDataAvailability = isGetDataAvailability;
	}

	public boolean isGetObservation() {
		return isGetObservation;
	}

	public void setGetObservation(boolean isGetObservation) {
		this.isGetObservation = isGetObservation;
	}
	
	

}
