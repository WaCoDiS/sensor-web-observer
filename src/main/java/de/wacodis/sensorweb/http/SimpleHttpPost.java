package de.wacodis.sensorweb.http;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.esotericsoftware.minlog.Log;

public class SimpleHttpPost implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpPost.class.getName());
	
	private String contentType;
	private String accept;
	
	public SimpleHttpPost(String contentType, String accept) {
		this.contentType = contentType;
		this.accept = accept;
	}
	
	public String doPost(String url, String payload) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
//		headers.set("Content-Type", "application/soap+xml");
//		headers.set("Accept", "application/soap+xml");
		headers.set("Content-Type", contentType);
		headers.set("Accept", accept);
		HttpEntity<String> requestBody = new HttpEntity<>(payload, headers);
		ResponseEntity<String> responseEntity = rest.postForEntity(url, requestBody, String.class);
		
		Log.info("Response from " + url + ": " + responseEntity.getBody());
		return responseEntity.getBody();
	}

}
